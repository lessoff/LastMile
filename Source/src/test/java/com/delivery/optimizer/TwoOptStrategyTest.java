package com.delivery.optimizer;

import com.delivery.optimizer.algorithm.NearestNeighborStrategy;
import com.delivery.optimizer.algorithm.TwoOptStrategy;
import com.delivery.optimizer.model.DeliveryGraph;
import com.delivery.optimizer.model.Package;
import com.delivery.optimizer.model.Priority;
import com.delivery.optimizer.model.Route;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TwoOptStrategyTest {

    @Test
    void testOptimize_improvesOnNearestNeighbor() {
        // Classic crossing example:
        // Depot(0,0), A(1,3), B(3,1), C(4,4), D(6,2)
        // Use Euclidean-like distances that create a crossing in NN
        // NN: Depot->A(nearest)->B->C->D->Depot  may cross
        // 2-opt should uncross and reduce distance
        double[][] matrix = {
            {0, 10, 25, 30, 20},
            {10, 0, 30, 15, 25},
            {25, 30, 0, 20, 10},
            {30, 15, 20, 0, 18},
            {20, 25, 10, 18, 0}
        };
        String[] names = {"Depot", "A", "B", "C", "D"};
        DeliveryGraph graph = new DeliveryGraph(matrix, names);

        List<Package> packages = Arrays.asList(
            new Package("A", "A", Priority.NORMAL, 1.0, "08:00", "18:00"),
            new Package("B", "B", Priority.NORMAL, 1.0, "08:00", "18:00"),
            new Package("C", "C", Priority.NORMAL, 1.0, "08:00", "18:00"),
            new Package("D", "D", Priority.NORMAL, 1.0, "08:00", "18:00")
        );

        NearestNeighborStrategy nn = new NearestNeighborStrategy();
        Route nnRoute = nn.optimize(graph, packages);

        TwoOptStrategy twoOpt = new TwoOptStrategy();
        Route twoOptRoute = twoOpt.optimize(graph, packages);

        assertTrue(twoOptRoute.getTotalDistance() <= nnRoute.getTotalDistance(),
            "2-Opt (" + twoOptRoute.getTotalDistance() + ") should be <= NN (" +
                nnRoute.getTotalDistance() + ")");
    }

    // Test 20
    @Test
    void testOptimize_singleStop_returnsValidRoute() {
        double[][] matrix = {
            {0, 15},
            {15, 0}
        };
        String[] names = {"Depot", "A"};
        DeliveryGraph graph = new DeliveryGraph(matrix, names);
        List<Package> packages = Arrays.asList(
            new Package("A", "Stop A", Priority.HIGH, 2.0, "08:00", "10:00")
        );

        TwoOptStrategy twoOpt = new TwoOptStrategy();
        Route route = twoOpt.optimize(graph, packages);

        assertEquals(Arrays.asList(0, 1, 0), route.getNodeSequence(),
            "Single-stop 2-Opt route must be Depot → A → Depot");
        assertEquals(30.0, route.getTotalDistance(), 0.001);
    }

    // Test 21
    @Test
    void testOptimize_allStopsVisited_noDuplicates() {
        double[][] matrix = {
            {0, 10, 20, 30},
            {10, 0, 15, 25},
            {20, 15, 0, 10},
            {30, 25, 10, 0}
        };
        String[] names = {"Depot", "A", "B", "C"};
        DeliveryGraph graph = new DeliveryGraph(matrix, names);
        List<Package> packages = Arrays.asList(
            new Package("A", "A", Priority.NORMAL, 1.0, "08:00", "18:00"),
            new Package("B", "B", Priority.NORMAL, 1.0, "08:00", "18:00"),
            new Package("C", "C", Priority.NORMAL, 1.0, "08:00", "18:00")
        );

        TwoOptStrategy twoOpt = new TwoOptStrategy();
        Route route = twoOpt.optimize(graph, packages);

        List<Integer> stops = route.getStops();
        assertEquals(3, stops.size(), "All 3 stops must appear in route");
        assertEquals(3, stops.stream().distinct().count(), "No duplicate stops");
    }

    // Test 22
    @Test
    void testGetName_returnsTwoOptImprovement() {
        assertEquals("2-Opt Improvement", new TwoOptStrategy().getName());
    }

    @Test
    void testOptimize_alreadyOptimal_returnsUnchanged() {
        // Triangle: Depot-A-B-Depot where the NN route is already optimal
        double[][] matrix = {
            {0, 5, 10},
            {5, 0, 5},
            {10, 5, 0}
        };
        String[] names = {"Depot", "A", "B"};
        DeliveryGraph graph = new DeliveryGraph(matrix, names);

        List<Package> packages = Arrays.asList(
            new Package("A", "A", Priority.NORMAL, 1.0, "08:00", "18:00"),
            new Package("B", "B", Priority.NORMAL, 1.0, "08:00", "18:00")
        );

        NearestNeighborStrategy nn = new NearestNeighborStrategy();
        Route nnRoute = nn.optimize(graph, packages);

        TwoOptStrategy twoOpt = new TwoOptStrategy();
        Route twoOptRoute = twoOpt.optimize(graph, packages);

        assertEquals(nnRoute.getTotalDistance(), twoOptRoute.getTotalDistance(), 0.001,
            "2-Opt should not change an already optimal route");
    }
}
