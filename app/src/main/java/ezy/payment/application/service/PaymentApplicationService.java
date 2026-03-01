package ezy.payment.application.service;

import ezy.payment.application.dto.CreatePaymentInputDto;
import ezy.payment.application.usecase.CreatePaymentUseCase;
import org.springframework.stereotype.Service;

/**
 * Application Service for Payment Operations.
 * Orchestrates use cases and provides high-level application operations.
 */
@Service
public class PaymentApplicationService {

    private final CreatePaymentUseCase createPaymentUseCase;

    public PaymentApplicationService(CreatePaymentUseCase createPaymentUseCase) {
        this.createPaymentUseCase = createPaymentUseCase;
    }

    public void createPayment(String idempotencyKey, CreatePaymentInputDto input) {
        createPaymentUseCase.execute(idempotencyKey, input);
    }
}
