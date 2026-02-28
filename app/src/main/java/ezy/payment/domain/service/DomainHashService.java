package ezy.payment.domain.service;

/**
 * Domain Service for hashing operations.
 * Core business logic for ensuring idempotency key matching.
 */
public interface DomainHashService {
    String hashRequest(String firstName, String lastName, String expiry, String cardNumber, String cvv);
}
