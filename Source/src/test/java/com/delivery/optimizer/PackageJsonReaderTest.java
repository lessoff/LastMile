package com.delivery.optimizer;

import com.delivery.optimizer.exception.InvalidPackageDataException;
import com.delivery.optimizer.io.PackageJsonReader;
import com.delivery.optimizer.model.DeliveryGraph;
import com.delivery.optimizer.model.Package;
import com.delivery.optimizer.model.Priority;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PackageJsonReaderTest {

    @TempDir
    Path tempDir;

    private File createTempJson(String content) throws IOException {
        File file = tempDir.resolve("packages.json").toFile();
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        }
        return file;
    }

    private DeliveryGraph buildGraph(String[] names, double dist) {
        int n = names.length;
        double[][] matrix = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                matrix[i][j] = (i == j) ? 0 : dist;
            }
        }
        return new DeliveryGraph(matrix, names);
    }

    // Test 9
    @Test
    void testRead_validJson_returnsCorrectPackageCount() throws IOException {
        String json = "["
            + "{\"id\":\"A\",\"label\":\"Stop A\",\"priority\":\"NORMAL\",\"weightKg\":5.0,\"timeWindowOpen\":\"08:00\",\"timeWindowClose\":\"12:00\"},"
            + "{\"id\":\"B\",\"label\":\"Stop B\",\"priority\":\"NORMAL\",\"weightKg\":3.0,\"timeWindowOpen\":\"09:00\",\"timeWindowClose\":\"15:00\"}"
            + "]";

        File file = createTempJson(json);
        DeliveryGraph graph = buildGraph(new String[]{"Depot", "A", "B"}, 10.0);
        PackageJsonReader reader = new PackageJsonReader();

        List<Package> packages = reader.read(file.getAbsolutePath(), graph);

        assertEquals(2, packages.size(), "Should parse 2 packages from JSON");
    }

    // Test 10
    @Test
    void testRead_validJson_highPriorityParsedCorrectly() throws IOException {
        String json = "["
            + "{\"id\":\"A\",\"label\":\"Stop A\",\"priority\":\"HIGH\",\"weightKg\":8.0,\"timeWindowOpen\":\"07:00\",\"timeWindowClose\":\"10:00\"}"
            + "]";

        File file = createTempJson(json);
        DeliveryGraph graph = buildGraph(new String[]{"Depot", "A"}, 15.0);
        PackageJsonReader reader = new PackageJsonReader();

        List<Package> packages = reader.read(file.getAbsolutePath(), graph);

        assertEquals(Priority.HIGH, packages.get(0).getPriority(),
            "Priority field 'HIGH' should be parsed as Priority.HIGH");
        assertEquals(8.0, packages.get(0).getWeightKg(), 0.001,
            "Weight should be read correctly");
    }

    // Test 11
    @Test
    void testRead_unknownPriority_defaultsToNormal() throws IOException {
        String json = "["
            + "{\"id\":\"A\",\"label\":\"Stop A\",\"priority\":\"URGENT\",\"weightKg\":2.0,\"timeWindowOpen\":\"08:00\",\"timeWindowClose\":\"12:00\"}"
            + "]";

        File file = createTempJson(json);
        DeliveryGraph graph = buildGraph(new String[]{"Depot", "A"}, 10.0);
        PackageJsonReader reader = new PackageJsonReader();

        List<Package> packages = reader.read(file.getAbsolutePath(), graph);

        assertEquals(Priority.NORMAL, packages.get(0).getPriority(),
            "Unrecognized priority string should default to NORMAL");
    }

    // Test 12
    @Test
    void testRead_negativeWeight_throwsInvalidPackageDataException() throws IOException {
        String json = "["
            + "{\"id\":\"A\",\"label\":\"Stop A\",\"priority\":\"NORMAL\",\"weightKg\":-5.0,\"timeWindowOpen\":\"08:00\",\"timeWindowClose\":\"12:00\"}"
            + "]";

        File file = createTempJson(json);
        DeliveryGraph graph = buildGraph(new String[]{"Depot", "A"}, 10.0);
        PackageJsonReader reader = new PackageJsonReader();

        assertThrows(InvalidPackageDataException.class,
            () -> reader.read(file.getAbsolutePath(), graph),
            "Negative weight should throw InvalidPackageDataException");
    }

    // Test 13
    @Test
    void testRead_packageIdNotInGraph_throwsInvalidPackageDataException() throws IOException {
        String json = "["
            + "{\"id\":\"Z\",\"label\":\"Unknown Stop\",\"priority\":\"NORMAL\",\"weightKg\":2.0,\"timeWindowOpen\":\"08:00\",\"timeWindowClose\":\"12:00\"}"
            + "]";

        File file = createTempJson(json);
        // Graph only has "Depot" and "A" — no "Z"
        DeliveryGraph graph = buildGraph(new String[]{"Depot", "A"}, 10.0);
        PackageJsonReader reader = new PackageJsonReader();

        assertThrows(InvalidPackageDataException.class,
            () -> reader.read(file.getAbsolutePath(), graph),
            "Package referencing a stop not in the graph should throw exception");
    }

    // Test 14
    @Test
    void testRead_invalidTimeFormat_throwsInvalidPackageDataException() throws IOException {
        // "8:0" does not match the required HH:MM format
        String json = "["
            + "{\"id\":\"A\",\"label\":\"Stop A\",\"priority\":\"NORMAL\",\"weightKg\":2.0,\"timeWindowOpen\":\"8:0\",\"timeWindowClose\":\"12:00\"}"
            + "]";

        File file = createTempJson(json);
        DeliveryGraph graph = buildGraph(new String[]{"Depot", "A"}, 10.0);
        PackageJsonReader reader = new PackageJsonReader();

        assertThrows(InvalidPackageDataException.class,
            () -> reader.read(file.getAbsolutePath(), graph),
            "Malformed time format should throw InvalidPackageDataException");
    }
}
