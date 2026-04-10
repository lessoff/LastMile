package com.delivery.optimizer;

import com.delivery.optimizer.exception.InvalidMatrixException;
import com.delivery.optimizer.io.CsvMatrixReader;
import com.delivery.optimizer.model.DeliveryGraph;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class CsvMatrixReaderTest {

    @TempDir
    Path tempDir;

    private File createTempCsv(String content) throws IOException {
        File file = tempDir.resolve("test.csv").toFile();
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        }
        return file;
    }

    @Test
    void testRead_validCsv_returnsCorrectGraph() throws IOException {
        String csv = ",Depot,A,B\n" +
                     "Depot,0,10,20\n" +
                     "A,10,0,15\n" +
                     "B,20,15,0\n";

        File file = createTempCsv(csv);
        CsvMatrixReader reader = new CsvMatrixReader();
        DeliveryGraph graph = reader.read(file.getAbsolutePath());

        assertEquals(3, graph.getNodeCount());
        assertEquals("Depot", graph.getNodeName(0));
        assertEquals("A", graph.getNodeName(1));
        assertEquals("B", graph.getNodeName(2));
        assertEquals(10, graph.getDistance(0, 1));
        assertEquals(20, graph.getDistance(0, 2));
        assertEquals(15, graph.getDistance(1, 2));
    }

    @Test
    void testRead_nonSquareMatrix_throwsInvalidMatrixException() throws IOException {
        String csv = ",Depot,A,B\n" +
                     "Depot,0,10,20\n" +
                     "A,10,0,15\n";

        File file = createTempCsv(csv);
        CsvMatrixReader reader = new CsvMatrixReader();

        assertThrows(InvalidMatrixException.class,
            () -> reader.read(file.getAbsolutePath()));
    }

    @Test
    void testRead_negativeDistance_throwsInvalidMatrixException() throws IOException {
        String csv = ",Depot,A\n" +
                     "Depot,0,-5\n" +
                     "A,-5,0\n";

        File file = createTempCsv(csv);
        CsvMatrixReader reader = new CsvMatrixReader();

        assertThrows(InvalidMatrixException.class,
            () -> reader.read(file.getAbsolutePath()));
    }
}
