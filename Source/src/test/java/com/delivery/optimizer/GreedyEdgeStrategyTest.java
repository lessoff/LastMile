package com.delivery.optimizer;

import com.delivery.optimizer.algorithm.GreedyEdgeStrategy;
import com.delivery.optimizer.model.DeliveryGraph;
import com.delivery.optimizer.model.Package;
import com.delivery.optimizer.model.Priority;
import com.delivery.optimizer.model.Route;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GreedyEdgeStrategyTest {

    // Test 1
    @Test
    void testOptimize_threeNodes_startsAndEndsAtDepot() {
        double[][] matrix = {
            {0, 5, 20},
            {5, 0, 10},
            {20, 10, 0}
        };
        String[] names = {"Depot", "A", "B"};
        DeliveryGraph graph = new DeliveryGraph(matrix, names);
        List<Package> packages = Arrays.asList(
            new Package("A", "Stop A", Priority.NORMAL, 2.0, "08:00", "12:00"),
            new Package("B", "Stop B", Priority.NORMAL, 3.0, "08:00", "14:00")
        );

        GreedyEdgeStrategy greedy = new GreedyEdgeStrategy();
        Route route = greedy.optimize(graph, packages);

        List<Integer> seq = route.getNodeSequence();
        assertEquals(0, seq.get(0), "Route must start at Depot (node 0)");
        assertEquals(0, seq.get(seq.size() - 1), "Route must end at Depot (node 0)");
    }

    // Test 2
    @Test
    void testOptimize_threeNodes_allStopsVisited() {
        double[][] matrix = {
            {0, 5, 20},
            {5, 0, 10},
            {20, 10, 0}
        };
        String[] names = {"Depot", "A", "B"};
        DeliveryGraph graph = new DeliveryGraph(matrix, names);
        List<Package> packages = Arrays.asList(
            new Package("A", "Stop A", Priority.NORMAL, 2.0, "08:00", "12:00"),
            new Package("B", "Stop B", Priority.NORMAL, 3.0, "08:00", "14:00")
        );

        GreedyEdgeStrategy greedy = new GreedyEdgeStrategy();
        Route route = greedy.optimize(graph, packages);

        List<Integer> stops = route.getStops();
        assertEquals(2, stops.size(), "Should visit exactly 2 stops");
        assertTrue(stops.contains(1), "Stop A (index 1) must be visited");
        assertTrue(stops.contains(2), "Stop B (index 2) must be visited");
    }

    // Test 3
    @Test
    void testOptimize_threeNodes_correctTotalDistance() {
        // Greedy picks edges by cost: (Depot-A)=5, (A-B)=10, (B-Depot)=20 → total=35
        double[][] matrix = {
            {0, 5, 20},
            {5, 0, 10},
            {20, 10, 0}
        };
        String[] names = {"Depot", "A", "B"};
        DeliveryGraph graph = new DeliveryGraph(matrix, names);
        List<Package> packages = Arrays.asList(
            new Package("A", "Stop A", Priority.NORMAL, 2.0, "08:00", "12:00"),
            new Package("B", "Stop B", Priority.NORMAL, 3.0, "08:00", "14:00")
        );

        GreedyEdgeStrategy greedy = new GreedyEdgeStrategy();
        Route route = greedy.optimize(graph, packages);

        assertEquals(35.0, route.getTotalDistance(), 0.001,
            "Greedy picks cheapest edges: 5+10+20=35");
    }

    // Test 4
    @Test
    void testOptimize_singleStop_returnsDepotStopDepot() {
        double[][] matrix = {
            {0, 12},
            {12, 0}
        };
        String[] names = {"Depot", "A"};
        DeliveryGraph graph = new DeliveryGraph(matrix, names);
        List<Package> packages = Arrays.asList(
            new Package("A", "Stop A", Priority.HIGH, 1.0, "08:00", "10:00")
        );

        GreedyEdgeStrategy greedy = new GreedyEdgeStrategy();
        Route route = greedy.optimize(graph, packages);

        assertEquals(Arrays.asList(0, 1, 0), route.getNodeSequence(),
            "Single stop route must be Depot → A → Depot");
    }

    // Test 5
    @Test
    void testOptimize_singleStop_distanceIsRoundTrip() {
        double[][] matrix = {
            {0, 12},
            {12, 0}
        };
        String[] names = {"Depot", "A"};
        DeliveryGraph graph = new DeliveryGraph(matrix, names);
        List<Package> packages = Arrays.asList(
            new Package("A", "Stop A", Priority.NORMAL, 1.0, "08:00", "17:00")
        );

        GreedyEdgeStrategy greedy = new GreedyEdgeStrategy();
        Route route = greedy.optimize(graph, packages);

        assertEquals(24.0, route.getTotalDistance(), 0.001,
            "Round trip distance should be 2 × 12 = 24");
    }

    // Test 6
    @Test
    void testOptimize_fiveNodes_allStopsVisited() {
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

        GreedyEdgeStrategy greedy = new GreedyEdgeStrategy();
        Route route = greedy.optimize(graph, packages);

        List<Integer> stops = route.getStops();
        assertEquals(4, stops.size(), "All 4 stops must be visited");
        assertTrue(stops.contains(1), "Stop A must be visited");
        assertTrue(stops.contains(2), "Stop B must be visited");
        assertTrue(stops.contains(3), "Stop C must be visited");
        assertTrue(stops.contains(4), "Stop D must be visited");
    }

    // Test 7
    @Test
    void testOptimize_fiveNodes_depotFirstAndLast() {
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
            new Package("A", "A", Priority.HIGH, 2.0, "08:00", "18:00"),
            new Package("B", "B", Priority.NORMAL, 2.0, "08:00", "18:00"),
            new Package("C", "C", Priority.NORMAL, 2.0, "08:00", "18:00"),
            new Package("D", "D", Priority.LOW, 2.0, "08:00", "18:00")
        );

        GreedyEdgeStrategy greedy = new GreedyEdgeStrategy();
        Route route = greedy.optimize(graph, packages);

        List<Integer> seq = route.getNodeSequence();
        assertEquals(0, seq.get(0), "Route must start at Depot");
        assertEquals(0, seq.get(seq.size() - 1), "Route must end at Depot");
        assertEquals(6, seq.size(), "Sequence should be: Depot + 4 stops + Depot = 6 nodes");
    }

    // Test 8
    @Test
    void testGetName_returnsGreedyEdgeInsertion() {
        assertEquals("Greedy Edge Insertion", new GreedyEdgeStrategy().getName());
    }
}
