package ezy.payment.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA Repository for IdempotencyRecordJpaEntity.
 * Pure technical interface for database access.
 */
public interface IdempotencyRecordJpaRepository extends JpaRepository<IdempotencyRecordJpaEntity, String> {
}
