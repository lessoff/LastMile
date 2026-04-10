package com.delivery.optimizer.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Route {
    private final List<Integer> nodeSequence;
    private double totalDistance;
    private final String algorithmName;

    public Route(List<Integer> nodeSequence, double totalDistance, String algorithmName) {
        this.nodeSequence = new ArrayList<>(nodeSequence);
        this.totalDistance = totalDistance;
        this.algorithmName = algorithmName;
    }

    public List<Integer> getNodeSequence() {
        return Collections.unmodifiableList(nodeSequence);
    }

    public double getTotalDistance() {
        return totalDistance;
    }

    public String getAlgorithmName() {
        return algorithmName;
    }

    public List<Integer> getStops() {
        if (nodeSequence.size() <= 2) return Collections.emptyList();
        return Collections.unmodifiableList(nodeSequence.subList(1, nodeSequence.size() - 1));
    }

    public double calculateTotalDistance(DeliveryGraph graph) {
        double total = 0;
        for (int i = 0; i < nodeSequence.size() - 1; i++) {
            total += graph.getDistance(nodeSequence.get(i), nodeSequence.get(i + 1));
        }
        this.totalDistance = total;
        return total;
    }

    public String toRouteString(DeliveryGraph graph) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < nodeSequence.size(); i++) {
            if (i > 0) sb.append(" → ");
            sb.append(graph.getNodeName(nodeSequence.get(i)));
        }
        return sb.toString();
    }
}
