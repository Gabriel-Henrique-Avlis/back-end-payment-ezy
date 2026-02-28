package ezy.payment.application.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Application Output DTO for Payment Creation.
 * Returned to clients without exposing sensitive data.
 */
public class CreatePaymentOutputDto {

    private UUID id;
    private String firstName;
    private String lastName;
    private String expiry;
    private String cardLast4;
    private OffsetDateTime createdAt;

    public CreatePaymentOutputDto() {}

    public CreatePaymentOutputDto(UUID id, String firstName, String lastName, String expiry, 
                                 String cardLast4, OffsetDateTime createdAt) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.expiry = expiry;
        this.cardLast4 = cardLast4;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getExpiry() {
        return expiry;
    }

    public void setExpiry(String expiry) {
        this.expiry = expiry;
    }

    public String getCardLast4() {
        return cardLast4;
    }

    public void setCardLast4(String cardLast4) {
        this.cardLast4 = cardLast4;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
