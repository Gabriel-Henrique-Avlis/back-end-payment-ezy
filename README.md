# Payment API

A REST API for processing payments with encryption at rest and idempotency support.

> **Note:** This project is implemented with pre-configured settings to accelerate validation and testing of the API. The encryption key and database credentials are hardcoded for quick setup. For production use, these should be externalized and managed securely via environment variables or a secrets management system.

## Features

- **Create Payment** — `POST /payments` with card details
- **Encryption** — Card numbers encrypted with AES-256-GCM
- **Idempotency** — Prevent duplicate payments via `Idempotency-Key` header

## Tech Stack

- Java 17 | Spring Boot 4.0.3 | PostgreSQL 15 | Maven

## API Documentation

Full OpenAPI 3.0 specification available at [`openapi.yaml`](openapi.yaml).

You can view the interactive documentation using:
- [Swagger Editor](https://editor.swagger.io/) - Paste the contents of `openapi.yaml`
- [Swagger UI](https://petstore.swagger.io/) - Use "Explore" and paste the raw GitHub URL
- VS Code with "OpenAPI (Swagger) Editor" extension

The specification includes:
- Complete endpoint documentation
- Request/response schemas with examples
- Validation rules and constraints
- Error response formats
- Rate limiting information

## Project Structure

### Clean Architecture Layers

This project follows **Clean Architecture** with clear separation of concerns across four layers:

```
src/main/java/ezy/payment/
│
├── domain/                           [BUSINESS LOGIC LAYER]
│   ├── entity/                       Domain entities (core business objects)
│   │   ├── Payment                   Payment transaction entity
│   │   └── IdempotencyRecord         Idempotency tracking entity
│   ├── valueobject/                  Value objects (immutable, no identity)
│   │   ├── CardNumber                Card number with validation & encryption
│   │   └── IdempotencyKey            Idempotency key wrapper
│   ├── repository/                   Domain repository interfaces (abstractions)
│   │   ├── PaymentRepository         Interface for payment persistence
│   │   └── IdempotencyRepository     Interface for idempotency tracking
│   └── service/                      Domain services (business logic)
│       ├── DomainEncryptionService   Card encryption interface
│       └── DomainHashService         Request hashing interface
│
├── application/                      [USE CASE LAYER]
│   ├── dto/                          Data transfer objects
│   │   └── CreatePaymentInputDto     API input validation & transformation
│   ├── usecase/                      Use cases (orchestrate business logic)
│   │   └── CreatePaymentUseCase      Create payment with idempotency
│   └── service/                      Application services
│       └── PaymentApplicationService Orchestrate use cases
│
├── infrastructure/                   [TECHNICAL IMPLEMENTATION LAYER]
│   ├── persistence/                  JPA entities & repository interfaces
│   │   ├── PaymentJpaEntity          Database mapping for payments
│   │   ├── IdempotencyRecordJpaEntity Database mapping for idempotency
│   │   └── *JpaRepository            Spring Data JPA interfaces
│   ├── repository/                   Domain repository implementations
│   │   ├── PaymentRepositoryImpl      Payment persistence adapter
│   │   └── IdempotencyRepositoryImpl  Idempotency persistence adapter
│   ├── service/                      Technical service implementations
│   │   ├── EncryptionServiceImpl      AES-256-GCM encryption
│   │   └── HashServiceImpl            SHA-256 request hashing
│   ├── filter/                       HTTP filters
│   │   └── RateLimitFilter           30 requests/minute per IP
│   └── config/                       Spring configuration
│       └── CorsConfig                CORS for localhost:5173
│
└── presentation/                     [API LAYER]
    └── controller/
        └── PaymentController         REST endpoints for /payments
```

### Architecture Benefits

- **Testability** — Business logic (domain) has zero framework dependencies
- **Maintainability** — Clear boundaries and responsibilities per layer
- **Flexibility** — Swap implementations (e.g., PostgreSQL → MongoDB) without affecting domain logic
- **Scalability** — Infrastructure changes don't impact business rules

### Database Entities

The application uses two main tables:

**1. `payments` table** — Stores encrypted payment transactions
```sql
-- Column          Type
id                UUID (Primary Key)
first_name        VARCHAR
last_name         VARCHAR
expiry            VARCHAR (e.g., "12/25")
encrypted_card_number  TEXT (AES-256-GCM encrypted)
card_last4        VARCHAR (Last 4 digits only, for display)
created_at        TIMESTAMP WITH TIMEZONE
```

**2. `idempotency_records` table** — Tracks requests to ensure idempotent behavior
```sql
-- Column          Type
idempotency_key   VARCHAR (Primary Key, 16-128 chars)
request_hash      VARCHAR (SHA-256 hash of request)
payment_id        UUID (Foreign Key → payments.id)
status_code       INTEGER (HTTP status code - always 201)
response_body     TEXT (Empty "{}" for now)
created_at        TIMESTAMP WITH TIMEZONE
```

## Getting Started with Docker Compose

This is the **recommended way** to run the entire application with PostgreSQL automatically configured.

### Prerequisites

- Docker & Docker Compose installed
- 2GB minimum free memory
- Port 8080 (API), 5432 (PostgreSQL) available

### Step 1: Build the Application

```bash
cd back-end-payment-ezy
docker-compose build
```

This command:
- Pulls the official Maven image
- Compiles the Java application
- Runs all 26 unit tests
- Packages into WAR/JAR
- Creates Docker image `payment-app:latest`

Expected output:
```
[INFO] BUILD SUCCESS
...
Successfully tagged payment-app:latest
```

### Step 2: Start the Application & Database

```bash
docker-compose up
```

This command:
- Starts PostgreSQL 15 container (port 5432)
- Creates `payments` and `idempotency_records` tables automatically
- Starts Payment API container (port 8080)
- Sets up network connectivity between containers

Expected output:
```
payment-db  | PostgreSQL init process complete; ready for start up.
payment-db  | database system is ready to accept connections
payment-app | Started PaymentApplication in X seconds
```

### Step 3: Test the API

```bash
curl -X POST http://localhost:8080/payments \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: test-key-1234567890ab" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "expiry": "12/25",
    "cvv": "123",
    "cardNumber": "4532015112830366"
  }'
```

Expected response:
```
HTTP/1.1 201 Created
(no response body)
```

### Step 4: Verify Data in PostgreSQL

Open another terminal and connect to PostgreSQL:

```bash
docker exec -it payment-db psql -U postgres -d payments
```

Inside the PostgreSQL shell:

```sql
-- View all payments
SELECT id, first_name, last_name, expiry, card_last4, created_at FROM payments;

-- View all idempotency records
SELECT idempotency_key, request_hash, payment_id, status_code, created_at FROM idempotency_records;

-- View encrypted card numbers (they look like gibberish)
SELECT id, encrypted_card_number FROM payments;

-- Exit PostgreSQL
\q
```

Example output:
```
payments=# SELECT * FROM payments;
                  id                  | first_name | last_name | expiry | card_last4 |     created_at
--------------------------------------+------------+-----------+--------+------------+---------------------
 550e8400-e29b-41d4-a716-446655440000 | John       | Doe       | 12/25  | 0366       | 2026-02-28 13:00:00
(1 row)

payments=# SELECT * FROM idempotency_records;
   idempotency_key    |                          request_hash                           | payment_id | status_code | created_at
----------------------+------------------------------------------------------------+------+------------+---------------------
 test-key-1234567890ab | a7f3c9... (SHA-256 hash)                                | ...  |        201 | 2026-02-28 13:00:00
(1 row)
```

### Step 5: Run Unit Tests (Inside Container)

```bash
docker exec payment-app ./mvnw test
```

This runs all 26 tests:
- 3 CreatePaymentUseCase tests
- 5 PaymentController tests
- 8 CardNumber validation tests
- 4 Encryption service tests
- 5 Hash service tests
- 1 Integration test

Expected output:
```
[INFO] Results:
[INFO] Tests run: 26, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

### Step 6: View Application Logs

```bash
docker-compose logs -f payment-app
```

Press `Ctrl+C` to stop following logs.

### Stop the Application

```bash
docker-compose down
```

This stops and removes containers. Data in PostgreSQL is preserved (volume: `payment_db_data`).

### Alternative: Local Development Setup

If you prefer to avoid Docker for development:

```bash
# Start only PostgreSQL
docker run --name payments-db \
  -e POSTGRES_PASSWORD=postgres \
  -e POSTGRES_DB=payments \
  -p 5432:5432 \
  -d postgres:15

# Build and test (from app/ directory)
cd app
mvn clean test
mvn spring-boot:run
```

### Troubleshooting

**Port 8080/5432 already in use:**
```bash
docker-compose down  # Stop all containers
netstat -tuln | grep 8080  # Check what's using the port
```

**Database connection refused:**
```bash
docker-compose down && docker-compose up  # Restart fresh
```

**Tests fail in container:**
```bash
docker exec payment-app ./mvnw clean test -X  # Run with debug output
```

**View all database tables:**
```bash
docker exec -it payment-db psql -U postgres -d payments -c "\dt"
```

## API Usage

```bash
curl -X POST http://localhost:8080/payments \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: unique-key-1234567890" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "expiry": "12/25",
    "cvv": "123",
    "cardNumber": "4532015112830366"
  }'
```

**Response (201 Created):**
```
HTTP/1.1 201 Created
(no response body)
```

## Frontend Integration

The API is configured with CORS to accept requests from `http://localhost:5173` (default Vite dev server).

**JavaScript/TypeScript Example:**
```javascript
const response = await fetch('http://localhost:8080/payments', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Idempotency-Key': crypto.randomUUID() // or your preferred UUID generation
  },
  body: JSON.stringify({
    firstName: 'John',
    lastName: 'Doe',
    expiry: '12/25',
    cvv: '123',
    cardNumber: '4532015112830366'
  })
});

if (response.status === 201) {
  console.log('Payment created successfully');
  // Response has no body, just check status code
} else {
  const error = await response.json();
  console.error(error);
}
```

**Allowed CORS Headers:**
- `Content-Type`
- `Idempotency-Key`

**Allowed Methods:**
- GET, POST, PUT, DELETE, OPTIONS

> **Note:** To use a different frontend origin, update `allowedOrigins` in [CorsConfig.java](app/src/main/java/ezy/payment/infrastructure/config/CorsConfig.java)

## Idempotency

- Same key + same payload → Returns original result (201)
- Same key + different payload → Returns 409 Conflict
- Idempotency key must be 16-128 characters

## Input Validation

All input fields are validated with the following rules:

| Field | Validation Rules |
|-------|-----------------|
| `firstName` | Required, letters/spaces/hyphens/apostrophes only, max 50 chars |
| `lastName` | Required, letters/spaces/hyphens/apostrophes only, max 50 chars |
| `expiry` | Required, MM/YY format (e.g., "12/25") |
| `cvv` | Required, 3-4 digits only |
| `cardNumber` | Required, 13-19 digits |
| `Idempotency-Key` header | Required, 16-128 characters |

**Error Response (400 Bad Request):**
```json
{
  "error": "CVV must be 3-4 digits",
  "correlationId": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Rate Limit Response (429 Too Many Requests):**
```json
{
  "error": "Rate limit exceeded. Maximum 30 requests per minute allowed. Please try again later."
}
```

## Security

### ⚠️ **CRITICAL: Production Security Requirements**

> **🔴 AUTHENTICATION IS MANDATORY FOR PRODUCTION USE**  
> This is a challenge/demonstration project. In a real production system, **API authentication and authorization are ABSOLUTELY REQUIRED**. Without authentication, anyone can create payment transactions, leading to:
> - Unauthorized payment processing
> - Financial fraud and abuse
> - Regulatory compliance violations (PCI-DSS, GDPR, etc.)
> - Legal liability
>
> **Production systems MUST implement:**
> - API Key authentication, OAuth2, or JWT-based authentication
> - Role-based access control (RBAC)
> - Audit logging of all authentication attempts
> - Multi-factor authentication for administrative access

### Implemented Security Features

✅ **Card Data Encryption** — AES-256-GCM encryption at rest  
✅ **No Plaintext Storage** — Card numbers never stored unencrypted  
✅ **PCI-DSS Compliance** — CVV not stored, only last 4 digits exposed  
✅ **Input Validation** — Card format (13-19 digits), CVV format (3-4 digits), expiry format (MM/YY), name sanitization  
✅ **Idempotency Protection** — Prevents duplicate transaction charges  
✅ **Rate Limiting** — 30 requests per minute per IP address  
✅ **CORS Enabled** — Configured for local frontend development (http://localhost:5173)  
✅ **Error Handling** — Stack traces not exposed, correlation IDs for tracking  
✅ **Environment Variables** — Database credentials externalized  
✅ **SQL Injection Protection** — JPA/Hibernate parameterized queries  

### Security Configuration

**Rate Limiting:** 30 requests/min per IP (configured in [RateLimitFilter.java](app/src/main/java/ezy/payment/infrastructure/filter/RateLimitFilter.java))  
**CORS:** Enabled for local development at `http://localhost:5173` (configured in [CorsConfig.java](app/src/main/java/ezy/payment/infrastructure/config/CorsConfig.java))  
- Allowed Methods: GET, POST, PUT, DELETE, OPTIONS
- Allowed Headers: Content-Type, Idempotency-Key
- Max Age: 3600 seconds (1 hour)

> **Note on HTTPS:**  
> HTTPS/TLS is not configured in this demonstration project but is **absolutely critical for production systems** to encrypt data in transit. This is a non-negotiable requirement for any production payment system.  

### Environment Variables (Production)

```bash
export DATABASE_URL=jdbc:postgresql://prod-host:5432/payments
export DATABASE_USERNAME=secure_user
export DATABASE_PASSWORD=secure_password_from_vault
export ENCRYPTION_KEY=generate_32_byte_key_securely
```

### Additional Production Recommendations

- **HTTPS/TLS** — Mandatory for encrypting data in transit (not included in this demo)
- **CORS Configuration** — Control which domains can access your API (not included in this demo)
- **API Authentication** — Implement OAuth2/JWT/API Keys (CRITICAL, not included)
- **Distributed Rate Limiting** — Use Redis for multi-instance deployments
- **Audit Logging** — Log all payment operations for compliance
- **Monitoring & Alerting** — Set up APM tools for real-time visibility
- **Security Audits** — Regular penetration testing and code reviews
- **Data Retention** — Implement policies compliant with regulations
- **Database Encryption** — Enable encryption at rest in PostgreSQL
