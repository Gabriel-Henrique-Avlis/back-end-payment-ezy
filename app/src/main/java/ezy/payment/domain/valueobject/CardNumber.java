package ezy.payment.domain.valueobject;

import java.util.Objects;

/**
 * Value Object representing a Card Number.
 * Ensures the card number is valid (13-19 digits) and provides access to last4.
 */
public class CardNumber {
    private final String value;

    public CardNumber(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Card number cannot be blank");
        }
        if (!value.matches("\\d{13,19}")) {
            throw new IllegalArgumentException("Card number must be 13-19 digits");
        }
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public String getLast4() {
        return value.length() >= 4 ? value.substring(value.length() - 4) : value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CardNumber that = (CardNumber) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "****" + getLast4();
    }
}
