# User Manual — Delivery Route Optimizer

## Overview
The Delivery Route Optimizer is a CLI application that computes optimized delivery
routes given a distance matrix and package information. It supports three routing
algorithms and handles real-world constraints like vehicle capacity, delivery
priorities, and time windows.

## Running the Application

```bash
java -jar Release/DeliveryRouteOptimizer.jar [options]
```

## CLI Options

| Flag | Description | Default |
|------|-------------|---------|
| `--matrix <path>` | Path to distance matrix CSV file | `data/distances.csv` |
| `--packages <path>` | Path to packages JSON file | `data/packages.json` |
| `--algorithm <type>` | Algorithm to use: `nn`, `greedy`, `twoopt`, `all` | `all` |
| `--capacity <kg>` | Vehicle capacity in kg (triggers multi-trip splitting) | unlimited |
| `--output <path>` | Path to save the report file | `Docs/last_report.txt` |

## Algorithms

### Nearest Neighbor (`nn`)
A greedy heuristic that starts at the depot and always visits the nearest unvisited
stop. Fast (O(n²)) but may produce suboptimal routes.

### Greedy Edge Insertion (`greedy`)
Sorts all edges by distance and builds a route by greedily adding the shortest
edges that don't violate Hamiltonian circuit constraints. Often produces different
results than Nearest Neighbor.

### 2-Opt Improvement (`twoopt`)
Takes the Nearest Neighbor solution and iteratively improves it by reversing
sub-segments of the route. Continues until no further improvement is found.
Typically produces the best results.

### Compare All (`all`)
Runs all three algorithms and displays a comparison table showing total distance
and estimated travel time for each.

## Input File Formats

### Distance Matrix (CSV)
```
,Depot,A,B,C,D
Depot,0,10,20,15,25
A,10,0,8,12,18
B,20,8,0,6,14
C,15,12,6,0,9
D,25,18,14,9,0
```

### Packages (JSON)
```json
[
  {
    "id": "A",
    "label": "123 Main St",
    "priority": "HIGH",
    "weightKg": 5.0,
    "timeWindowOpen": "09:00",
    "timeWindowClose": "12:00"
  }
]
```

- `id`: Must match a node name in the distance matrix
- `priority`: HIGH, NORMAL, or LOW
- `timeWindowOpen`/`timeWindowClose`: HH:mm format

## Constraints

- **Priority**: HIGH priority packages should be delivered before NORMAL/LOW. Violations are reported.
- **Capacity**: Use `--capacity` to set max vehicle weight. Packages are split into multiple trips.
- **Time Windows**: Arrival times are estimated at 40 km/h from an 08:00 departure. Violations are flagged.

## Output

The application produces:
1. Console output with delivery events and the full report
2. A report file (default: `Docs/last_report.txt`)
3. An event log (`logs/events.log`)

## Examples

```bash
# Run all algorithms with comparison
java -jar Release/DeliveryRouteOptimizer.jar \
  --matrix data/distances.csv \
  --packages data/packages.json \
  --algorithm all

# Run with capacity constraint
java -jar Release/DeliveryRouteOptimizer.jar \
  --matrix data/distances.csv \
  --packages data/packages.json \
  --algorithm twoopt \
  --capacity 100

# Save report to custom location
java -jar Release/DeliveryRouteOptimizer.jar \
  --matrix data/distances.csv \
  --packages data/packages.json \
  --algorithm all \
  --output reports/run1.txt
```
