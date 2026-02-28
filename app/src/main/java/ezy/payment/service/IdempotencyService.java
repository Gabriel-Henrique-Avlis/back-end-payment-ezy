package ezy.payment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ezy.payment.entity.IdempotencyRecord;
import ezy.payment.entity.Payment;
import ezy.payment.repository.IdempotencyRepository;
import ezy.payment.repository.PaymentRepository;
import ezy.payment.util.HashUtil;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class IdempotencyService {

    private final IdempotencyRepository idempotencyRepository;
    private final PaymentRepository paymentRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public IdempotencyService(IdempotencyRepository idempotencyRepository, PaymentRepository paymentRepository) {
        this.idempotencyRepository = idempotencyRepository;
        this.paymentRepository = paymentRepository;
    }

    public String computeRequestHash(String firstName, String lastName, String expiry, String cardNumber, String cvv) {
        String canonical = String.format("firstName=%s|lastName=%s|expiry=%s|card=%s|cvv=%s",
                firstName, lastName, expiry, cardNumber, cvv);
        return HashUtil.sha256Hex(canonical);
    }

    public Optional<IdempotencyRecord> findByKey(String key) {
        return idempotencyRepository.findById(key);
    }

    public boolean isConflict(IdempotencyRecord existing, String newHash) {
        return !existing.getRequestHash().equals(newHash);
    }

    public Map<String, Object> getConflictError() {
        return Map.of("error", "Idempotency key reused with different request payload");
    }

    @Transactional
    public void store(String key, String requestHash, Payment payment, int statusCode, Object responseBody) throws Exception {
        String responseJson = objectMapper.writeValueAsString(responseBody);
        
        IdempotencyRecord rec = new IdempotencyRecord();
        rec.setKey(key);
        rec.setRequestHash(requestHash);
        rec.setPaymentId(payment.getId().toString());
        rec.setStatusCode(statusCode);
        rec.setResponseBody(responseJson);
        rec.setCreatedAt(OffsetDateTime.now());
        
        idempotencyRepository.save(rec);
    }

    public Payment getPaymentFromRecord(IdempotencyRecord record) {
        return paymentRepository.findById(UUID.fromString(record.getPaymentId()))
                .orElseThrow(() -> new IllegalStateException("Payment not found for idempotency record"));
    }
}
