# Observability Service

GTBS **Audit and Observability** component: consumes asynchronous events, stores an append-only audit trail in PostgreSQL, and exposes read APIs plus Actuator metrics and OpenTelemetry-compatible tracing.

It is **not** on the booking correctness path. If this service or Kafka is slow or down, bookings must still succeed; audit catch-up can lag.

## What it does

- Consumes **booking lifecycle** messages from Kafka (same JSON as `booking-service` outbox).
- Consumes **route/policy** messages from the `route-policy-updated` topic (Terraform name in `the-infra-repo`).
- Persists rows in `audit_records` (Flyway migration `V1__create_audit_records.sql`).
- Exposes `GET /api/v1/audit/bookings/{bookingId}` and `GET /api/v1/audit/segments/{segmentId}`.
- Exposes `/actuator/health`, `/actuator/prometheus`, and OTLP trace export (see `application.yml`).

High-level architecture reference: [`the-infra-repo/docs/architecture.md`](../the-infra-repo/docs/architecture.md) (section 5.7).

---

## Event and topic contract

Authoritative Kafka topic **names** are created in Terraform (`the-infra-repo/infra/terraform/modules/region/main.tf`, `locals.kafka_topics`). This service defaults to the same names:

| Terraform / Event Hubs topic | Consumer | Producer (typical) |
|-------------------------------|----------|--------------------|
| `booking-lifecycle` | This service | `booking-service` (outbox publisher) |
| `route-policy-updated` | This service | Route / traffic / policy services (when implemented) |

Override via env if needed: `KAFKA_TOPIC_BOOKING_LIFECYCLE`, `KAFKA_TOPIC_ROUTE_POLICY_UPDATED`.

### Booking outbox JSON (from `booking-service`)

The booking service writes one JSON object per message (snake_case keys), for example:

- `booking_id` (UUID)
- `user_id`
- `state` — must match a `BookingState` / `AuditEventType` name: `PENDING`, `RESERVED`, `CONFIRMED`, `REJECTED`, `CANCELLED`, `FAILED`
- `origin`, `destination`
- `map_version` (may be empty string)
- `time_window_start`, `time_window_end` (ISO-8601 strings)

`region_id`, `segment_id`, and `correlation_id` are **not** in the outbox payload today. This service sets:

- `regionId` → **`APP_AUDIT_DEFAULT_REGION_ID`** (required in production; default `local-dev` for laptop dev).
- `sourceService` → **`APP_AUDIT_DEFAULT_BOOKING_SOURCE_SERVICE`** (default `booking-service`).
- `eventTimestamp` → consume time (`Instant.now()`).

Malformed JSON or unknown `state` values are **skipped** (logged); the consumer does not throw, so one bad message does not block the partition.

### Policy / route events (`route-policy-updated`)

Payload maps to `PolicyChangeEvent` (camelCase JSON):

- `eventType` — must be a value of `AuditEventType` (e.g. `POLICY_CHANGED`, `SEGMENT_CLOSED`, `SEGMENT_REOPENED`)
- `segmentId`, `regionId`, `sourceService`, `correlationId`, `mapVersion`, `eventTimestamp` (ISO-8601 instant)

`regionId` and `sourceService` are **required**; unknown `eventType` is skipped (logged).

---

## Configuration

### Core environment variables

| Variable | Purpose |
|----------|---------|
| `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` | PostgreSQL (use in `prod` / Azure; local defaults use `DB_*` in `application.yml`) |
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` or `KAFKA_BOOTSTRAP_SERVERS` | Kafka or Event Hubs bootstrap |
| `APP_AUDIT_DEFAULT_REGION_ID` | Regional deployments must set this (e.g. Terraform `region_key`) |
| `APP_AUDIT_DEFAULT_BOOKING_SOURCE_SERVICE` | Shown on audit rows from booking feed (default `booking-service`) |
| `OTEL_EXPORTER_OTLP_ENDPOINT` | OTLP trace endpoint (optional) |

### Spring profiles

- **Local:** default `application.yml` only; Postgres and Kafka from Docker Compose.
- **Kubernetes / Azure:** `SPRING_PROFILES_ACTIVE=prod,azure` as in `k8s/deployment.yaml`.  
  **`application-azure.yml`** documents Event Hubs SASL — set `SPRING_KAFKA_PROPERTIES_*` via env or secrets; never commit connection strings.

### Database naming in production

Terraform provisions a Postgres database named **`audit`** per region. Point `SPRING_DATASOURCE_URL` at that database (see `k8s/configmap.yaml` placeholder). Local Compose still uses database name `observability` by default.

---

## Run locally

Prerequisites: Java 21+, Maven 3.9+, Docker.

`docker-compose.yml` runs **Apache Kafka in KRaft mode** with the official **`apache/kafka`** image (Bitnami tags are often unavailable on Docker Hub). Your app on the host still uses **`localhost:9092`**.

```bash
docker compose up -d
mvn spring-boot:run
```

Useful URLs:

- Health: `http://localhost:8080/actuator/health`
- Metrics: `http://localhost:8080/actuator/prometheus`
- Audit by booking: `http://localhost:8080/api/v1/audit/bookings/{bookingId}`
- Kafka UI: `http://localhost:8081`

To manually emit a booking message (after `booking-service` uses topic `booking-lifecycle`), publish JSON matching the contract to topic `booking-lifecycle` and confirm rows in `audit_records`.

---

## Build, test, container

```bash
mvn test
mvn -DskipTests package
docker build -t observability-service:local .
```

---

## CI: GitHub Container Registry

| Workflow | When | Image tags |
|----------|------|------------|
| `.github/workflows/publish-and-deploy.yml` | Push to `main` or `development` | `ghcr.io/<owner>/<repo>:latest` + optional AKS `kubectl set image` (uses `deploy.env`) |
| `.github/workflows/docker-pr.yml` | Pull request to `main` or `development` | `pr-<number>` and `pr-<number>-<full-sha>` (no AKS deploy) |

**Trying a PR build in staging**

```bash
kubectl set image deployment/observability-service \
  observability-service=ghcr.io/<org>/observability-service:pr-42 \
  -n <your-namespace>
```

Replace tag with the exact tag shown in the PR workflow run (“Packages” / job output).

---

## Kubernetes manifests

Paths mirror `journey-compatibility-service`:

- `k8s/deployment.yaml` — Deployment + ClusterIP Service (`observability-service`), Workload Identity + CSI placeholders.
- `k8s/configmap.yaml` — Non-secret URLs and `APP_AUDIT_DEFAULT_REGION_ID` (replace `REPLACE_*` values).
- `k8s/keyvault/` — `SecretProviderClass` + `ServiceAccount` placeholders for DB credentials sync.
- `k8s/local/` — Minimal local/minikube-style example.

Ensure `deploy.env` (`DEPLOYMENT_NAME`, `CONTAINER_NAME`, `NAMESPACE`) matches the Kubernetes Deployment if you use the publish workflow’s deploy job.

---

## Security

Treat audit APIs as **internal**. Do not expose them on a public API gateway without authentication and authorization. Metrics endpoints should be scraped only from the mesh / monitoring network.

---

## Project layout

```text
observability-service/
├── .github/workflows/
├── k8s/
├── docker-compose.yml
├── Dockerfile
├── deploy.env
├── src/main/java/com/gtbs/observability/
│   ├── audit/          # REST, domain, repository, service
│   └── ingest/       # Kafka consumers, payloads, booking outbox mapper
└── src/main/resources/
    ├── application.yml
    ├── application-prod.yml
    ├── application-azure.yml
    └── db/migration/
```

## Further improvements

- Region-scoped query API and pagination.
- Tamper-evident or signed audit export for compliance coursework.
- Integration tests with Testcontainers (Postgres + Kafka) alongside current H2 + `@EmbeddedKafka` smoke test.
