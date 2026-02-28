package ezy.payment.domain.entity;

import ezy.payment.domain.valueobject.IdempotencyKey;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Domain Entity representing an Idempotency Record.
 * Tracks requests to ensure idempotent behavior.
 */
public class IdempotencyRecord {
    private final IdempotencyKey key;
    private final String requestHash;
    private final UUID paymentId;
    private final int statusCode;
    private final String responseBody;
    private final OffsetDateTime createdAt;

    public IdempotencyRecord(IdempotencyKey key, String requestHash, UUID paymentId, 
                            int statusCode, String responseBody, OffsetDateTime createdAt) {
        if (key == null) throw new IllegalArgumentException("Idempotency key cannot be null");
        if (requestHash == null || requestHash.isBlank()) throw new IllegalArgumentException("Request hash cannot be blank");
        if (paymentId == null) throw new IllegalArgumentException("Payment ID cannot be null");
        if (statusCode < 100 || statusCode >= 600) throw new IllegalArgumentException("Invalid HTTP status code");
        if (responseBody == null || responseBody.isBlank()) throw new IllegalArgumentException("Response body cannot be blank");
        if (createdAt == null) throw new IllegalArgumentException("Created at cannot be null");

        this.key = key;
        this.requestHash = requestHash;
        this.paymentId = paymentId;
        this.statusCode = statusCode;
        this.responseBody = responseBody;
        this.createdAt = createdAt;
    }

    public IdempotencyKey getKey() {
        return key;
    }

    public String getRequestHash() {
        return requestHash;
    }

    public UUID getPaymentId() {
        return paymentId;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean matchesHash(String hash) {
        return this.requestHash.equals(hash);
    }
}
