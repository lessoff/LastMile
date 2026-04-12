package com.delivery.optimizer;

import com.delivery.optimizer.exception.InvalidMatrixException;
import com.delivery.optimizer.model.DeliveryGraph;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DeliveryGraphTest {

    @Test
    void testGetDistance_symmetricMatrix_returnsCorrectValue() {
        double[][] matrix = {
            {0, 10, 20},
            {10, 0, 15},
            {20, 15, 0}
        };
        String[] names = {"Depot", "A", "B"};
        DeliveryGraph graph = new DeliveryGraph(matrix, names);

        assertEquals(10, graph.getDistance(0, 1));
        assertEquals(10, graph.getDistance(1, 0));
        assertEquals(20, graph.getDistance(0, 2));
        assertEquals(15, graph.getDistance(1, 2));
        assertTrue(graph.isSymmetric());
    }

    @Test
    void testGetDistance_invalidNode_throwsException() {
        double[][] matrix = {
            {0, 10},
            {10, 0}
        };
        String[] names = {"Depot", "A"};
        DeliveryGraph graph = new DeliveryGraph(matrix, names);

        assertThrows(ArrayIndexOutOfBoundsException.class,
            () -> graph.getDistance(0, 5));
    }

    @Test
    void testIsSymmetric_asymmetricInput_returnsFalse() {
        double[][] matrix = {
            {0, 10},
            {15, 0}
        };
        String[] names = {"Depot", "A"};
        DeliveryGraph graph = new DeliveryGraph(matrix, names);

        assertFalse(graph.isSymmetric());
    }

    @Test
    void testConstructor_nonSquareMatrix_throwsInvalidMatrixException() {
        double[][] matrix = {
            {0, 10, 20},
            {10, 0, 15}
        };
        String[] names = {"Depot", "A", "B"};

        assertThrows(InvalidMatrixException.class,
            () -> new DeliveryGraph(matrix, names));
    }

    // Test 29
    @Test
    void testGetNodeIndex_existingNode_returnsCorrectIndex() {
        double[][] matrix = {
            {0, 10, 20},
            {10, 0, 15},
            {20, 15, 0}
        };
        String[] names = {"Depot", "A", "B"};
        DeliveryGraph graph = new DeliveryGraph(matrix, names);

        assertEquals(0, graph.getNodeIndex("Depot"), "'Depot' should be at index 0");
        assertEquals(1, graph.getNodeIndex("A"), "'A' should be at index 1");
        assertEquals(2, graph.getNodeIndex("B"), "'B' should be at index 2");
    }

    // Test 30
    @Test
    void testGetNodeIndex_unknownNode_returnsNegativeOne() {
        double[][] matrix = {
            {0, 10},
            {10, 0}
        };
        String[] names = {"Depot", "A"};
        DeliveryGraph graph = new DeliveryGraph(matrix, names);

        assertEquals(-1, graph.getNodeIndex("Z"),
            "Querying a node name that does not exist should return -1");
    }

    @Test
    void testConstructor_negativeDistance_throwsInvalidMatrixException() {
        double[][] matrix = {
            {0, -5},
            {-5, 0}
        };
        String[] names = {"Depot", "A"};

        assertThrows(InvalidMatrixException.class,
            () -> new DeliveryGraph(matrix, names));
    }
}
