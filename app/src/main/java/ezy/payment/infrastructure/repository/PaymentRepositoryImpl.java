package ezy.payment.infrastructure.repository;

import ezy.payment.domain.entity.Payment;
import ezy.payment.domain.repository.PaymentRepository;
import ezy.payment.domain.valueobject.CardNumber;
import ezy.payment.infrastructure.persistence.PaymentJpaEntity;
import ezy.payment.infrastructure.persistence.PaymentJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class PaymentRepositoryImpl implements PaymentRepository {

    private final PaymentJpaRepository jpaRepository;

    public PaymentRepositoryImpl(PaymentJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void save(Payment payment) {
        PaymentJpaEntity entity = toJpaEntity(payment);
        jpaRepository.save(entity);
    }

    @Override
    public Optional<Payment> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(this::toDomainEntity);
    }

    private PaymentJpaEntity toJpaEntity(Payment domainPayment) {
        PaymentJpaEntity entity = new PaymentJpaEntity();
        entity.setId(domainPayment.getId());
        entity.setFirstName(domainPayment.getFirstName());
        entity.setLastName(domainPayment.getLastName());
        entity.setExpiry(domainPayment.getExpiry());
        entity.setEncryptedCardNumber(domainPayment.getEncryptedCardNumber());
        entity.setCardLast4(domainPayment.getCardLast4());
        entity.setCreatedAt(domainPayment.getCreatedAt());
        return entity;
    }

    private Payment toDomainEntity(PaymentJpaEntity jpaEntity) {
        CardNumber cardNumber = new CardNumber(
                "****" + jpaEntity.getCardLast4() // Reconstruct from last4
        );
        return new Payment(
                jpaEntity.getId(),
                jpaEntity.getFirstName(),
                jpaEntity.getLastName(),
                jpaEntity.getExpiry(),
                cardNumber,
                jpaEntity.getEncryptedCardNumber(),
                jpaEntity.getCreatedAt()
        );
    }
}
