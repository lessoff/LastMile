package com.delivery.optimizer;

import com.delivery.optimizer.model.DeliveryGraph;
import com.delivery.optimizer.model.Route;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RouteTest {

    // Test 15
    @Test
    void testGetStops_multipleStops_returnsMiddleNodesOnly() {
        // Sequence: Depot(0) → A(1) → B(2) → C(3) → Depot(0)
        // getStops() should return [1, 2, 3] — not the depot bookends
        Route route = new Route(Arrays.asList(0, 1, 2, 3, 0), 0.0, "Test");

        List<Integer> stops = route.getStops();

        assertEquals(3, stops.size());
        assertEquals(1, stops.get(0));
        assertEquals(2, stops.get(1));
        assertEquals(3, stops.get(2));
    }

    // Test 16
    @Test
    void testGetStops_singleStop_returnsOneElement() {
        Route route = new Route(Arrays.asList(0, 1, 0), 0.0, "Test");

        List<Integer> stops = route.getStops();

        assertEquals(1, stops.size());
        assertEquals(1, stops.get(0));
    }

    // Test 17
    @Test
    void testGetStops_onlyDepot_returnsEmpty() {
        // A route that never leaves the depot — size is 2 ([0, 0])
        Route route = new Route(Arrays.asList(0, 0), 0.0, "Test");

        List<Integer> stops = route.getStops();

        assertTrue(stops.isEmpty(), "No stops when route only contains depot");
    }

    // Test 18
    @Test
    void testCalculateTotalDistance_correctSum() {
        // Depot(0) → A(1) → B(2) → Depot(0)
        // Distances: 0→1 = 10, 1→2 = 15, 2→0 = 20 → total = 45
        double[][] matrix = {
            {0, 10, 20},
            {10, 0, 15},
            {20, 15, 0}
        };
        String[] names = {"Depot", "A", "B"};
        DeliveryGraph graph = new DeliveryGraph(matrix, names);
        Route route = new Route(Arrays.asList(0, 1, 2, 0), 0.0, "Test");

        double calculated = route.calculateTotalDistance(graph);

        assertEquals(45.0, calculated, 0.001, "0→1(10) + 1→2(15) + 2→0(20) = 45");
        assertEquals(45.0, route.getTotalDistance(), 0.001,
            "getTotalDistance() should reflect the recalculated value");
    }

    // Test 19
    @Test
    void testToRouteString_containsAllNodeNames() {
        double[][] matrix = {
            {0, 10, 20},
            {10, 0, 15},
            {20, 15, 0}
        };
        String[] names = {"Depot", "Alpha", "Beta"};
        DeliveryGraph graph = new DeliveryGraph(matrix, names);
        Route route = new Route(Arrays.asList(0, 1, 2, 0), 0.0, "Test");

        String routeStr = route.toRouteString(graph);

        assertTrue(routeStr.contains("Depot"), "Route string should contain 'Depot'");
        assertTrue(routeStr.contains("Alpha"), "Route string should contain 'Alpha'");
        assertTrue(routeStr.contains("Beta"), "Route string should contain 'Beta'");
        assertTrue(routeStr.contains("→"), "Route string should use '→' separator");
    }
}
