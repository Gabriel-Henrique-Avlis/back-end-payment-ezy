package ezy.payment.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

/**
 * Spring Data JPA Repository for PaymentJpaEntity.
 * Pure technical interface for database access.
 */
public interface PaymentJpaRepository extends JpaRepository<PaymentJpaEntity, UUID> {
}
