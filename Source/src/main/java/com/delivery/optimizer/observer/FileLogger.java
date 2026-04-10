package com.delivery.optimizer.observer;

import com.delivery.optimizer.model.Package;
import com.delivery.optimizer.model.Route;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FileLogger implements DeliveryObserver {

    private final String logFilePath;
    private static final DateTimeFormatter TIMESTAMP_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public FileLogger(String logFilePath) {
        this.logFilePath = logFilePath;
        File file = new File(logFilePath);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
    }

    @Override
    public void onStopReached(Package stop, LocalTime arrivalTime) {
        log("[STOP] " + stop.getId() + " (" + stop.getLabel() + ") arrived at " +
            arrivalTime + " | Priority: " + stop.getPriority() + " | Weight: " + stop.getWeightKg() + " kg");
    }

    @Override
    public void onConstraintViolation(Package stop, String violation) {
        log("[VIOLATION] " + stop.getId() + ": " + violation);
    }

    @Override
    public void onRouteComplete(Route route, double totalDistance) {
        log("[COMPLETE] Route (" + route.getAlgorithmName() + ") total distance: " +
            String.format("%.2f", totalDistance) + " km");
    }

    private void log(String message) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFilePath, true))) {
            String timestamp = LocalDateTime.now().format(TIMESTAMP_FMT);
            writer.write(timestamp + " " + message);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Warning: Could not write to log file: " + e.getMessage());
        }
    }
}
