package com.delivery.optimizer.report;

import com.delivery.optimizer.model.DeliveryGraph;
import com.delivery.optimizer.model.Package;
import com.delivery.optimizer.model.Route;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;

public abstract class ReportGenerator {

    public final String generateReport(Route route, DeliveryGraph graph,
                                       List<Package> packages, List<String> violations,
                                       Map<String, Route> allRoutes) {
        StringBuilder sb = new StringBuilder();
        printHeader(sb);
        printRouteSummary(sb, route, graph);
        printStopDetails(sb, route, graph, packages);
        printConstraintViolations(sb, violations);
        if (allRoutes != null && allRoutes.size() > 1) {
            printComparisonTable(sb, allRoutes);
        }
        printFooter(sb);
        String report = sb.toString();
        outputReport(report);
        return report;
    }

    protected abstract void printHeader(StringBuilder sb);

    protected abstract void printRouteSummary(StringBuilder sb, Route route, DeliveryGraph graph);

    protected abstract void printStopDetails(StringBuilder sb, Route route,
                                             DeliveryGraph graph, List<Package> packages);

    protected abstract void printConstraintViolations(StringBuilder sb, List<String> violations);

    protected abstract void printComparisonTable(StringBuilder sb, Map<String, Route> allRoutes);

    protected abstract void printFooter(StringBuilder sb);

    protected abstract void outputReport(String report);

    protected String formatStopDetails(Route route, DeliveryGraph graph, List<Package> packages) {
        StringBuilder sb = new StringBuilder();
        double cumulativeDistance = 0;
        LocalTime departure = LocalTime.of(8, 0);
        List<Integer> sequence = route.getNodeSequence();

        sb.append(String.format("  %-6s %-20s %-10s %-10s %-10s %-15s%n",
            "Stop", "Label", "Priority", "Weight", "Arrival", "Time Window"));
        sb.append(String.format("  %-6s %-20s %-10s %-10s %-10s %-15s%n",
            "----", "----", "--------", "------", "-------", "-----------"));

        for (int i = 1; i < sequence.size() - 1; i++) {
            int nodeIndex = sequence.get(i);
            cumulativeDistance += graph.getDistance(sequence.get(i - 1), nodeIndex);
            double travelMinutes = (cumulativeDistance / 40.0) * 60.0;
            LocalTime arrival = departure.plusMinutes((long) travelMinutes);

            String nodeName = graph.getNodeName(nodeIndex);
            Package pkg = findPackage(packages, nodeName);

            String label = pkg != null ? pkg.getLabel() : "N/A";
            String priority = pkg != null ? pkg.getPriority().toString() : "N/A";
            String weight = pkg != null ? String.format("%.1f kg", pkg.getWeightKg()) : "N/A";
            String timeWindow = pkg != null ?
                pkg.getTimeWindowOpen() + "-" + pkg.getTimeWindowClose() : "N/A";

            int arrivalMin = arrival.getHour() * 60 + arrival.getMinute();
            String status = "";
            if (pkg != null) {
                int open = pkg.getTimeWindowOpenMinutes();
                int close = pkg.getTimeWindowCloseMinutes();
                if (open >= 0 && close >= 0) {
                    if (arrivalMin >= open && arrivalMin <= close) {
                        status = " [OK]";
                    } else {
                        status = " [LATE]";
                    }
                }
            }

            sb.append(String.format("  %-6s %-20s %-10s %-10s %-10s %-15s%n",
                nodeName, label, priority, weight, arrival.toString() + status, timeWindow));
        }
        return sb.toString();
    }

    protected Package findPackage(List<Package> packages, String nodeId) {
        for (Package pkg : packages) {
            if (pkg.getId().equals(nodeId)) {
                return pkg;
            }
        }
        return null;
    }
}
