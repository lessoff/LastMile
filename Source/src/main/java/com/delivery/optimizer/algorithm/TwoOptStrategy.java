package com.delivery.optimizer.algorithm;

import com.delivery.optimizer.model.DeliveryGraph;
import com.delivery.optimizer.model.Package;
import com.delivery.optimizer.model.Route;

import java.util.ArrayList;
import java.util.List;

public class TwoOptStrategy implements RoutingStrategy {

    private final NearestNeighborStrategy nnStrategy;

    public TwoOptStrategy() {
        this.nnStrategy = new NearestNeighborStrategy();
    }

    @Override
    public Route optimize(DeliveryGraph graph, List<Package> packages) {
        // Get initial solution from Nearest Neighbor
        Route initialRoute = nnStrategy.optimize(graph, packages);
        List<Integer> bestRoute = new ArrayList<>(initialRoute.getNodeSequence());
        double bestDistance = initialRoute.getTotalDistance();

        boolean improved = true;
        while (improved) {
            improved = false;
            // Iterate over all pairs (i, j) excluding depot positions
            for (int i = 1; i < bestRoute.size() - 2; i++) {
                for (int j = i + 1; j < bestRoute.size() - 1; j++) {
                    double delta = calculateDelta(graph, bestRoute, i, j);
                    if (delta < -0.0001) {
                        // Reverse the segment between i and j
                        reverse(bestRoute, i, j);
                        bestDistance += delta;
                        improved = true;
                    }
                }
            }
        }

        Route result = new Route(bestRoute, bestDistance, getName());
        result.calculateTotalDistance(graph);
        return result;
    }

    private double calculateDelta(DeliveryGraph graph, List<Integer> route, int i, int j) {
        int a = route.get(i - 1);
        int b = route.get(i);
        int c = route.get(j);
        int d = route.get(j + 1);

        double oldDist = graph.getDistance(a, b) + graph.getDistance(c, d);
        double newDist = graph.getDistance(a, c) + graph.getDistance(b, d);
        return newDist - oldDist;
    }

    private void reverse(List<Integer> route, int i, int j) {
        while (i < j) {
            int temp = route.get(i);
            route.set(i, route.get(j));
            route.set(j, temp);
            i++;
            j--;
        }
    }

    @Override
    public String getName() {
        return "2-Opt Improvement";
    }
}
