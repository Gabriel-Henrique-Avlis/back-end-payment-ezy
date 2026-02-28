package ezy.payment.domain.entity;

import ezy.payment.domain.valueobject.CardNumber;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Domain Entity representing a Payment.
 * Encapsulates all business logic related to payments.
 */
public class Payment {
    private final UUID id;
    private final String firstName;
    private final String lastName;
    private final String expiry;
    private final CardNumber cardNumber;
    private final String encryptedCardNumber;
    private final OffsetDateTime createdAt;

    public Payment(UUID id, String firstName, String lastName, String expiry, 
                   CardNumber cardNumber, String encryptedCardNumber, OffsetDateTime createdAt) {
        if (id == null) throw new IllegalArgumentException("Payment ID cannot be null");
        if (firstName == null || firstName.isBlank()) throw new IllegalArgumentException("First name cannot be blank");
        if (lastName == null || lastName.isBlank()) throw new IllegalArgumentException("Last name cannot be blank");
        if (expiry == null || expiry.isBlank()) throw new IllegalArgumentException("Expiry cannot be blank");
        if (cardNumber == null) throw new IllegalArgumentException("Card number cannot be null");
        if (encryptedCardNumber == null || encryptedCardNumber.isBlank()) throw new IllegalArgumentException("Encrypted card cannot be blank");
        if (createdAt == null) throw new IllegalArgumentException("Created at cannot be null");

        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.expiry = expiry;
        this.cardNumber = cardNumber;
        this.encryptedCardNumber = encryptedCardNumber;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getExpiry() {
        return expiry;
    }

    public CardNumber getCardNumber() {
        return cardNumber;
    }

    public String getEncryptedCardNumber() {
        return encryptedCardNumber;
    }

    public String getCardLast4() {
        return cardNumber.getLast4();
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
