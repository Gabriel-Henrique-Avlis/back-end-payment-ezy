package ezy.payment.service;

import ezy.payment.dto.PaymentRequest;
import ezy.payment.dto.PaymentResponse;
import ezy.payment.entity.Payment;
import ezy.payment.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final EncryptionService encryptionService;

    public PaymentService(PaymentRepository paymentRepository, EncryptionService encryptionService) {
        this.paymentRepository = paymentRepository;
        this.encryptionService = encryptionService;
    }

    @Transactional
    public Payment createPayment(PaymentRequest req) {
        Payment p = new Payment();
        p.setId(UUID.randomUUID());
        p.setFirstName(req.getFirstName());
        p.setLastName(req.getLastName());
        p.setExpiry(req.getExpiry());
        
        String encrypted = encryptionService.encrypt(req.getCardNumber());
        p.setEncryptedCardNumber(encrypted);
        
        String last4 = req.getCardNumber().length() >= 4 
            ? req.getCardNumber().substring(req.getCardNumber().length() - 4) 
            : req.getCardNumber();
        p.setCardLast4(last4);
        p.setCreatedAt(OffsetDateTime.now());
        
        return paymentRepository.save(p);
    }

    public PaymentResponse toResponse(Payment p) {
        PaymentResponse r = new PaymentResponse();
        r.setId(p.getId());
        r.setFirstName(p.getFirstName());
        r.setLastName(p.getLastName());
        r.setExpiry(p.getExpiry());
        r.setCardLast4(p.getCardLast4());
        r.setCreatedAt(p.getCreatedAt());
        return r;
    }
}
