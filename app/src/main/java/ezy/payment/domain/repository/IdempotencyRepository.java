package ezy.payment.domain.repository;

import ezy.payment.domain.entity.IdempotencyRecord;
import ezy.payment.domain.valueobject.IdempotencyKey;
import java.util.Optional;

/**
 * Domain Repository Interface for IdempotencyRecord.
 * Abstracts persistence of idempotency records.
 */
public interface IdempotencyRepository {
    void save(IdempotencyRecord record);
    Optional<IdempotencyRecord> findByKey(IdempotencyKey key);
}
