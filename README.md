# Delivery Route Optimizer

A Java CLI application that solves vehicle routing optimization problems. Given a distance matrix and package data, it computes optimized delivery routes and reports constraint violations.

---

## Features

- **3 routing algorithms** — Nearest Neighbor, Greedy Edge Insertion, 2-Opt Improvement
- **Side-by-side comparison** — run all three and see which performs best
- **Constraint checking** — priority ordering, time windows, vehicle capacity
- **Multi-trip support** — automatically splits packages across trips when capacity is exceeded
- **Structured reports** — saved to `Docs/last_report.txt` and `logs/events.log` after every run

---

## Quick Start

```bash
# 1. Build
cd Source && ./mvnw clean package && cd ..

# 2. Run
java -jar Release/DeliveryRouteOptimizer.jar \
  --matrix data/distances.csv \
  --packages data/packages.json \
  --algorithm all
```

---

## CLI Options

| Flag | Description | Default |
|------|-------------|---------|
| `--matrix <path>` | Distance matrix CSV file | `data/distances.csv` |
| `--packages <path>` | Package details JSON file | `data/packages.json` |
| `--algorithm <type>` | `nn` / `greedy` / `twoopt` / `all` | `all` |
| `--capacity <kg>` | Vehicle weight limit (splits into multiple trips) | unlimited |
| `--output <path>` | Custom report output path | `Docs/last_report.txt` |

---

## Example Output

```
--- Algorithm Comparison ---
  Nearest Neighbor          121.00 km    182 min
  Greedy Edge Insertion     111.00 km    167 min
  2-Opt Improvement         107.00 km    161 min

--- Constraint Violations ---
  * Late arrival at C: arrived at 10:10 but window closes at 09:30
  * HIGH priority package D delivered after NORMAL/LOW priority packages
```

---

## Design Patterns

| Pattern | Implementation |
|---------|---------------|
| **Strategy** | `RoutingStrategy` interface → 3 interchangeable algorithm classes |
| **Observer** | `DeliveryObserver` interface → `ConsoleLogger`, `FileLogger`, `ConstraintChecker` |
| **Template Method** | `ReportGenerator` abstract class → `ConsoleReportGenerator`, `FileReportGenerator` |

---

## Project Structure

```
├── Source/          — Maven project (src/, pom.xml)
├── Release/         — Executable JAR
├── data/            — Sample input files (CSV, JSON)
├── Docs/            — User manual, installation guide, generated reports
└── logs/            — Event log (generated at runtime)
```

---

## Requirements

- Java 11+
- Maven 3.6+ *(or use the included `./mvnw` wrapper — no install needed)*

---

## Running Tests

```bash
cd Source && ./mvnw test
```

```
Tests run: 19, Failures: 0, Errors: 0, Skipped: 0
```

---

## Documentation

- [`Docs/UserManual.md`](Docs/UserManual.md) — full CLI usage and input format reference
- [`Docs/InstallationGuide.md`](Docs/InstallationGuide.md) — build instructions
