# Distributed Transport Booking Platform

![Java](https://img.shields.io/badge/Java-17%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![Go](https://img.shields.io/badge/Go-1.21+-00ADD8?style=for-the-badge&logo=go&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![MongoDB](https://img.shields.io/badge/MongoDB-6.0-4EA94B?style=for-the-badge&logo=mongodb&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![React](https://img.shields.io/badge/React-Frontend-20232A?style=for-the-badge&logo=react&logoColor=61DAFB)

A microservices-based transport booking and fleet management system built with **Java (Spring Boot)**, a custom **Go API Gateway**, **PostgreSQL**, and **MongoDB**. Designed to handle user authentication, route search, seat booking, payment processing, and live fleet tracking through isolated, containerized services.

---

## System Architecture

The backend is split into **8 independent services** behind a single Go API Gateway. Rather than sharing one massive database, each core domain manages its own data store to prevent tight coupling and single points of failure.

```mermaid
graph TD
    Client["React Client / Web App"] -->|HTTPS / REST| Gateway["Go API Gateway<br/>(Reverse Proxy, JWT Auth, Rate Limiting)"]

    subgraph Microservices Cluster [Docker Compose Network]
        Gateway -->|/api/auth/*| Auth["Auth & User Service<br/>(Spring Boot)"]
        Gateway -->|/api/bookings/*| Booking["Booking Service<br/>(Spring Boot)"]
        Gateway -->|/api/payments/*| Payment["Payment Service<br/>(Spring Boot)"]
        Gateway -->|/api/fleet/*| Fleet["Fleet & Vehicle Service<br/>(Spring Boot)"]
        Gateway -->|/api/routes/*| Route["Route & Schedule Service<br/>(Spring Boot)"]
        Gateway -->|/api/pricing/*| Pricing["Pricing Service<br/>(Spring Boot)"]
        Gateway -->|/api/analytics/*| Analytics["Telemetry & Logging Service<br/>(Spring Boot)"]

        Booking -->|Sync REST| Pricing
        Booking -->|Sync REST| Payment
        Booking -->|Async Event / Webhook| Notification["Notification Service<br/>(Spring Boot)"]
    end

    subgraph Data Persistence Layer
        Auth --> PG_Users[("PostgreSQL<br/>Users & Roles DB")]
        Booking --> PG_Bookings[("PostgreSQL<br/>Bookings & Transactions DB")]
        Payment --> PG_Bookings
        Fleet --> Mongo_Fleet[("MongoDB<br/>Fleet, Telemetry & Routes")]
        Route --> Mongo_Fleet
        Analytics --> Mongo_Fleet
    end
```

---

## Booking & Payment Flow (Sequence Diagram)

When a user books a trip, the **Go API Gateway** validates their JWT token first before forwarding the request to the **Booking Service**, which coordinates seat availability, fare calculation, payment, and user notifications.

```mermaid
sequenceDiagram
    autonumber
    actor User as Client (React)
    participant GW as Go API Gateway
    participant BS as Booking Service
    participant FS as Fleet/Route Service
    participant PS as Payment Service
    participant NS as Notification Service
    participant DB as PostgreSQL

    User->>GW: POST /api/bookings (JWT + Trip Details)
    GW->>GW: Validate JWT Signature & Expiry
    alt Invalid Token
        GW-->>User: 401 Unauthorized
    else Valid Token
        GW->>BS: Forward Request (with User ID header)
        BS->>FS: Check Seat Availability & Lock Seat
        FS-->>BS: Seat Reserved (Temporary Hold)
        BS->>DB: Create Booking Record (Status: PENDING)
        BS->>PS: Process Payment
        alt Payment Successful
            PS-->>BS: 200 OK (Transaction ID)
            BS->>DB: Update Booking (Status: CONFIRMED)
            BS--)NS: Trigger Booking Confirmation Email/Alert
            BS-->>GW: 201 Created (Booking Confirmation)
            GW-->>User: 201 Created (Ticket Details)
        else Payment Failed
            PS-->>BS: 402 Payment Required
            BS->>FS: Release Held Seat
            BS->>DB: Update Booking (Status: FAILED)
            BS-->>GW: 400 Payment Failed
            GW-->>User: 400 Payment Failed
        end
    end
```

---

## Core Services Overview

| Service | Tech Stack | Responsibility |
| :--- | :--- | :--- |
| **1. API Gateway** | Go (Golang) | Single entry point, reverse-proxy routing, round-robin load balancing, and JWT token verification. |
| **2. Auth & User Service** | Java, Spring Boot, PostgreSQL | User registration, login, password hashing (BCrypt), JWT issuance, and Role-Based Access Control (RBAC). |
| **3. Booking Service** | Java, Spring Boot, PostgreSQL | Manages trip reservations, seat locking, ticket lifecycle (`PENDING`, `CONFIRMED`, `CANCELLED`). |
| **4. Payment Service** | Java, Spring Boot, PostgreSQL | Handles fare checkout validation, transaction records, and refund state management. |
| **5. Fleet Management Service** | Java, Spring Boot, MongoDB | Tracks active vehicles, capacities, maintenance states, and driver assignments. |
| **6. Route & Schedule Service** | Java, Spring Boot, MongoDB | Stores stops, timetables, and calculates route connections across the transport network. |
| **7. Pricing Engine** | Java, Spring Boot | Computes dynamic ticket fares based on distance, vehicle class, and current demand. |
| **8. Notification Service** | Java, Spring Boot | Dispatches booking confirmations, cancellations, and schedule delay alerts to users. |

---

## Key Engineering Highlights

* **Custom Go API Gateway:** Instead of exposing all 8 Spring Boot services directly to the client, the Go gateway handles incoming HTTP traffic concurrently using goroutines, strips unauthorized requests at the edge, and routes valid traffic to internal Docker container hostnames.
* **Polyglot Persistence (PostgreSQL + MongoDB):**
  * **PostgreSQL** is used for **Users, Bookings, and Payments** where ACID transactions and relational integrity are critical (preventing double-booked seats).
  * **MongoDB** is used for **Fleet Telemetry, Routes, and Schedules** where flexible JSON schemas and fast document reads fit nested route/stop structures.
* **Containerized Local Environment:** The entire cluster including the Go gateway, Spring Boot services, PostgreSQL, and MongoDB instances spins up with a single `docker-compose up` command with configured health checks and restart policies.

---

## Getting Started

### Prerequisites
* **Docker** & **Docker Compose** installed on your machine
* *(Optional for local non-Docker builds)*: **Java 17+**, **Maven**, **Go 1.21+**, **Node.js 18+**

### Running the Full System via Docker Compose

1. **Clone the repository:**
   ```bash
   git clone https://github.com/gauravnaudiyal/distributed-transport-platform.git
   cd distributed-transport-platform
   ```

2. **Build and start all containers:**
   ```bash
   docker-compose up --build -d
   ```

3. **Verify running services:**
   ```bash
   docker-compose ps
   ```

4. **Stop and clean up containers:**
   ```bash
   docker-compose down
   ```

---

## Project Structure

```text
distributed-transport-platform/
├── api-gateway/               # Go reverse proxy, JWT validation & routing
├── auth-service/              # Spring Boot - User management & JWT generation
├── booking-service/           # Spring Boot - Core reservation state machine
├── payment-service/           # Spring Boot - Transaction processing
├── fleet-service/             # Spring Boot - Vehicle & driver inventory
├── route-service/             # Spring Boot - Timetables & route lookups
├── pricing-service/           # Spring Boot - Fare calculation rules
├── notification-service/      # Spring Boot - User alerts & confirmations
├── frontend/                  # React client dashboard
└── docker-compose.yml         # Multi-container orchestration config
```

---

## Author

**Gaurav Naudiyal**  
MSc in Computer Science, Trinity College Dublin  
[LinkedIn](https://www.linkedin.com/in/gaurav-naudiyal-277965388/) | [GitHub](https://github.com/gauravnaudiyal) | [Portfolio](https://gauravnaudiyal.github.io/)
