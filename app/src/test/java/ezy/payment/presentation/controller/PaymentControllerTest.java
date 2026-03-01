package ezy.payment.presentation.controller;

import ezy.payment.application.dto.CreatePaymentInputDto;
import ezy.payment.application.service.PaymentApplicationService;
import ezy.payment.application.usecase.CreatePaymentUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    @Mock
    private PaymentApplicationService paymentApplicationService;

    @InjectMocks
    private PaymentController controller;

    @Test
    void shouldReturnBadRequestWhenIdempotencyKeyIsBlank() {
        CreatePaymentInputDto input = new CreatePaymentInputDto("John", "Doe", "12/25", "123", "4111111111111111");

        ResponseEntity<?> response = controller.createPayment("  ", input);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(Map.of("error", "Idempotency-Key header is required"));
        verify(paymentApplicationService, never()).createPayment(anyString(), any());
    }

    @Test
    void shouldReturnBadRequestWhenIdempotencyKeyIsNull() {
        CreatePaymentInputDto input = new CreatePaymentInputDto("John", "Doe", "12/25", "123", "4111111111111111");

        ResponseEntity<?> response = controller.createPayment(null, input);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(Map.of("error", "Idempotency-Key header is required"));
        verify(paymentApplicationService, never()).createPayment(anyString(), any());
    }

    @Test
    void shouldReturnCreatedWhenPaymentSuccessful() {
        CreatePaymentInputDto input = new CreatePaymentInputDto("John", "Doe", "12/25", "123", "4111111111111111");

        ResponseEntity<?> response = controller.createPayment("valid-key-123456", input);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNull();
        verify(paymentApplicationService).createPayment("valid-key-123456", input);
    }

    @Test
    void shouldReturnConflictWhenIdempotencyConflict() {
        CreatePaymentInputDto input = new CreatePaymentInputDto("John", "Doe", "12/25", "123", "4111111111111111");

        doThrow(new CreatePaymentUseCase.IdempotencyConflictException("Idempotency key reused with different request payload"))
                .when(paymentApplicationService).createPayment("conflict-key-12345", input);

        ResponseEntity<?> response = controller.createPayment("conflict-key-12345", input);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isEqualTo(Map.of("error", "Idempotency key reused with different request payload"));
    }

    @Test
    void shouldReturnInfoWhenGetPayments() {
        ResponseEntity<?> response = controller.info();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertThat(body).containsKeys("service", "endpoint", "requiredHeaders");
    }
}
