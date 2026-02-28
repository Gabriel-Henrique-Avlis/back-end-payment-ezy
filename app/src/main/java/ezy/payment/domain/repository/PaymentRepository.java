package ezy.payment.domain.repository;

import ezy.payment.domain.entity.Payment;
import java.util.UUID;
import java.util.Optional;

/**
 * Domain Repository Interface for Payment.
 * Abstracts persistence of Payment aggregate.
 */
public interface PaymentRepository {
    void save(Payment payment);
    Optional<Payment> findById(UUID id);
}
