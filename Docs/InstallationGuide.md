# Installation Guide — Delivery Route Optimizer

## Prerequisites

- **Java Development Kit (JDK)**: Version 11 or higher
  - Verify: `java -version`
  - Download: https://adoptium.net/
- **Apache Maven**: Version 3.6 or higher (optional — Maven Wrapper included)
  - Verify: `mvn -version` or use the included `./mvnw` wrapper

## Building from Source

1. Navigate to the Source directory:
   ```bash
   cd Source
   ```

2. Build the project:
   ```bash
   ./mvnw clean package
   ```
   Or if Maven is installed system-wide:
   ```bash
   mvn clean package
   ```

3. The executable JAR will be created at:
   ```
   Release/DeliveryRouteOptimizer.jar
   ```

## Verifying the Installation

Run the application with the sample data:
```bash
java -jar Release/DeliveryRouteOptimizer.jar \
  --matrix data/distances.csv \
  --packages data/packages.json \
  --algorithm all
```

You should see a route optimization report with a comparison table.

## Running Tests

```bash
cd Source
./mvnw test
```

All 19 tests should pass.

## Project Structure

```
DeliveryRouteOptimizer/
  Source/          — Maven project with all source code
  Docs/            — User manual, installation guide, generated reports
  Release/         — Executable JAR
  data/            — Sample input files (CSV, JSON)
  logs/            — Event logs (generated at runtime)
  readme.txt       — Project overview
```
