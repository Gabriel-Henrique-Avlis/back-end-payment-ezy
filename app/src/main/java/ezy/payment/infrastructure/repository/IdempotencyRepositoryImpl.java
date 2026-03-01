package ezy.payment.infrastructure.repository;

import ezy.payment.domain.entity.IdempotencyRecord;
import ezy.payment.domain.repository.IdempotencyRepository;
import ezy.payment.domain.valueobject.IdempotencyKey;
import ezy.payment.infrastructure.persistence.IdempotencyRecordJpaEntity;
import ezy.payment.infrastructure.persistence.IdempotencyRecordJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Infrastructure Implementation of Domain IdempotencyRepository.
 * Adapts JPA persistence to domain repository interface.
 */
@Repository
public class IdempotencyRepositoryImpl implements IdempotencyRepository {

    private final IdempotencyRecordJpaRepository jpaRepository;

    public IdempotencyRepositoryImpl(IdempotencyRecordJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void save(IdempotencyRecord record) {
        IdempotencyRecordJpaEntity entity = toJpaEntity(record);
        jpaRepository.save(entity);
    }

    @Override
    public Optional<IdempotencyRecord> findByKey(IdempotencyKey key) {
        return jpaRepository.findById(key.getValue())
                .map(this::toDomainEntity);
    }

    private IdempotencyRecordJpaEntity toJpaEntity(IdempotencyRecord domainRecord) {
        IdempotencyRecordJpaEntity entity = new IdempotencyRecordJpaEntity();
        entity.setKey(domainRecord.getKey().getValue());
        entity.setRequestHash(domainRecord.getRequestHash());
        entity.setPaymentId(domainRecord.getPaymentId().toString());
        entity.setStatusCode(domainRecord.getStatusCode());
        entity.setResponseBody(domainRecord.getResponseBody());
        entity.setCreatedAt(domainRecord.getCreatedAt());
        return entity;
    }

    private IdempotencyRecord toDomainEntity(IdempotencyRecordJpaEntity jpaEntity) {
        return new IdempotencyRecord(
                new IdempotencyKey(jpaEntity.getKey()),
                jpaEntity.getRequestHash(),
                UUID.fromString(jpaEntity.getPaymentId()),
                jpaEntity.getStatusCode(),
                jpaEntity.getResponseBody(),
                jpaEntity.getCreatedAt()
        );
    }
}
