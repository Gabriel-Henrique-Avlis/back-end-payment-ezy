package ezy.payment.domain.service;

/**
 * Domain Service for encryption operations.
 * Core business logic for protecting sensitive card data.
 */
public interface DomainEncryptionService {
    String encrypt(String plaintext);
    String decrypt(String ciphertext);
}
