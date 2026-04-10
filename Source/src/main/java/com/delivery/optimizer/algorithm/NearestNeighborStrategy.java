package com.delivery.optimizer.algorithm;

import com.delivery.optimizer.model.DeliveryGraph;
import com.delivery.optimizer.model.Package;
import com.delivery.optimizer.model.Route;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NearestNeighborStrategy implements RoutingStrategy {

    @Override
    public Route optimize(DeliveryGraph graph, List<Package> packages) {
        Set<Integer> nodesToVisit = new HashSet<>();
        for (Package pkg : packages) {
            int idx = graph.getNodeIndex(pkg.getId());
            if (idx > 0) {
                nodesToVisit.add(idx);
            }
        }

        List<Integer> route = new ArrayList<>();
        route.add(0); // Start at depot

        Set<Integer> visited = new HashSet<>();
        int current = 0;

        while (visited.size() < nodesToVisit.size()) {
            double minDist = Double.MAX_VALUE;
            int nearest = -1;

            for (int node : nodesToVisit) {
                if (!visited.contains(node)) {
                    double dist = graph.getDistance(current, node);
                    if (dist < minDist) {
                        minDist = dist;
                        nearest = node;
                    }
                }
            }

            if (nearest == -1) break;

            route.add(nearest);
            visited.add(nearest);
            current = nearest;
        }

        route.add(0); // Return to depot

        Route result = new Route(route, 0, getName());
        result.calculateTotalDistance(graph);
        return result;
    }

    @Override
    public String getName() {
        return "Nearest Neighbor";
    }
}
