package ezy.payment.controller;

import ezy.payment.dto.PaymentRequest;
import ezy.payment.dto.PaymentResponse;
import ezy.payment.entity.IdempotencyRecord;
import ezy.payment.entity.Payment;
import ezy.payment.service.IdempotencyService;
import ezy.payment.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import java.util.Optional;

@RestController
@RequestMapping(path = "/payments", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
public class PaymentController {

    private final PaymentService paymentService;
    private final IdempotencyService idempotencyService;

    public PaymentController(PaymentService paymentService, IdempotencyService idempotencyService) {
        this.paymentService = paymentService;
        this.idempotencyService = idempotencyService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createPayment(@RequestHeader("Idempotency-Key") String idempotencyKey,
                                           @Valid @RequestBody PaymentRequest req) throws Exception {

        String reqHash = idempotencyService.computeRequestHash(
                req.getFirstName(), req.getLastName(), req.getExpiry(), 
                req.getCardNumber(), req.getCvv());

        Optional<IdempotencyRecord> existing = idempotencyService.findByKey(idempotencyKey);
        if (existing.isPresent()) {
            IdempotencyRecord rec = existing.get();
            if (idempotencyService.isConflict(rec, reqHash)) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(idempotencyService.getConflictError());
            }
            // return previously stored response
            Payment payment = idempotencyService.getPaymentFromRecord(rec);
            PaymentResponse resp = paymentService.toResponse(payment);
            return ResponseEntity.status(rec.getStatusCode()).body(resp);
        }

        // create new payment
        Payment p = paymentService.createPayment(req);
        PaymentResponse resp = paymentService.toResponse(p);

        // store idempotency record
        idempotencyService.store(idempotencyKey, reqHash, p, HttpStatus.CREATED.value(), resp);

        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }
}
