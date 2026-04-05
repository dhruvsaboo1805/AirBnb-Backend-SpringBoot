# 🏠 Airbnb Booking System — Backend

A production-grade Airbnb-style booking backend built with **Spring Boot**, featuring microservice architecture, CQRS pattern, distributed locking, saga pattern, rate limiting, observability, and CI/CD pipeline.

---

## 🏗️ Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                        Client (Postman / Frontend)           │
└──────────────────────────┬──────────────────────────────────┘
                           │
              ┌────────────▼────────────┐
              │    Auth Microservice     │  ← Docker (port 8080)
              │  Spring Boot + MySQL     │
              │  JWT + 2FA (Google Auth) │
              └────────────┬────────────┘
                           │ JWT Token
              ┌────────────▼────────────┐
              │   Airbnb Booking Service │  ← Local (port 3000)
              │     Spring Boot          │
              │  MySQL + Redis + Saga    │
              │  Rate Limiting + Metrics │
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
- Owner auto-provisioned from JWT email on first listing creation
- Automatic **availability seeding** for 365 days on listing creation
- Role-based access control via Spring Security

### 📅 Booking System
- **Idempotency** — duplicate booking requests safely handled via UUID idempotency keys
- **Distributed locking** via Redis TTL locks — prevents concurrent double bookings
- **CQRS Pattern** — writes go to MySQL, reads served from Redis cache
- **Saga Pattern** — booking state machine via Redis queue
  - `PENDING → CONFIRMED → availability locked`
  - `PENDING → CANCELLED → availability released`

### 🔒 Rate Limiting (Token Bucket Algorithm)
- Implemented using **Bucket4j** library
- Different limits per endpoint type:

| Endpoint | Limit | Reason |
|----------|-------|--------|
| `POST /api/bookings` | 5 req/min | Expensive — DB + Redis + Saga |
| `POST /api/airbnb` | 10 req/min | Moderate — DB + seed 365 slots |
| Everything else | 30 req/min | Cheap reads |

- Per-user buckets — each user gets their own token bucket
- ADMIN role exempt from rate limiting
- Standard response headers:
  ```
  X-Rate-Limit-Limit: 30
  X-Rate-Limit-Remaining: 24
  X-Rate-Limit-Retry-After-Seconds: 47  ← only on 429
  ```
- Rate limit blocks tracked in Prometheus metrics

### 🔄 Concurrency Control
- Redis TTL-based distributed lock per `airbnbId + dateRange`
- Availability slot pre-seeding for `O(1)` conflict detection
- `countByAirbnbIdAndDateBetweenAndBookingIdIsNotNull` — instant availability check

### 📊 Observability (Prometheus + Grafana)
- Spring Boot Actuator + Micrometer integration
- Custom business metrics:
  - `booking_created_total` — total bookings created
  - `booking_confirmed_total` — total bookings confirmed
  - `booking_cancelled_total` — total bookings cancelled
  - `booking_failed_total` — total failed booking attempts
  - `booking_double_booking_total` — double booking attempts blocked
  - `booking_pending_current` — current pending bookings (Gauge)
  - `booking_creation_duration` — booking creation time (Timer)
  - `rate_limit_blocked_total` — requests blocked by rate limiter
- JVM, HTTP, DB connection pool metrics out of the box
- Alert rules for high failure rate, slow API, high JVM heap
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
| Rate Limiting | Bucket4j (Token Bucket Algorithm) |
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
├── ratelimiter/
│   ├── RateLimiterService.java            ← token bucket management
│   └── RateLimiterFilter.java            ← rate limit enforcement
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
GET  /api/v1/auth/validate       → validate JWT token (inter-service)
```

### Airbnb Service (port 3000)
```
POST   /api/airbnb               → create listing (ADMIN only) [10 req/min]
GET    /api/airbnb               → get all listings             [30 req/min]
GET    /api/airbnb/{id}          → get listing by id            [30 req/min]
PUT    /api/airbnb/{id}          → update listing (ADMIN only)  [10 req/min]
DELETE /api/airbnb/{id}          → delete listing (ADMIN only)  [10 req/min]

POST   /api/bookings             → create booking (USER only)   [5 req/min]
PATCH  /api/bookings             → confirm/cancel booking        [5 req/min]

GET    /actuator/prometheus      → Prometheus metrics (public, no rate limit)
GET    /actuator/health          → health check (public, no rate limit)
```

---

## 🔄 Booking Flow

```
1.  USER hits POST /api/bookings
2.  JwtAuthFilter → calls auth service /validate → extracts email + role
3.  RateLimiterFilter → checks token bucket → 5 req/min per user
4.  Redis lock acquired for airbnbId + dateRange
5.  Availability checked → if booked → 422 error
6.  Booking saved to MySQL (PENDING)
7.  Availability slots marked with bookingId
8.  Booking written to Redis (CQRS read model)
9.  idempotencyKey returned to client
10. Prometheus counter incremented

11. USER hits PATCH /api/bookings with idempotencyKey + CONFIRMED
12. Redis read model fetched (CQRS)
13. Status updated to CONFIRMED in MySQL + Redis
14. Saga event published → BOOKING_CONFIRM_REQUESTED
15. SagaEventConsumer processes → BOOKING_CONFIRMED
16. Availability slots finalized
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

# Actuator
management.endpoints.web.exposure.include=health,info,prometheus,metrics
management.endpoint.prometheus.enabled=true
management.prometheus.metrics.export.enabled=true
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
```promql
# Bookings per minute
rate(booking_created_total[1m])

# Booking failure rate %
rate(booking_failed_total[5m]) / rate(booking_created_total[5m]) * 100

# Rate limit blocks per minute
rate(rate_limit_blocked_total[1m])

# Average API response time
rate(http_server_requests_seconds_sum[5m]) / rate(http_server_requests_seconds_count[5m])

# Active DB connections
hikaricp_connections_active

# JVM Heap usage %
jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"} * 100
```

---

## 🚦 Rate Limiting

### Token Bucket Algorithm
```
Bucket capacity = N tokens
Refill rate     = N tokens per minute

Each request    → consumes 1 token
No tokens left  → 429 Too Many Requests
Tokens refill   → gradually over time
```

### Testing Rate Limits
```bash
# Test general endpoint (30 req/min limit)
for i in {1..32}; do
  curl -s -o /dev/null -w "Request $i: HTTP %{http_code} | Remaining: %header{x-rate-limit-remaining}\n" \
    -X GET http://localhost:3000/api/airbnb \
    -H "Authorization: Bearer <your_token>"
done
```

Expected output:
```
Request 1:  HTTP 200 | Remaining: 29
...
Request 30: HTTP 200 | Remaining: 0
Request 31: HTTP 429 | Remaining: (empty)
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

```
Push to master
    ↓
Spin up MySQL + Redis in pipeline
    ↓
Set up JDK 21 + Cache Gradle
    ↓
Build project (skip tests)
    ↓
Run tests
    ↓
Upload test reports
    ↓
Print build summary
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
| Token Bucket | Rate limiting via Bucket4j | Smooth rate limiting with burst support |

---

## 🔐 Security

- All endpoints protected by JWT validation via Auth Microservice
- Role-based access: `ADMIN` for listings, `USER` for bookings
- No JWT logic duplicated in Airbnb service — delegated to Auth service
- Customer identity derived from JWT — never trusted from request body
- ADMIN role exempt from rate limiting
- Rate limiting applied per-user using email from JWT

---

## 📝 Author

**Dhruv Saboo**
- GitHub: [@dhruvsaboo1805](https://github.com/dhruvsaboo1805)
