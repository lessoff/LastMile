package com.delivery.optimizer.observer;

import com.delivery.optimizer.model.Package;
import com.delivery.optimizer.model.Priority;
import com.delivery.optimizer.model.Route;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ConstraintChecker implements DeliveryObserver {

    private final List<String> violations = new ArrayList<>();
    private boolean highPriorityComplete = false;
    private boolean normalDelivered = false;

    @Override
    public void onStopReached(Package stop, LocalTime arrivalTime) {
        // Check priority ordering: HIGH should come before NORMAL/LOW
        if (stop.getPriority() == Priority.NORMAL || stop.getPriority() == Priority.LOW) {
            normalDelivered = true;
        }
        if (stop.getPriority() == Priority.HIGH && normalDelivered) {
            String msg = "HIGH priority package " + stop.getId() +
                " delivered after NORMAL/LOW priority packages";
            violations.add(msg);
        }

        // Check time window
        int arrivalMinutes = arrivalTime.getHour() * 60 + arrivalTime.getMinute();
        int windowOpen = stop.getTimeWindowOpenMinutes();
        int windowClose = stop.getTimeWindowCloseMinutes();

        if (windowOpen >= 0 && windowClose >= 0) {
            if (arrivalMinutes < windowOpen) {
                String msg = "Early arrival at " + stop.getId() + ": arrived at " +
                    arrivalTime + " but window opens at " + stop.getTimeWindowOpen();
                violations.add(msg);
            } else if (arrivalMinutes > windowClose) {
                String msg = "Late arrival at " + stop.getId() + ": arrived at " +
                    arrivalTime + " but window closes at " + stop.getTimeWindowClose();
                violations.add(msg);
            }
        }
    }

    @Override
    public void onConstraintViolation(Package stop, String violation) {
        violations.add(violation);
    }

    @Override
    public void onRouteComplete(Route route, double totalDistance) {
        // No additional checks needed at route completion
    }

    public List<String> getViolations() {
        return new ArrayList<>(violations);
    }

    public void reset() {
        violations.clear();
        highPriorityComplete = false;
        normalDelivered = false;
    }
}
