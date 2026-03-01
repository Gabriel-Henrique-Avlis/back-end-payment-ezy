package ezy.payment.presentation.controller;

import ezy.payment.application.dto.CreatePaymentInputDto;
import ezy.payment.application.service.PaymentApplicationService;
import ezy.payment.application.usecase.CreatePaymentUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping(path = "/payments", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
@CrossOrigin(
    origins = "http://localhost:5173",
    allowedHeaders = {"Content-Type", "Idempotency-Key"},
    methods = {org.springframework.web.bind.annotation.RequestMethod.GET, 
               org.springframework.web.bind.annotation.RequestMethod.POST,
               org.springframework.web.bind.annotation.RequestMethod.PUT,
               org.springframework.web.bind.annotation.RequestMethod.DELETE,
               org.springframework.web.bind.annotation.RequestMethod.OPTIONS},
    allowCredentials = "true",
    maxAge = 3600
)
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);
    private final PaymentApplicationService paymentApplicationService;

    public PaymentController(PaymentApplicationService paymentApplicationService) {
        this.paymentApplicationService = paymentApplicationService;
    }

    @GetMapping
    public ResponseEntity<?> info() {
        return ResponseEntity.ok(Map.of(
                "service", "Payment API",
                "endpoint", "POST /payments",
                "requiredHeaders", "Idempotency-Key"
        ));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createPayment(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody CreatePaymentInputDto input) {

        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Idempotency-Key header is required"));
        }
        if (idempotencyKey.length() < 16 || idempotencyKey.length() > 128) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Idempotency-Key must be between 16 and 128 characters"));
        }

        try {
            paymentApplicationService.createPayment(idempotencyKey, input);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (CreatePaymentUseCase.IdempotencyConflictException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", ex.getMessage()));
        } catch (IllegalArgumentException ex) {
            String correlationId = UUID.randomUUID().toString();
            logger.error("Validation error [{}]: {}", correlationId, ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", ex.getMessage(), "correlationId", correlationId));
        } catch (Exception ex) {
            String correlationId = UUID.randomUUID().toString();
            logger.error("Unexpected error [{}]: ", correlationId, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred. Please contact support with correlation ID: " + correlationId, "correlationId", correlationId));
        }
    }
}
