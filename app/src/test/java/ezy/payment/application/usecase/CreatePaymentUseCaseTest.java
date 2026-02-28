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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreatePaymentUseCaseTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private IdempotencyRepository idempotencyRepository;

    @Mock
    private DomainEncryptionService encryptionService;

    @Mock
    private DomainHashService hashService;

    @InjectMocks
    private CreatePaymentUseCase useCase;

    private CreatePaymentInputDto validInput;

    @BeforeEach
    void setUp() {
        validInput = new CreatePaymentInputDto("John", "Doe", "12/25", "123", "4111111111111111");
    }

    @Test
    void shouldCreateNewPaymentWhenIdempotencyKeyIsNew() {
        String idempotencyKey = "new-key";
        String requestHash = "hash123";

        when(idempotencyRepository.findByKey(any())).thenReturn(Optional.empty());
        when(encryptionService.encrypt("4111111111111111")).thenReturn("encrypted-card");
        when(hashService.hashRequest("John", "Doe", "12/25", "4111111111111111", "123"))
                .thenReturn(requestHash);

        UUID expectedPaymentId = UUID.randomUUID();
        Payment savedPayment = new Payment(
                expectedPaymentId, "John", "Doe", "12/25",
                new CardNumber("4111111111111111"), "encrypted-card", OffsetDateTime.now()
        );
        doNothing().when(paymentRepository).save(any());
        doNothing().when(idempotencyRepository).save(any());

        CreatePaymentOutputDto output = useCase.execute(idempotencyKey, validInput);

        assertThat(output.getId()).isNotNull();
        assertThat(output.getCardLast4()).isEqualTo("1111");
        verify(paymentRepository, times(1)).save(any());
        verify(idempotencyRepository, times(1)).save(any());
    }

    @Test
    void shouldReturnExistingPaymentWhenIdempotencyKeyMatchesHash() {
        String idempotencyKey = "same-key";
        String requestHash = "hash123";
        UUID paymentId = UUID.randomUUID();

        IdempotencyRecord existingRecord = new IdempotencyRecord(
                new IdempotencyKey(idempotencyKey), requestHash, paymentId, 201, "{}", OffsetDateTime.now()
        );
        when(idempotencyRepository.findByKey(any())).thenReturn(Optional.of(existingRecord));
        when(hashService.hashRequest("John", "Doe", "12/25", "4111111111111111", "123"))
                .thenReturn(requestHash);

        Payment existingPayment = new Payment(
                paymentId, "John", "Doe", "12/25",
                new CardNumber("4111111111111111"), "encrypted-card", OffsetDateTime.now()
        );
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(existingPayment));

        CreatePaymentOutputDto output = useCase.execute(idempotencyKey, validInput);

        assertThat(output.getId()).isEqualTo(paymentId);
        verify(paymentRepository, never()).save(any());
        verify(idempotencyRepository, never()).save(any());
    }

    @Test
    void shouldThrowConflictWhenIdempotencyKeyWithDifferentPayload() {
        String idempotencyKey = "conflict-key";
        String originalHash = "hash-original";
        String newHash = "hash-different";

        IdempotencyRecord existingRecord = new IdempotencyRecord(
                new IdempotencyKey(idempotencyKey), originalHash, UUID.randomUUID(), 201, "{}", OffsetDateTime.now()
        );
        when(idempotencyRepository.findByKey(any())).thenReturn(Optional.of(existingRecord));
        when(hashService.hashRequest("John", "Doe", "12/25", "4111111111111111", "123"))
                .thenReturn(newHash);

        assertThatThrownBy(() -> useCase.execute(idempotencyKey, validInput))
                .isInstanceOf(CreatePaymentUseCase.IdempotencyConflictException.class)
                .hasMessage("Idempotency key reused with different request payload");

        verify(paymentRepository, never()).save(any());
        verify(idempotencyRepository, never()).save(any());
    }
}
