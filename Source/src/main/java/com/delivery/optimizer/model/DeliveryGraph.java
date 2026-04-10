package com.delivery.optimizer.model;

import com.delivery.optimizer.exception.InvalidMatrixException;

import java.util.Arrays;

public class DeliveryGraph {
    private final double[][] matrix;
    private final String[] nodeNames;

    public DeliveryGraph(double[][] matrix, String[] nodeNames) {
        validate(matrix, nodeNames);
        this.matrix = matrix;
        this.nodeNames = nodeNames;
    }

    private void validate(double[][] matrix, String[] nodeNames) {
        if (matrix == null || matrix.length == 0) {
            throw new InvalidMatrixException("Distance matrix is empty or null.");
        }
        int n = matrix.length;
        if (nodeNames == null || nodeNames.length != n) {
            throw new InvalidMatrixException("Node names count does not match matrix size.");
        }
        for (int i = 0; i < n; i++) {
            if (matrix[i].length != n) {
                throw new InvalidMatrixException(
                    "Matrix is not square. Row " + i + " has " + matrix[i].length +
                    " columns, expected " + n + ".");
            }
        }
        for (int i = 0; i < n; i++) {
            if (matrix[i][i] != 0) {
                throw new InvalidMatrixException(
                    "Diagonal value at [" + i + "][" + i + "] is " + matrix[i][i] + ", expected 0.");
            }
        }
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (matrix[i][j] < 0) {
                    throw new InvalidMatrixException(
                        "Negative distance " + matrix[i][j] + " at [" + i + "][" + j + "].");
                }
            }
        }
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (Math.abs(matrix[i][j] - matrix[j][i]) > 0.001) {
                    System.err.println("Warning: Matrix is not symmetric at [" + i + "][" + j +
                        "]: " + matrix[i][j] + " vs " + matrix[j][i]);
                }
            }
        }
    }

    public double getDistance(int from, int to) {
        return matrix[from][to];
    }

    public int getNodeCount() {
        return matrix.length;
    }

    public String getNodeName(int index) {
        return nodeNames[index];
    }

    public int getNodeIndex(String name) {
        for (int i = 0; i < nodeNames.length; i++) {
            if (nodeNames[i].equals(name)) {
                return i;
            }
        }
        return -1;
    }

    public String[] getAllNodeNames() {
        return Arrays.copyOf(nodeNames, nodeNames.length);
    }

    public boolean isSymmetric() {
        int n = matrix.length;
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (Math.abs(matrix[i][j] - matrix[j][i]) > 0.001) {
                    return false;
                }
            }
        }
        return true;
    }
}
