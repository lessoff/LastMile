package com.delivery.optimizer.model;

public enum Priority {
    HIGH, NORMAL, LOW;

    public static Priority fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return NORMAL;
        }
        try {
            return Priority.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return NORMAL;
        }
    }
}
