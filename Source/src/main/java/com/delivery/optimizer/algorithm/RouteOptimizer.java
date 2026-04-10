package com.delivery.optimizer.algorithm;

import com.delivery.optimizer.model.DeliveryGraph;
import com.delivery.optimizer.model.Package;
import com.delivery.optimizer.model.Route;
import com.delivery.optimizer.observer.DeliveryObserver;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RouteOptimizer {
    private RoutingStrategy strategy;
    private final List<DeliveryObserver> observers = new ArrayList<>();

    public void setStrategy(RoutingStrategy strategy) {
        this.strategy = strategy;
    }

    public void addObserver(DeliveryObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(DeliveryObserver observer) {
        observers.remove(observer);
    }

    public Route computeRoute(DeliveryGraph graph, List<Package> packages) {
        if (strategy == null) {
            throw new IllegalStateException("No routing strategy set.");
        }
        Route route = strategy.optimize(graph, packages);
        replayRoute(route, graph, packages);
        return route;
    }

    public Map<String, Route> computeAllRoutes(DeliveryGraph graph, List<Package> packages) {
        Map<String, Route> results = new LinkedHashMap<>();

        RoutingStrategy[] strategies = {
            new NearestNeighborStrategy(),
            new GreedyEdgeStrategy(),
            new TwoOptStrategy()
        };

        for (RoutingStrategy strat : strategies) {
            setStrategy(strat);
            Route route = strat.optimize(graph, packages);
            results.put(strat.getName(), route);
        }

        // Replay the best route for observer notifications
        Route bestRoute = null;
        for (Route route : results.values()) {
            if (bestRoute == null || route.getTotalDistance() < bestRoute.getTotalDistance()) {
                bestRoute = route;
            }
        }
        if (bestRoute != null) {
            replayRoute(bestRoute, graph, packages);
        }

        return results;
    }

    private void replayRoute(Route route, DeliveryGraph graph, List<Package> packages) {
        double cumulativeDistance = 0;
        LocalTime departureTime = LocalTime.of(8, 0);
        List<Integer> sequence = route.getNodeSequence();

        for (int i = 1; i < sequence.size() - 1; i++) {
            int nodeIndex = sequence.get(i);
            cumulativeDistance += graph.getDistance(sequence.get(i - 1), nodeIndex);

            // Calculate arrival time: distance / 40 km/h -> hours -> add to departure
            double travelMinutes = (cumulativeDistance / 40.0) * 60.0;
            LocalTime arrivalTime = departureTime.plusMinutes((long) travelMinutes);

            // Find the package for this node
            String nodeName = graph.getNodeName(nodeIndex);
            Package pkg = findPackage(packages, nodeName);

            if (pkg != null) {
                for (DeliveryObserver observer : observers) {
                    observer.onStopReached(pkg, arrivalTime);
                }
            }
        }

        for (DeliveryObserver observer : observers) {
            observer.onRouteComplete(route, route.getTotalDistance());
        }
    }

    private Package findPackage(List<Package> packages, String nodeId) {
        for (Package pkg : packages) {
            if (pkg.getId().equals(nodeId)) {
                return pkg;
            }
        }
        return null;
    }
}
