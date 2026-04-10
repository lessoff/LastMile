package com.delivery.optimizer.io;

import com.delivery.optimizer.exception.InvalidMatrixException;
import com.delivery.optimizer.model.DeliveryGraph;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CsvMatrixReader {

    public DeliveryGraph read(String filePath) {
        List<String[]> rows = new ArrayList<>();
        String[] nodeNames;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String headerLine = br.readLine();
            if (headerLine == null || headerLine.trim().isEmpty()) {
                throw new InvalidMatrixException("CSV file is empty.");
            }

            String[] headerParts = headerLine.split(",");
            int nodeCount = headerParts.length - 1;
            if (nodeCount <= 0) {
                throw new InvalidMatrixException("CSV header has no node names.");
            }

            nodeNames = new String[nodeCount];
            for (int i = 0; i < nodeCount; i++) {
                nodeNames[i] = headerParts[i + 1].trim();
            }

            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",");
                rows.add(parts);
            }
        } catch (IOException e) {
            throw new InvalidMatrixException("Could not read file: " + filePath + " - " + e.getMessage());
        }

        int n = nodeNames.length;
        if (rows.size() != n) {
            throw new InvalidMatrixException(
                "Matrix is not square. Expected " + n + " data rows, got " + rows.size() + ".");
        }

        double[][] matrix = new double[n][n];
        for (int i = 0; i < n; i++) {
            String[] parts = rows.get(i);
            if (parts.length - 1 != n) {
                throw new InvalidMatrixException(
                    "Row " + i + " has " + (parts.length - 1) + " values, expected " + n + ".");
            }
            for (int j = 0; j < n; j++) {
                try {
                    matrix[i][j] = Double.parseDouble(parts[j + 1].trim());
                } catch (NumberFormatException e) {
                    throw new InvalidMatrixException(
                        "Invalid number '" + parts[j + 1].trim() + "' at row " + i + ", col " + j + ".");
                }
                if (matrix[i][j] < 0) {
                    throw new InvalidMatrixException(
                        "Negative distance " + matrix[i][j] + " at row " + i + ", col " + j + ".");
                }
            }
        }

        return new DeliveryGraph(matrix, nodeNames);
    }
}
