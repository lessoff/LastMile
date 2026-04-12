package com.delivery.optimizer;

import com.delivery.optimizer.algorithm.NearestNeighborStrategy;
import com.delivery.optimizer.model.DeliveryGraph;
import com.delivery.optimizer.model.Package;
import com.delivery.optimizer.model.Priority;
import com.delivery.optimizer.model.Route;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NearestNeighborStrategyTest {

    @Test
    void testOptimize_threeNodes_returnsShortestGreedyRoute() {
        // Graph: Depot=0, A=1, B=2
        // Depot-A=5, Depot-B=20, A-B=10
        // NN should go Depot -> A (nearest=5) -> B (10) -> Depot (20) = 35
        double[][] matrix = {
            {0, 5, 20},
            {5, 0, 10},
            {20, 10, 0}
        };
        String[] names = {"Depot", "A", "B"};
        DeliveryGraph graph = new DeliveryGraph(matrix, names);

        List<Package> packages = Arrays.asList(
            new Package("A", "Stop A", Priority.NORMAL, 1.0, "08:00", "12:00"),
            new Package("B", "Stop B", Priority.NORMAL, 1.0, "08:00", "12:00")
        );

        NearestNeighborStrategy nn = new NearestNeighborStrategy();
        Route route = nn.optimize(graph, packages);

        List<Integer> seq = route.getNodeSequence();
        assertEquals(0, seq.get(0), "Route should start at Depot");
        assertEquals(1, seq.get(1), "First stop should be A (nearest to Depot)");
        assertEquals(2, seq.get(2), "Second stop should be B");
        assertEquals(0, seq.get(3), "Route should end at Depot");
        assertEquals(35.0, route.getTotalDistance(), 0.001);
    }

    @Test
    void testOptimize_allEqualDistances_returnsValidRoute() {
        double[][] matrix = {
            {0, 10, 10, 10},
            {10, 0, 10, 10},
            {10, 10, 0, 10},
            {10, 10, 10, 0}
        };
        String[] names = {"Depot", "A", "B", "C"};
        DeliveryGraph graph = new DeliveryGraph(matrix, names);

        List<Package> packages = Arrays.asList(
            new Package("A", "Stop A", Priority.NORMAL, 1.0, "08:00", "12:00"),
            new Package("B", "Stop B", Priority.NORMAL, 1.0, "08:00", "12:00"),
            new Package("C", "Stop C", Priority.NORMAL, 1.0, "08:00", "12:00")
        );

        NearestNeighborStrategy nn = new NearestNeighborStrategy();
        Route route = nn.optimize(graph, packages);

        List<Integer> seq = route.getNodeSequence();
        assertEquals(5, seq.size(), "Should have 5 entries: Depot + 3 stops + Depot");
        assertEquals(0, seq.get(0), "Should start at Depot");
        assertEquals(0, seq.get(4), "Should end at Depot");

        // All 3 stops visited exactly once
        List<Integer> stops = route.getStops();
        assertEquals(3, stops.size());
        assertTrue(stops.contains(1));
        assertTrue(stops.contains(2));
        assertTrue(stops.contains(3));
        assertEquals(40.0, route.getTotalDistance(), 0.001);
    }

    @Test
    void testOptimize_singleStop_returnsDepotStopDepot() {
        double[][] matrix = {
            {0, 15},
            {15, 0}
        };
        String[] names = {"Depot", "A"};
        DeliveryGraph graph = new DeliveryGraph(matrix, names);

        List<Package> packages = Arrays.asList(
            new Package("A", "Stop A", Priority.HIGH, 2.0, "08:00", "10:00")
        );

        NearestNeighborStrategy nn = new NearestNeighborStrategy();
        Route route = nn.optimize(graph, packages);

        assertEquals(Arrays.asList(0, 1, 0), route.getNodeSequence());
        assertEquals(30.0, route.getTotalDistance(), 0.001);
    }

    // Test 28
    @Test
    void testOptimize_mixedPriorities_allStopsVisited() {
        double[][] matrix = {
            {0, 10, 20, 30},
            {10, 0, 15, 25},
            {20, 15, 0, 10},
            {30, 25, 10, 0}
        };
        String[] names = {"Depot", "A", "B", "C"};
        DeliveryGraph graph = new DeliveryGraph(matrix, names);
        List<Package> packages = Arrays.asList(
            new Package("A", "Stop A", Priority.HIGH, 5.0, "08:00", "10:00"),
            new Package("B", "Stop B", Priority.NORMAL, 3.0, "09:00", "13:00"),
            new Package("C", "Stop C", Priority.LOW, 2.0, "10:00", "16:00")
        );

        NearestNeighborStrategy nn = new NearestNeighborStrategy();
        Route route = nn.optimize(graph, packages);

        List<Integer> stops = route.getStops();
        assertEquals(3, stops.size(), "All 3 stops must be visited regardless of priority mix");
        assertTrue(stops.contains(1), "HIGH priority stop A must be visited");
        assertTrue(stops.contains(2), "NORMAL priority stop B must be visited");
        assertTrue(stops.contains(3), "LOW priority stop C must be visited");
    }

    @Test
    void testOptimize_returnsToDepot() {
        double[][] matrix = {
            {0, 10, 20, 30},
            {10, 0, 15, 25},
            {20, 15, 0, 10},
            {30, 25, 10, 0}
        };
        String[] names = {"Depot", "A", "B", "C"};
        DeliveryGraph graph = new DeliveryGraph(matrix, names);

        List<Package> packages = Arrays.asList(
            new Package("A", "Stop A", Priority.NORMAL, 1.0, "08:00", "12:00"),
            new Package("B", "Stop B", Priority.NORMAL, 1.0, "08:00", "12:00"),
            new Package("C", "Stop C", Priority.NORMAL, 1.0, "08:00", "12:00")
        );

        NearestNeighborStrategy nn = new NearestNeighborStrategy();
        Route route = nn.optimize(graph, packages);

        List<Integer> seq = route.getNodeSequence();
        assertEquals(0, seq.get(seq.size() - 1), "Last node in route must be Depot");
        assertEquals(0, seq.get(0), "First node in route must be Depot");
    }
}
