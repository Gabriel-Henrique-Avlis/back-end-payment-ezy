package ezy.payment.application.usecase;

import ezy.payment.application.dto.CreatePaymentInputDto;
import ezy.payment.application.dto.CreatePaymentOutputDto;
import ezy.payment.domain.entity.IdempotencyRecord;
import ezy.payment.domain.entity.Payment;
import ezy.payment.domain.repository.IdempotencyRepository;
import ezy.payment.domain.repository.PaymentRepository;
import ezy.payment.domain.service.DomainEncryptionService;
import ezy.payment.domain.service.DomainHashService;
import ezy.payment.domain.valueobject.CardNumber;
import ezy.payment.domain.valueobject.IdempotencyKey;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Use Case: Create Payment with Idempotency.
 * Orchestrates the business logic for creating a payment while ensuring idempotency.
 */
@Component
public class CreatePaymentUseCase {

    private final PaymentRepository paymentRepository;
    private final IdempotencyRepository idempotencyRepository;
    private final DomainEncryptionService encryptionService;
    private final DomainHashService hashService;

    public CreatePaymentUseCase(PaymentRepository paymentRepository,
                               IdempotencyRepository idempotencyRepository,
                               DomainEncryptionService encryptionService,
                               DomainHashService hashService) {
        this.paymentRepository = paymentRepository;
        this.idempotencyRepository = idempotencyRepository;
        this.encryptionService = encryptionService;
        this.hashService = hashService;
    }

    @Transactional
    public CreatePaymentOutputDto execute(String idempotencyKeyValue, CreatePaymentInputDto input) {
        // Create value objects
        IdempotencyKey idempotencyKey = new IdempotencyKey(idempotencyKeyValue);
        CardNumber cardNumber = new CardNumber(input.getCardNumber());

        // Compute request hash
        String requestHash = hashService.hashRequest(
                input.getFirstName(),
                input.getLastName(),
                input.getExpiry(),
                input.getCardNumber(),
                input.getCvv()
        );

        // Check if idempotency key already exists
        Optional<IdempotencyRecord> existingRecord = idempotencyRepository.findByKey(idempotencyKey);
        if (existingRecord.isPresent()) {
            IdempotencyRecord record = existingRecord.get();
            // If same payload, return previously created payment
            if (record.matchesHash(requestHash)) {
                Payment payment = paymentRepository.findById(record.getPaymentId())
                        .orElseThrow(() -> new IllegalStateException("Payment associated with idempotency record not found"));
                return toOutputDto(payment);
            } else {
                // If different payload, throw conflict error
                throw new IdempotencyConflictException(
                        "Idempotency key reused with different request payload"
                );
            }
        }

        // Create new payment
        String encryptedCardNumber = encryptionService.encrypt(input.getCardNumber());
        UUID paymentId = UUID.randomUUID();
        
        Payment payment = new Payment(
                paymentId,
                input.getFirstName(),
                input.getLastName(),
                input.getExpiry(),
                cardNumber,
                encryptedCardNumber,
                OffsetDateTime.now()
        );

        // Persist payment
        paymentRepository.save(payment);

        // Create and persist idempotency record
        IdempotencyRecord idempotencyRecord = new IdempotencyRecord(
                idempotencyKey,
                requestHash,
                paymentId,
                201, // HTTP 201 Created
                "{}", // Response body will be set by application service
                OffsetDateTime.now()
        );
        idempotencyRepository.save(idempotencyRecord);

        return toOutputDto(payment);
    }

    private CreatePaymentOutputDto toOutputDto(Payment payment) {
        return new CreatePaymentOutputDto(
                payment.getId(),
                payment.getFirstName(),
                payment.getLastName(),
                payment.getExpiry(),
                payment.getCardLast4(),
                payment.getCreatedAt()
        );
    }

    /**
     * Custom exception for idempotency conflicts.
     */
    public static class IdempotencyConflictException extends RuntimeException {
        public IdempotencyConflictException(String message) {
            super(message);
        }
    }
}
