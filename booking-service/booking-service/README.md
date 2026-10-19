# Booking Service

Booking Service for the Distributed Traffic System — Group B. Built with Spring Boot, this service handles trip bookings, cancellations, and verification proofs within a distributed architecture.

## 🚀 Technology Stack

- **Framework**: [Spring Boot 3.4.3](https://spring.io/projects/spring-boot) (Java 17)
- **Database**: [PostgreSQL 16](https://www.postgresql.org/) (Main persistence)
- **Caching**: [Redis 7](https://redis.io/) (Idempotency & state)
- **Messaging**: [Apache Kafka](https://kafka.apache.org/) (Event-driven communication)
- **Infrastructure**: [Terraform](https://www.terraform.io/) & [Docker](https://www.docker.com/)
- **Cloud**: [Azure](https://azure.microsoft.com/) (Container Instances, ACR, Flexible Server)

---

## 🛠️ Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+
- Docker & Docker Compose
- Azure CLI & Terraform (for deployment)

### Local Development

1. **Spin up dependencies**:
   ```bash
   docker-compose up -d
   ```
   This will start PostgreSQL (`5432`), Redis (`6379`), and Kafka (`9092`).

2. **Run the application**:
   ```bash
   mvn spring-boot:run
   ```
   The service will be available at `http://localhost:8081`.

---

## 🛰️ API Reference

### 1. Create a Booking
Creates a new trip booking. Supports idempotency via `idempotency_key`.

- **URL**: `POST /api/bookings`
- **Body**:
  ```json
  {
    "user_id": "user-123",
    "origin": "Dublin-6",
    "destination": "Dublin-2",
    "time_window_start": "2025-04-01T08:00:00Z",
    "time_window_end": "2025-04-01T10:00:00Z",
    "idempotency_key": "unique-request-id-001"
  }
  ```
- **Response** (`201 Created` or `200 OK`):
  ```json
  {
    "booking_id": "uuid-string",
    "user_id": "user-123",
    "state": "CONFIRMED",
    "map_version": "v1.2",
    "coordinator_region": "EU-WEST",
    "created_at": "2025-03-31T18:00:00Z"
  }
  ```

### 2. Get Booking Details
Retrieves current status and metadata of a specific booking.

- **URL**: `GET /api/bookings/{bookingId}`
- **Response**: Same as Booking Response above.

### 3. Cancel Booking
Cancels an existing booking if it is in a cancellable state.

- **URL**: `POST /api/bookings/{bookingId}/cancel`
- **Response**: Updated booking details with state `CANCELLED`.

### 4. Verify Booking (Proof)
Generates a verification proof for the trip, including a zero-knowledge or distributed proof token.

- **URL**: `GET /api/bookings/{bookingId}/verify`
- **Response**:
  ```json
  {
    "booking_id": "uuid-string",
    "proof_token": "jwt-or-hash-token",
    "map_version": "v1.2",
    "time_window_start": "2025-04-01T08:00:00Z",
    "time_window_end": "2025-04-01T10:00:00Z",
    "segment_scope": ["seg-001", "seg-002"]
  }
  ```

---

## ☁️ Deployment

The project includes a `deploy.sh` script to automate Azure deployment using Terraform.

1. **Configure Azure credentials**:
   ```bash
   az login
   ```

2. **Run the deployment script**:
   ```bash
   ./deploy.sh
   ```
   The script builds the Docker image, provisions Azure infrastructure (ACR, PostgreSQL, ACI), and pushes the image.

---

## 📁 Project Structure

- `src/main/java`: Core application logic.
  - `controller`: REST API endpoints.
  - `service`: Business logic & transaction handling.
  - `model`: JPA entities and DTOs.
- `terraform/`: Infrastructure as Code (Azure resource definitions).
- `docker-compose.yml`: Local infrastructure setup.
- `deploy.sh`: Continuous Deployment script.
