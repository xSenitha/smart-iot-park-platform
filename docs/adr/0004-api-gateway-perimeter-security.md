# ADR 0004: API Gateway Perimeter Routing, JWT Validation & Rate Limiting

## Status
Accepted

## Context
Exposing multiple heterogeneous backend microservices (Spring Boot Core API, Reactive Ingestion, FastAPI AI Engine) directly to the public internet creates security risks, complex client routing, and vulnerability to DDoS or brute-force traffic.

## Decision
We implemented a perimeter **API Gateway using Spring Cloud Gateway (Netty Reactive)**:
1. **Single Entry Point**: All client requests route through port `8080`.
2. **Edge JWT Authentication**: A custom `JwtAuthGatewayFilterFactory` validates HMAC-SHA256 signatures, extracts claims (`sub`, `role`), and propagates authenticated identity headers (`X-User-Username`, `X-User-Role`) to internal services.
3. **Token Bucket Rate Limiting**: A custom `RateLimitGatewayFilterFactory` prevents endpoint starvation by enforcing request burst and refill limits per client IP.
4. **Unified CORS Policy**: Centralized CORS preflight handling for web dashboard clients.

## Consequences
- **Positive**:
  - Downstream services focus strictly on business logic and trust pre-validated headers.
  - Zero external port exposure required for internal database engines or microservices.
- **Negative**:
  - The gateway represents a single point of entry requiring multi-instance redundancy in production.
