package ezy.payment.infrastructure.service;

import ezy.payment.domain.service.DomainHashService;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Infrastructure Implementation of DomainHashService.
 * Uses SHA-256 to hash requests for idempotency key matching.
 */
@Component
public class HashServiceImpl implements DomainHashService {

    @Override
    public String hashRequest(String firstName, String lastName, String expiry, String cardNumber, String cvv) {
        String canonical = String.format("firstName=%s|lastName=%s|expiry=%s|card=%s|cvv=%s",
                firstName, lastName, expiry, cardNumber, cvv);
        return sha256Hex(canonical);
    }

    private String sha256Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception ex) {
            throw new RuntimeException("Hashing failed", ex);
        }
    }
}
