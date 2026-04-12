package com.delivery.optimizer;

import com.delivery.optimizer.model.Package;
import com.delivery.optimizer.model.Priority;
import com.delivery.optimizer.model.Route;
import com.delivery.optimizer.observer.ConstraintChecker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConstraintCheckerTest {

    private ConstraintChecker checker;

    @BeforeEach
    void setUp() {
        checker = new ConstraintChecker();
    }

    @Test
    void testOnStopReached_highPriorityBeforeNormal_noViolation() {
        Package highPkg = new Package("A", "A", Priority.HIGH, 5.0, "08:00", "12:00");
        Package normalPkg = new Package("B", "B", Priority.NORMAL, 3.0, "08:00", "14:00");

        checker.onStopReached(highPkg, LocalTime.of(8, 30));
        checker.onStopReached(normalPkg, LocalTime.of(9, 0));

        List<String> violations = checker.getViolations();
        boolean hasPriorityViolation = violations.stream()
            .anyMatch(v -> v.contains("HIGH priority"));
        assertFalse(hasPriorityViolation, "No priority violation expected");
    }

    @Test
    void testOnStopReached_normalBeforeHighPriority_flagsViolation() {
        Package normalPkg = new Package("B", "B", Priority.NORMAL, 3.0, "08:00", "14:00");
        Package highPkg = new Package("A", "A", Priority.HIGH, 5.0, "08:00", "12:00");

        checker.onStopReached(normalPkg, LocalTime.of(8, 30));
        checker.onStopReached(highPkg, LocalTime.of(9, 0));

        List<String> violations = checker.getViolations();
        boolean hasPriorityViolation = violations.stream()
            .anyMatch(v -> v.contains("HIGH priority"));
        assertTrue(hasPriorityViolation, "Should flag HIGH priority delivered after NORMAL");
    }

    @Test
    void testOnStopReached_arrivalWithinTimeWindow_noViolation() {
        Package pkg = new Package("A", "A", Priority.NORMAL, 2.0, "09:00", "12:00");

        checker.onStopReached(pkg, LocalTime.of(10, 0));

        List<String> violations = checker.getViolations();
        boolean hasTimeViolation = violations.stream()
            .anyMatch(v -> v.contains("arrival") || v.contains("Late") || v.contains("Early"));
        assertFalse(hasTimeViolation, "Arrival within window should not flag violation");
    }

    @Test
    void testOnStopReached_arrivalOutsideTimeWindow_flagsViolation() {
        Package pkg = new Package("A", "A", Priority.NORMAL, 2.0, "09:00", "10:00");

        checker.onStopReached(pkg, LocalTime.of(11, 0));

        List<String> violations = checker.getViolations();
        boolean hasTimeViolation = violations.stream()
            .anyMatch(v -> v.contains("Late arrival") || v.contains("arrived"));
        assertTrue(hasTimeViolation, "Late arrival should flag violation");
    }

    // Test 23
    @Test
    void testGetViolations_noActions_returnsEmptyList() {
        assertTrue(checker.getViolations().isEmpty(),
            "A fresh ConstraintChecker should have no violations");
    }

    // Test 24
    @Test
    void testOnStopReached_earlyArrival_flagsViolation() {
        // Window opens at 10:00 but vehicle arrives at 08:00
        Package pkg = new Package("A", "A", Priority.NORMAL, 2.0, "10:00", "12:00");

        checker.onStopReached(pkg, LocalTime.of(8, 0));

        List<String> violations = checker.getViolations();
        assertTrue(violations.stream().anyMatch(v -> v.contains("Early arrival")),
            "Arriving before window open should flag an 'Early arrival' violation");
    }

    // Test 25
    @Test
    void testReset_clearsAllViolations() {
        Package pkg = new Package("A", "A", Priority.NORMAL, 2.0, "09:00", "10:00");
        checker.onStopReached(pkg, LocalTime.of(11, 0)); // late arrival — adds a violation
        assertFalse(checker.getViolations().isEmpty(), "Precondition: should have a violation");

        checker.reset();

        assertTrue(checker.getViolations().isEmpty(),
            "After reset(), violation list must be empty");
    }

    @Test
    void testOnRouteComplete_capacityExceeded_reportsSplit() {
        // This test verifies that the constraint checker collects violations
        // when capacity is exceeded (simulated via manual violation addition)
        Package pkg = new Package("A", "A", Priority.NORMAL, 50.0, "08:00", "12:00");

        checker.onConstraintViolation(pkg, "Vehicle capacity exceeded: 50.0 kg > 30.0 kg limit");

        List<String> violations = checker.getViolations();
        assertTrue(violations.stream().anyMatch(v -> v.contains("capacity")),
            "Should report capacity violation");
    }
}
