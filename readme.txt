Delivery Route Optimizer
========================

CS3342 Software Engineering Group Project

Team: NoU-Turn

Description:
  A Java CLI application that solves vehicle routing optimization problems.
  Given a distance matrix and package information, the system computes optimized
  delivery routes using three algorithms: Nearest Neighbor, Greedy Edge Insertion,
  and 2-Opt Improvement.

Design Patterns:
  - Strategy Pattern: Interchangeable routing algorithms
  - Observer Pattern: Event-driven delivery notifications and constraint checking
  - Template Method Pattern: Pluggable report generation

Requirements:
  - Java 11+
  - Maven 3.6+ (or use included Maven Wrapper)

Quick Start:
  cd Source
  ./mvnw clean package
  cd ..
  java -jar Release/DeliveryRouteOptimizer.jar --algorithm all

Configuration:
  Input files are in the data/ directory:
    - distances.csv: Distance matrix between delivery locations
    - packages.json: Package details with priorities and time windows

  See Docs/UserManual.md for full CLI usage.
  See Docs/InstallationGuide.md for build instructions.
