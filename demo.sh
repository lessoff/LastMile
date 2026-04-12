#!/bin/bash

JAR="Release/DeliveryRouteOptimizer.jar"
MATRIX="data/distances.csv"
PACKAGES="data/packages.json"

pause() {
    echo ""
    read -p "Press ENTER to continue to next demo..." _
    echo ""
}

clear
echo "=========================================================="
echo "       DELIVERY ROUTE OPTIMIZER — LIVE DEMO"
echo "=========================================================="
echo ""
echo "  5 Stops: A (123 Main St)    HIGH priority   5.0 kg"
echo "           B (456 Oak Ave)    NORMAL priority  8.0 kg"
echo "           C (789 Pine Rd)    LOW priority     6.0 kg"
echo "           D (321 Elm Blvd)   HIGH priority    4.0 kg"
echo "           E (654 Maple Dr)   NORMAL priority  7.0 kg"
echo ""
echo "  Total weight: 30.0 kg | Depot: starting point"
echo "=========================================================="

pause

# ── DEMO 1 ──────────────────────────────────────────────────
echo "=========================================================="
echo "  DEMO 1: Nearest Neighbor Algorithm"
echo "  Greedy approach — always go to the closest unvisited stop"
echo "=========================================================="
echo ""
java -jar "$JAR" --matrix "$MATRIX" --packages "$PACKAGES" --algorithm nn

pause

# ── DEMO 2 ──────────────────────────────────────────────────
echo "=========================================================="
echo "  DEMO 2: Greedy Edge Insertion Algorithm"
echo "  Builds route by picking cheapest edges globally"
echo "=========================================================="
echo ""
java -jar "$JAR" --matrix "$MATRIX" --packages "$PACKAGES" --algorithm greedy

pause

# ── DEMO 3 ──────────────────────────────────────────────────
echo "=========================================================="
echo "  DEMO 3: 2-Opt Improvement Algorithm"
echo "  Starts with Nearest Neighbor, then removes route crossings"
echo "=========================================================="
echo ""
java -jar "$JAR" --matrix "$MATRIX" --packages "$PACKAGES" --algorithm twoopt

pause

# ── DEMO 4 ──────────────────────────────────────────────────
echo "=========================================================="
echo "  DEMO 4: All Algorithms Side-by-Side Comparison"
echo "  See which algorithm wins on this dataset"
echo "=========================================================="
echo ""
java -jar "$JAR" --matrix "$MATRIX" --packages "$PACKAGES" --algorithm all

pause

# ── DEMO 5 ──────────────────────────────────────────────────
echo "=========================================================="
echo "  DEMO 5: Vehicle Capacity Constraint (15 kg limit)"
echo "  System splits deliveries into multiple trips"
echo "  HIGH priority packages are loaded first"
echo "=========================================================="
echo ""
java -jar "$JAR" --matrix "$MATRIX" --packages "$PACKAGES" --capacity 15

pause

# ── DEMO 6 ──────────────────────────────────────────────────
echo "=========================================================="
echo "  DEMO 6: Automated Test Suite — 49 Tests, 8 Files"
echo "  Covers all algorithms, constraints, IO, and data models"
echo "=========================================================="
echo ""
cd Source && ./mvnw test 2>&1 | grep -E "Tests run|BUILD|Running"
cd ..

echo ""
echo "=========================================================="
echo "  DEMO COMPLETE"
echo "  Full report saved to: Docs/last_report.txt"
echo "  Event log saved to:   logs/events.log"
echo "=========================================================="
