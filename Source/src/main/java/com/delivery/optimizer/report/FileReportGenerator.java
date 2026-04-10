package com.delivery.optimizer.report;

import com.delivery.optimizer.io.ReportWriter;
import com.delivery.optimizer.model.DeliveryGraph;
import com.delivery.optimizer.model.Package;
import com.delivery.optimizer.model.Route;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class FileReportGenerator extends ReportGenerator {

    private final String outputPath;

    public FileReportGenerator(String outputPath) {
        this.outputPath = outputPath;
    }

    @Override
    protected void printHeader(StringBuilder sb) {
        sb.append("==========================================================\n");
        sb.append("           DELIVERY ROUTE OPTIMIZATION REPORT\n");
        sb.append("==========================================================\n\n");
    }

    @Override
    protected void printRouteSummary(StringBuilder sb, Route route, DeliveryGraph graph) {
        sb.append("Algorithm: ").append(route.getAlgorithmName()).append("\n");
        sb.append("Route:     ").append(route.toRouteString(graph)).append("\n");
        sb.append("Total Distance: ").append(String.format("%.2f", route.getTotalDistance())).append(" km\n");
        double travelMinutes = (route.getTotalDistance() / 40.0) * 60.0;
        sb.append("Estimated Travel Time: ").append(String.format("%.0f", travelMinutes)).append(" minutes\n");
        sb.append("\n");
    }

    @Override
    protected void printStopDetails(StringBuilder sb, Route route,
                                    DeliveryGraph graph, List<Package> packages) {
        sb.append("--- Per-Stop Breakdown ---\n");
        sb.append(formatStopDetails(route, graph, packages));
        sb.append("\n");
    }

    @Override
    protected void printConstraintViolations(StringBuilder sb, List<String> violations) {
        sb.append("--- Constraint Violations ---\n");
        if (violations == null || violations.isEmpty()) {
            sb.append("  No violations detected.\n");
        } else {
            for (String v : violations) {
                sb.append("  * ").append(v).append("\n");
            }
        }
        sb.append("\n");
    }

    @Override
    protected void printComparisonTable(StringBuilder sb, Map<String, Route> allRoutes) {
        sb.append("--- Algorithm Comparison ---\n");
        sb.append(String.format("  %-25s %-15s %-15s%n", "Algorithm", "Distance (km)", "Time (min)"));
        sb.append(String.format("  %-25s %-15s %-15s%n", "-------------------------", "-------------", "-----------"));
        for (Map.Entry<String, Route> entry : allRoutes.entrySet()) {
            double dist = entry.getValue().getTotalDistance();
            double time = (dist / 40.0) * 60.0;
            sb.append(String.format("  %-25s %-15.2f %-15.0f%n", entry.getKey(), dist, time));
        }
        sb.append("\n");
    }

    @Override
    protected void printFooter(StringBuilder sb) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        sb.append("Report generated at: ").append(timestamp).append("\n");
        sb.append("==========================================================\n");
    }

    @Override
    protected void outputReport(String report) {
        try {
            ReportWriter.writeToFile(outputPath, report);
        } catch (IOException e) {
            System.err.println("Warning: Could not write report to " + outputPath + ": " + e.getMessage());
        }
    }
}
