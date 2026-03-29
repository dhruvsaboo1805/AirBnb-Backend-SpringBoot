# 🏠 Airbnb Booking System — Backend

A production-grade Airbnb-style booking backend built with **Spring Boot**, featuring microservice architecture, CQRS pattern, distributed locking, saga pattern, observability, and CI/CD pipeline.

---

## 🏗️ Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                        Client (Postman)                      │
└──────────────────────────┬──────────────────────────────────┘
                           │
              ┌────────────▼────────────┐
              │    Auth Microservice     │  ← Docker
              │  Spring Boot + MySQL     │
              │  JWT + 2FA (Google Auth) │
              └────────────┬────────────┘
                           │ JWT Token
              ┌────────────▼────────────┐
              │   Airbnb Booking Service │  ← Local (port 3000)
              │     Spring Boot          │
              │  MySQL + Redis + Saga    │
              └────────────┬────────────┘
                           │
         ┌─────────────────┼─────────────────┐
         │                 │                  │
    ┌────▼────┐      ┌─────▼─────┐    ┌──────▼──────┐
    │  MySQL  │      │   Redis   │    │  Prometheus  │
    │  (DB)   │      │ (Cache +  │    │  + Grafana   │
    │         │      │  Queue)   │    │ (Monitoring) │
    └─────────┘      └───────────┘    └─────────────┘
```

---

## ✨ Features

### 🔐 Authentication (Auth Microservice)
- User registration with role selection (`USER` / `ADMIN`)
- Google Authenticator based **2FA (Two Factor Authentication)**
- **JWT token** generation after OTP verification
- `/validate` endpoint for inter-service token validation
- Dockerized with MySQL

### 🏠 Listing Management
- ADMIN can create, update, delete listings
- Owner auto-provisioned from JWT email on first listing
- Automatic **availability seeding** for 365 days on listing creation
- Role-based access control via Spring Security

### 📅 Booking System
- **Idempotency** — duplicate booking requests safely handled via idempotency keys
- **Distributed locking** via Redis TTL locks — prevents concurrent double bookings
- **CQRS Pattern** — writes go to MySQL, reads served from Redis cache
- **Saga Pattern** — booking state machine via Redis queue
  - `PENDING → CONFIRMED → availability locked`
  - `PENDING → CANCELLED → availability released`

### 🔄 Concurrency Control
- Redis TTL-based distributed lock per `airbnbId + dateRange`
- Availability slot pre-seeding for `O(1)` conflict detection
- `countByAirbnbIdAndDateBetweenAndBookingIdIsNotNull` — instant availability check

### 📊 Observability (Prometheus + Grafana)
- Spring Boot Actuator + Micrometer integration
- Custom business metrics:
  - `booking_created_total`
  - `booking_confirmed_total`
  - `booking_cancelled_total`
  - `booking_failed_total`
  - `booking_double_booking_total`
  - `booking_pending_current` (Gauge)
  - `booking_creation_duration` (Timer)
- JVM, HTTP, DB connection pool metrics out of the box
- Grafana dashboards for business + infrastructure monitoring

### 🚀 CI/CD
- GitHub Actions pipeline on every push to `master`
- Spins up MySQL + Redis services in pipeline
- Builds + tests automatically
- Uploads test reports as artifacts

---

## 🛠️ Tech Stack

| Layer | Technology |
|-------|-----------|
| Framework | Spring Boot 3.x |
| Language | Java 21 |
| Database | MySQL 8.0 |
| Cache / Queue | Redis 7 |
| Auth | JWT + Google Authenticator (2FA) |
| Monitoring | Prometheus + Grafana + Micrometer |
| Build | Gradle |
| CI/CD | GitHub Actions |
| Containerization | Docker + Docker Compose |

---

## 📁 Project Structure

```
src/main/java/com/example/AirbnbBookingSpring/
├── controllers/
│   ├── AirbnbController.java
│   └── BookingController.java
├── services/
│   ├── AirbnbService.java
│   ├── BookingService.java
│   └── IdempotencyService.java
├── services/concurrency/
│   ├── ConcurrencyControlStrategy.java    ← interface
│   └── RedisLockStrategy.java             ← Redis TTL lock impl
├── saga/
│   ├── SagaEventPublisher.java
│   ├── SagaEventConsumer.java
│   └── SagaEventProcessor.java
├── services/handlers/
│   ├── BookingEventHandler.java
│   └── AvailabilityEventHandler.java
├── security/
│   ├── AuthServiceClient.java             ← calls auth microservice
│   ├── JwtAuthFilter.java                 ← intercepts all requests
│   └── SecurityConfig.java               ← role-based access control
├── metrics/
│   └── BookingMetrics.java               ← custom Prometheus metrics
├── models/
│   ├── Airbnb.java
│   ├── Booking.java
│   ├── Availability.java
│   └── readModels/BookingReadModel.java
├── repositories/
│   ├── writes/
│   └── reads/
├── dtos/
├── mappers/
├── exceptions/
│   ├── GlobalExceptionHandler.java
│   └── BookingException.java
└── configurations/
    └── RestTemplateConfig.java
```

---

## 🚦 API Endpoints

### Auth Service (port 8080)
```
POST /api/v1/auth/register       → register user (role: USER/ADMIN)
POST /api/v1/auth/login          → login → returns OTP_REQUIRED
POST /api/v1/auth/verify-otp     → verify OTP → returns JWT
POST /api/v1/2fa/enable          → enable 2FA, returns QR code
GET  /api/v1/auth/validate       → validate JWT token
```

### Airbnb Service (port 3000)
```
POST   /api/airbnb               → create listing (ADMIN only)
GET    /api/airbnb               → get all listings
GET    /api/airbnb/{id}          → get listing by id
PUT    /api/airbnb/{id}          → update listing (ADMIN only)
DELETE /api/airbnb/{id}          → delete listing (ADMIN only)

POST   /api/bookings             → create booking (USER only)
PATCH  /api/bookings             → confirm/cancel booking (USER only)

GET    /actuator/prometheus      → Prometheus metrics (public)
GET    /actuator/health          → health check (public)
```

---

## 🔄 Booking Flow

```
1. USER hits POST /api/bookings
2. JwtAuthFilter → calls auth service /validate → extracts email
3. Redis lock acquired for airbnbId + dateRange
4. Availability checked → if booked → 422 error
5. Booking saved to MySQL (PENDING)
6. Availability slots marked with bookingId
7. Booking written to Redis (CQRS read model)
8. idempotencyKey returned to client

9. USER hits PATCH /api/bookings with idempotencyKey + CONFIRMED
10. Redis read model fetched (CQRS)
11. Status updated to CONFIRMED in MySQL + Redis
12. Saga event published → BOOKING_CONFIRM_REQUESTED
13. SagaEventConsumer processes → BOOKING_CONFIRMED
14. Availability slots finalized
```

---

## ⚙️ Setup & Running

### Prerequisites
```
Java 21
Docker + Docker Compose
Gradle
Redis (local or Docker)
MySQL (local)
```

### 1. Clone the repository
```bash
git clone https://github.com/dhruvsaboo1805/AirBnb-Backend-SpringBoot.git
cd AirBnb-Backend-SpringBoot
```

### 2. Set up environment variables
Create `application.properties` or set env variables:
```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/airbnbbackend
spring.datasource.username=root
spring.datasource.password=yourpassword

# Redis
spring.data.redis.host=localhost
spring.data.redis.port=6379

# Auth Service
AUTH_SERVICE_URL=http://localhost:8080

# Saga
SAGA_QUEUE_VALUE=saga_queue
LOCK_KEY_PREFIX=lock:
LOCK_TIME_OUT_DURATION=PT5M
```

### 3. Start Auth Microservice (Docker)
```bash
cd /path/to/auth-microservice
docker-compose up -d
```

### 4. Start Airbnb Service
```bash
./gradlew bootRun
```

### 5. Start Monitoring Stack
```bash
docker-compose -f docker-compose-monitoring.yml up -d
```

---

## 📊 Monitoring

| Service | URL |
|---------|-----|
| Airbnb App | http://localhost:3000 |
| Prometheus | http://localhost:9090 |
| Grafana | http://localhost:3001 |
| Metrics | http://localhost:3000/actuator/prometheus |

**Grafana Login:** `admin / admin`

**Import Dashboard:** ID `19004` (Spring Boot 3.x)

### Key PromQL Queries
```
# Bookings per minute
rate(booking_created_total[1m])

# Booking failure rate
rate(booking_failed_total[5m]) / rate(booking_created_total[5m]) * 100

# Average API response time
rate(http_server_requests_seconds_sum[5m]) / rate(http_server_requests_seconds_count[5m])

# Active DB connections
hikaricp_connections_active
```

---

## 🧪 Testing

```bash
# Run tests
./gradlew test

# Build without tests
./gradlew build -x test
```

---

## 🔁 CI/CD Pipeline

GitHub Actions runs on every push to `master`:

```
Push to master
    ↓
Spin up MySQL + Redis in pipeline
    ↓
Set up JDK 21
    ↓
Cache Gradle dependencies
    ↓
Build project
    ↓
Run tests
    ↓
Upload test reports
```

---

## 🏛️ Design Patterns Used

| Pattern | Where Used | Why |
|---------|-----------|-----|
| CQRS | Booking reads from Redis, writes to MySQL | Separate read/write concerns, faster reads |
| Saga | Booking state transitions via Redis queue | Distributed transaction management |
| Repository Pattern | Write/Read repositories | Separation of concerns |
| Strategy Pattern | ConcurrencyControlStrategy | Swap locking strategy without changing business logic |
| Builder Pattern | All models via Lombok @SuperBuilder | Clean object construction |
| Idempotency | Booking creation | Prevent duplicate bookings |

---

## 🔐 Security

- All endpoints protected by JWT validation via Auth Microservice
- Role-based access: `ADMIN` for listings, `USER` for bookings
- No JWT logic duplicated in Airbnb service — delegated to Auth service
- Customer identity derived from JWT — never trusted from request body

---

## 📝 Author

**Dhruv Saboo**
- GitHub: [@dhruvsaboo1805](https://github.com/dhruvsaboo1805)
