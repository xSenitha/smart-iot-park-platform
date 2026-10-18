# ADR 0002: MQTT Edge Messaging & Server-Sent Events (SSE) Delivery

## Status
Accepted

## Context
IoT sensors operate in resource-constrained environments (battery powered, solar trickle-charge, cellular/LoRaWAN gateways). Simultaneously, the municipal command dashboard requires real-time incident notifications and telemetry visual updates without aggressive client polling.

## Decision
1. **Edge-to-Cloud Ingestion: Eclipse Mosquitto (MQTT 3.1.1 / 5.0)**:
   - Topic hierarchy: `parks/{parkId}/zones/{zoneId}/devices/{deviceUid}/telemetry`.
   - QoS 1 (At Least Once Delivery) ensures sensor telemetry is guaranteed even across intermittent wireless dropouts.
   - Paho MQTT client in Spring Boot Ingestion Service handles asynchronous event processing.
2. **Cloud-to-Browser Push: Server-Sent Events (SSE)**:
   - Endpoint: `GET /api/v1/events/alerts/stream`.
   - Lightweight, unidirectional HTTP streaming natively supported by web browsers with automatic reconnection and zero WebSocket handshake overhead.

## Consequences
- **Positive**:
  - Extremely low bandwidth overhead for edge sensors compared to HTTP/REST.
  - Near real-time alert dispatch to municipal operators without database polling.
- **Negative**:
  - SSE requires keeping open HTTP connections, managed via Spring `SseEmitter` connection pools.
