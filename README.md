# Razorpay Clone — Distributed Payment Platform

A distributed payment platform built using Java, Spring Boot microservices, PostgreSQL, Redis, Apache Kafka, and Kubernetes. The project simulates the payment lifecycle, including order creation, payment processing, settlement, and webhook delivery.

## Architecture Overview

The project consists of seven microservices:

| Microservice | Responsibility |
|---|---|
| API Gateway | API key authentication, rate limiting, and request routing |
| Payment Service | Order and payment processing, payment state machine, and bank callback simulation |
| Merchant Service | Merchant accounts, API keys, customers, and webhook management |
| Operations Service | Settlement processing, webhook delivery, and outbox relay |
| Vault Service | Card tokenization and encryption |
| Config Service | Centralized configuration using Spring Cloud Config |
| Discovery Service | Service registration and discovery |

### Supporting Infrastructure

- **PostgreSQL:** Persistent data storage
- **Redis:** Idempotency, rate limiting, caching, and distributed locks
- **Apache Kafka:** Asynchronous event-driven communication
- **Docker and Kubernetes:** Containerization and orchestration
- **Prometheus and Grafana:** Monitoring and metrics visualization
- **Zipkin:** Distributed tracing
- **Apache JMeter:** API load and performance testing

## Design Patterns and Key Concepts

- **Idempotency:** Prevents duplicate operations when requests are retried using `X-Idempotency-Key`.
- **Transactional Outbox:** Helps reliably publish database changes as Kafka events.
- **Distributed Locking:** Uses ShedLock to prevent multiple replicas from processing the same scheduled task concurrently.
- **Rate Limiting:** Uses Redis to control API request rates per API key.
- **Circuit Breaker and Retry:** Uses Resilience4j to handle downstream service failures.
- **Event-Driven Architecture:** Uses Kafka for asynchronous communication between microservices.
- **Stateless Services:** Supports horizontal scaling by keeping application state in external systems.

## Optimizations and Reliability

- Implemented idempotent request handling to reduce duplicate payment operations.
- Used transactional outbox processing for reliable event publishing.
- Added distributed locking for scheduled jobs.
- Implemented Redis-backed rate limiting.
- Configured circuit breakers and retry mechanisms for downstream calls.
- Integrated monitoring and distributed tracing for troubleshooting.

## Load Testing

Used Apache JMeter to test API behavior under concurrent workloads and analyze throughput, response times, error rates, and resource utilization.

## Kubernetes Deployment

Deployed and managed containerized services using Kubernetes manifests, including Deployments, StatefulSets, Services, ConfigMaps, and Secrets.

The architecture supports Kubernetes-based deployment and can be adapted for Google Kubernetes Engine (GKE) with the appropriate cloud configuration.

## Technology Stack

- **Language:** Java
- **Backend:** Spring Boot, Spring Security, Spring Data JPA, Hibernate, Spring Cloud
- **Database:** PostgreSQL
- **Messaging:** Apache Kafka
- **Caching:** Redis
- **Resilience:** Resilience4j, ShedLock
- **DevOps:** Docker, Kubernetes
- **Monitoring:** Prometheus, Grafana, Zipkin
- **Testing:** JUnit, Postman, Apache JMeter
- **Build Tool:** Maven

