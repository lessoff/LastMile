package com.delivery.optimizer.observer;

import com.delivery.optimizer.model.Package;
import com.delivery.optimizer.model.Route;

import java.time.LocalTime;

public interface DeliveryObserver {
    void onStopReached(Package stop, LocalTime arrivalTime);
    void onConstraintViolation(Package stop, String violation);
    void onRouteComplete(Route route, double totalDistance);
}
