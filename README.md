# Payment API

A REST API for processing payments with encryption at rest and idempotency support.

> **Note:** This project is implemented with pre-configured settings to accelerate validation and testing of the API. The encryption key and database credentials are hardcoded for quick setup. For production use, these should be externalized and managed securely via environment variables or a secrets management system.

## Features

- **Create Payment** — `POST /payments` with card details
- **Encryption** — Card numbers encrypted with AES-256-GCM
- **Idempotency** — Prevent duplicate payments via `Idempotency-Key` header

## Tech Stack

- Java 17 | Spring Boot 4.0.3 | PostgreSQL 15 | Maven

## Project Structure

```
src/main/java/ezy/payment/
├── domain/
│   ├── entity/           Payment, IdempotencyRecord
│   ├── valueobject/      CardNumber, IdempotencyKey
│   ├── repository/       PaymentRepository, IdempotencyRepository
│   └── service/          DomainEncryptionService, DomainHashService
├── application/
│   ├── dto/              CreatePaymentInputDto, CreatePaymentOutputDto
│   ├── usecase/          CreatePaymentUseCase
│   └── service/          PaymentApplicationService
├── infrastructure/
│   ├── persistence/      JPA entities & repositories
│   ├── repository/       Repository implementations
│   └── service/          EncryptionServiceImpl, HashServiceImpl
└── presentation/
    └── controller/       PaymentController
```

## Quick Start

### Docker Compose

```bash
docker-compose up --build
```

### Local Setup

```bash
# Start PostgreSQL container
docker run --name payments-db -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=payments -p 5432:5432 -d postgres:15

# Build & run
cd app
mvn clean package -DskipTests
java -jar target/payment-0.0.1-SNAPSHOT.jar
```

## API Usage

```bash
curl -X POST http://localhost:8080/payments \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: unique-key-123" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "expiry": "12/25",
    "cvv": "123",
    "cardNumber": "4532015112830366"
  }'
```

**Response (201 Created):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "firstName": "John",
  "lastName": "Doe",
  "expiry": "12/25",
  "cardLast4": "0366",
  "createdAt": "2026-02-27T10:30:00Z"
}
```

## Idempotency

- Same key + same payload → Returns original result (201)
- Same key + different payload → Returns 409 Conflict

## Security

- **Card Encryption** — AES-256-GCM at rest
- **No Raw Data** — API returns only masked card (last 4 digits)
- **Validation** — Card 13-19 digits, CVV 3-4 digits
- **Idempotency Keys** — Prevent duplicate transactions
