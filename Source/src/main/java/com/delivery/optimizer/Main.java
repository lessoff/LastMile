package com.delivery.optimizer;

import com.delivery.optimizer.algorithm.*;
import com.delivery.optimizer.io.CsvMatrixReader;
import com.delivery.optimizer.io.PackageJsonReader;
import com.delivery.optimizer.model.DeliveryGraph;
import com.delivery.optimizer.model.Package;
import com.delivery.optimizer.model.Priority;
import com.delivery.optimizer.model.Route;
import com.delivery.optimizer.observer.ConsoleLogger;
import com.delivery.optimizer.observer.ConstraintChecker;
import com.delivery.optimizer.observer.FileLogger;
import com.delivery.optimizer.report.ConsoleReportGenerator;
import com.delivery.optimizer.report.FileReportGenerator;

import java.io.File;
import java.util.*;

public class Main {

    public static void main(String[] args) {
        try {
            run(args);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void run(String[] args) {
        // Parse arguments
        String matrixPath = "data/distances.csv";
        String packagesPath = "data/packages.json";
        String algorithm = "all";
        double capacity = Double.MAX_VALUE;
        String outputPath = "Docs/last_report.txt";

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--matrix":
                    if (i + 1 < args.length) matrixPath = args[++i];
                    break;
                case "--packages":
                    if (i + 1 < args.length) packagesPath = args[++i];
                    break;
                case "--algorithm":
                    if (i + 1 < args.length) algorithm = args[++i].toLowerCase();
                    break;
                case "--capacity":
                    if (i + 1 < args.length) capacity = Double.parseDouble(args[++i]);
                    break;
                case "--output":
                    if (i + 1 < args.length) outputPath = args[++i];
                    break;
                default:
                    System.err.println("Unknown argument: " + args[i]);
                    break;
            }
        }

        // Validate matrix file exists
        if (!new File(matrixPath).exists()) {
            System.err.println("Error: Distance matrix file not found: " + matrixPath);
            System.exit(1);
        }

        // Validate packages file exists
        if (!new File(packagesPath).exists()) {
            System.err.println("Error: Packages file not found: " + packagesPath);
            System.exit(1);
        }

        // Read inputs
        CsvMatrixReader matrixReader = new CsvMatrixReader();
        DeliveryGraph graph = matrixReader.read(matrixPath);

        PackageJsonReader packageReader = new PackageJsonReader();
        List<Package> packages = packageReader.read(packagesPath, graph);

        if (packages.isEmpty()) {
            System.out.println("No packages to deliver.");
            return;
        }

        // Check total weight vs capacity
        double totalWeight = 0;
        for (Package pkg : packages) {
            totalWeight += pkg.getWeightKg();
        }

        if (totalWeight == 0 && capacity < Double.MAX_VALUE) {
            System.err.println("Warning: Total package weight is 0 but --capacity was set.");
        }

        // Set up observers
        RouteOptimizer optimizer = new RouteOptimizer();
        ConsoleLogger consoleLogger = new ConsoleLogger();
        FileLogger fileLogger = new FileLogger("logs/events.log");
        ConstraintChecker constraintChecker = new ConstraintChecker();

        optimizer.addObserver(consoleLogger);
        optimizer.addObserver(fileLogger);
        optimizer.addObserver(constraintChecker);

        // Handle capacity splitting
        List<List<Package>> trips = splitByCapacity(packages, capacity);

        if (trips.size() > 1) {
            System.out.println("\nVehicle capacity: " + capacity + " kg | Total weight: " +
                String.format("%.1f", totalWeight) + " kg");
            System.out.println("Splitting into " + trips.size() + " trips.\n");
        }

        // Run algorithms for each trip
        Map<String, Route> allAlgorithmRoutes = new LinkedHashMap<>();
        double grandTotalDistance = 0;

        for (int t = 0; t < trips.size(); t++) {
            List<Package> tripPackages = trips.get(t);

            if (trips.size() > 1) {
                double tripWeight = 0;
                for (Package pkg : tripPackages) tripWeight += pkg.getWeightKg();
                System.out.println("--- Trip " + (t + 1) + " (" +
                    String.format("%.1f", tripWeight) + " kg) ---");
            }

            if ("all".equals(algorithm)) {
                Map<String, Route> routes = optimizer.computeAllRoutes(graph, tripPackages);
                for (Map.Entry<String, Route> entry : routes.entrySet()) {
                    String key = trips.size() > 1 ?
                        entry.getKey() + " (Trip " + (t + 1) + ")" : entry.getKey();
                    allAlgorithmRoutes.put(key, entry.getValue());
                }
                // Use the best route for the report
                Route bestRoute = null;
                for (Route route : routes.values()) {
                    if (bestRoute == null || route.getTotalDistance() < bestRoute.getTotalDistance()) {
                        bestRoute = route;
                    }
                }
                if (bestRoute != null) {
                    grandTotalDistance += bestRoute.getTotalDistance();
                }
            } else {
                RoutingStrategy strategy = createStrategy(algorithm);
                optimizer.setStrategy(strategy);
                Route route = optimizer.computeRoute(graph, tripPackages);
                allAlgorithmRoutes.put(
                    trips.size() > 1 ? route.getAlgorithmName() + " (Trip " + (t + 1) + ")" :
                        route.getAlgorithmName(), route);
                grandTotalDistance += route.getTotalDistance();
            }
        }

        System.out.println();

        // Generate reports
        List<String> violations = constraintChecker.getViolations();

        // Determine the primary route for the report
        Route primaryRoute = allAlgorithmRoutes.values().iterator().next();
        if ("all".equals(algorithm)) {
            // Pick the best route for the primary display
            for (Route r : allAlgorithmRoutes.values()) {
                if (r.getTotalDistance() < primaryRoute.getTotalDistance()) {
                    primaryRoute = r;
                }
            }
        }

        // Console report
        ConsoleReportGenerator consoleReport = new ConsoleReportGenerator();
        consoleReport.generateReport(primaryRoute, graph, packages, violations,
            "all".equals(algorithm) ? allAlgorithmRoutes : null);

        // File report
        FileReportGenerator fileReport = new FileReportGenerator(outputPath);
        fileReport.generateReport(primaryRoute, graph, packages, violations,
            "all".equals(algorithm) ? allAlgorithmRoutes : null);

        if (trips.size() > 1) {
            System.out.println("Grand total distance (all trips): " +
                String.format("%.2f", grandTotalDistance) + " km");
        }

        System.out.println("\nReport saved to: " + outputPath);
        System.out.println("Event log saved to: logs/events.log");
    }

    private static RoutingStrategy createStrategy(String algorithm) {
        switch (algorithm) {
            case "nn":
                return new NearestNeighborStrategy();
            case "greedy":
                return new GreedyEdgeStrategy();
            case "twoopt":
                return new TwoOptStrategy();
            default:
                System.err.println("Unknown algorithm: " + algorithm +
                    ". Valid options: nn, greedy, twoopt, all");
                System.exit(1);
                return null;
        }
    }

    private static List<List<Package>> splitByCapacity(List<Package> packages, double capacity) {
        if (capacity >= Double.MAX_VALUE) {
            return Collections.singletonList(packages);
        }

        // Sort by priority (HIGH first) then by weight descending
        List<Package> sorted = new ArrayList<>(packages);
        sorted.sort((a, b) -> {
            int priorityCompare = a.getPriority().compareTo(b.getPriority());
            if (priorityCompare != 0) return priorityCompare;
            return Double.compare(b.getWeightKg(), a.getWeightKg());
        });

        List<List<Package>> trips = new ArrayList<>();
        List<Package> currentTrip = new ArrayList<>();
        double currentWeight = 0;

        for (Package pkg : sorted) {
            if (currentWeight + pkg.getWeightKg() > capacity && !currentTrip.isEmpty()) {
                trips.add(currentTrip);
                currentTrip = new ArrayList<>();
                currentWeight = 0;
            }
            currentTrip.add(pkg);
            currentWeight += pkg.getWeightKg();
        }

        if (!currentTrip.isEmpty()) {
            trips.add(currentTrip);
        }

        return trips;
    }
}
