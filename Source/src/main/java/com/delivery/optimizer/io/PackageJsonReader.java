package com.delivery.optimizer.io;

import com.delivery.optimizer.exception.InvalidPackageDataException;
import com.delivery.optimizer.model.DeliveryGraph;
import com.delivery.optimizer.model.Package;
import com.delivery.optimizer.model.Priority;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PackageJsonReader {

    public List<Package> read(String filePath, DeliveryGraph graph) {
        List<Package> packages = new ArrayList<>();

        try (FileReader reader = new FileReader(filePath)) {
            JsonArray array = JsonParser.parseReader(reader).getAsJsonArray();

            for (JsonElement element : array) {
                JsonObject obj = element.getAsJsonObject();

                String id = getStringField(obj, "id");
                String label = getStringField(obj, "label");
                String priorityStr = getStringField(obj, "priority");
                double weight = getDoubleField(obj, "weightKg");
                String timeOpen = getStringField(obj, "timeWindowOpen");
                String timeClose = getStringField(obj, "timeWindowClose");

                if (id == null || id.isEmpty()) {
                    throw new InvalidPackageDataException("Package is missing 'id' field.");
                }

                int nodeIndex = graph.getNodeIndex(id);
                if (nodeIndex < 0) {
                    throw new InvalidPackageDataException(
                        "Package '" + id + "' references a stop not in the distance matrix.");
                }

                if (weight < 0) {
                    throw new InvalidPackageDataException(
                        "Package '" + id + "' has negative weight: " + weight);
                }

                validateTimeFormat(id, timeOpen, "timeWindowOpen");
                validateTimeFormat(id, timeClose, "timeWindowClose");

                Priority priority = Priority.fromString(priorityStr);
                packages.add(new Package(id, label, priority, weight, timeOpen, timeClose));
            }
        } catch (IOException e) {
            throw new InvalidPackageDataException("Could not read file: " + filePath + " - " + e.getMessage());
        } catch (Exception e) {
            if (e instanceof InvalidPackageDataException) throw e;
            throw new InvalidPackageDataException("Error parsing packages JSON: " + e.getMessage());
        }

        return packages;
    }

    private String getStringField(JsonObject obj, String field) {
        if (obj.has(field) && !obj.get(field).isJsonNull()) {
            return obj.get(field).getAsString();
        }
        return "";
    }

    private double getDoubleField(JsonObject obj, String field) {
        if (obj.has(field) && !obj.get(field).isJsonNull()) {
            return obj.get(field).getAsDouble();
        }
        return 0.0;
    }

    private void validateTimeFormat(String packageId, String time, String fieldName) {
        if (time == null || time.isEmpty()) return;
        if (!time.matches("\\d{2}:\\d{2}")) {
            throw new InvalidPackageDataException(
                "Package '" + packageId + "' has invalid time format for " + fieldName + ": " + time);
        }
        String[] parts = time.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        if (hours < 0 || hours > 23 || minutes < 0 || minutes > 59) {
            throw new InvalidPackageDataException(
                "Package '" + packageId + "' has invalid time value for " + fieldName + ": " + time);
        }
    }
}
