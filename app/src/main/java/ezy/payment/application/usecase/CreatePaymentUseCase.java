package ezy.payment.application.usecase;

import ezy.payment.application.dto.CreatePaymentInputDto;
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
    public void execute(String idempotencyKeyValue, CreatePaymentInputDto input) {
        IdempotencyKey idempotencyKey = new IdempotencyKey(idempotencyKeyValue);
        CardNumber cardNumber = new CardNumber(input.getCardNumber());

        String requestHash = hashService.hashRequest(
                input.getFirstName(),
                input.getLastName(),
                input.getExpiry(),
                input.getCardNumber(),
                input.getCvv()
        );

        Optional<IdempotencyRecord> existingRecord = idempotencyRepository.findByKey(idempotencyKey);
        if (existingRecord.isPresent()) {
            IdempotencyRecord record = existingRecord.get();
            if (record.matchesHash(requestHash)) {
                return;
            } else {
                throw new IdempotencyConflictException(
                        "Idempotency key reused with different request payload"
                );
            }
        }

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

        paymentRepository.save(payment);

        IdempotencyRecord idempotencyRecord = new IdempotencyRecord(
                idempotencyKey,
                requestHash,
                paymentId,
                201,
                "{}",
                OffsetDateTime.now()
        );
        idempotencyRepository.save(idempotencyRecord);
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
