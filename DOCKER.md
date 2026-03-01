# Docker Setup

## Quick Start

```bash
docker-compose up --build
```

Or in background:

```bash
docker-compose up --build -d
```

## Verify Services

```bash
docker-compose ps
```

Both `payments-db` and `payment-app` should show `Up (healthy)`.

## Test API

```bash
curl -X POST http://localhost:8080/payments \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: test-123" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "expiry": "12/25",
    "cvv": "123",
    "cardNumber": "4532015112830366"
  }'
```

## Common Commands

Stop services:
```bash
docker-compose down
```

View logs:
```bash
docker-compose logs -f
```

Clean database:
```bash
docker-compose down -v
```


