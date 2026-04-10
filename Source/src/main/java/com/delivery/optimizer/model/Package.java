package com.delivery.optimizer.model;

public class Package {
    private String id;
    private String label;
    private Priority priority;
    private double weightKg;
    private String timeWindowOpen;
    private String timeWindowClose;

    public Package(String id, String label, Priority priority, double weightKg,
                   String timeWindowOpen, String timeWindowClose) {
        this.id = id;
        this.label = label;
        this.priority = priority;
        this.weightKg = weightKg;
        this.timeWindowOpen = timeWindowOpen;
        this.timeWindowClose = timeWindowClose;
    }

    public String getId() { return id; }
    public String getLabel() { return label; }
    public Priority getPriority() { return priority; }
    public double getWeightKg() { return weightKg; }
    public String getTimeWindowOpen() { return timeWindowOpen; }
    public String getTimeWindowClose() { return timeWindowClose; }

    public int getTimeWindowOpenMinutes() {
        return parseTimeToMinutes(timeWindowOpen);
    }

    public int getTimeWindowCloseMinutes() {
        return parseTimeToMinutes(timeWindowClose);
    }

    private int parseTimeToMinutes(String time) {
        if (time == null || time.isEmpty()) return -1;
        String[] parts = time.split(":");
        return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
    }

    @Override
    public String toString() {
        return "Package{id='" + id + "', label='" + label + "', priority=" + priority +
               ", weight=" + weightKg + "kg, window=" + timeWindowOpen + "-" + timeWindowClose + "}";
    }
}
