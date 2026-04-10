package com.delivery.optimizer.observer;

import com.delivery.optimizer.model.Package;
import com.delivery.optimizer.model.Route;

import java.time.LocalTime;

public class ConsoleLogger implements DeliveryObserver {

    @Override
    public void onStopReached(Package stop, LocalTime arrivalTime) {
        System.out.println("[LOG] Reached stop " + stop.getId() + " (" + stop.getLabel() +
            ") at " + arrivalTime + " | Priority: " + stop.getPriority() +
            " | Weight: " + stop.getWeightKg() + " kg");
    }

    @Override
    public void onConstraintViolation(Package stop, String violation) {
        System.out.println("[VIOLATION] " + stop.getId() + ": " + violation);
    }

    @Override
    public void onRouteComplete(Route route, double totalDistance) {
        System.out.println("[LOG] Route complete (" + route.getAlgorithmName() +
            "). Total distance: " + String.format("%.2f", totalDistance) + " km");
    }
}
