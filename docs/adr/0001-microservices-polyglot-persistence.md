# ADR 0001: Microservices Architecture & Polyglot Persistence Strategy

## Status
Accepted

## Context
The Smart IoT Park Platform manages municipal assets across two fundamentally divergent data access patterns:
1. **Relational Core Domain**: Park hierarchy, zones, asset registries, alert rules, user access control (RBAC), and maintenance work orders require ACID transactions, foreign key integrity, and relational joins.
2. **High-Velocity IoT Telemetry**: Telemetry streaming from hundreds of edge devices (soil sensors, compact bins, smart lighting) emits timestamped sensor readings at 5–10 second intervals. These time-series events require high-throughput ingestion, flexible schema for varying device types, and time-window indexing.

## Decision
We adopted a **Polyglot Persistence** pattern dividing responsibilities across service boundaries:
- **MySQL 8.0 (Core API)**: Dedicated to relational domain models, Flyway database schema migrations, and transactional audit trails.
- **MongoDB 7.0 (Ingestion Service)**: Dedicated to high-throughput time-series sensor telemetry and AI prediction event logs. Compound indexes on `{ deviceId: 1, timestamp: -1 }` and `{ zoneId: 1, timestamp: -1 }` enable sub-millisecond retrieval of recent telemetry windows.

## Consequences
- **Positive**:
  - Independent scaling of telemetry ingestion without lock contention on relational tables.
  - Flexibility in telemetry schema as new sensor varieties (e.g., weather stations, air quality) are deployed.
  - Clear service ownership and bounded contexts.
- **Negative**:
  - Requires cross-service eventual consistency handled via asynchronous REST/MQTT sync for heartbeats and battery statuses.
