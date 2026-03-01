package ezy.payment.domain.valueobject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CardNumberTest {

    @Test
    void shouldCreateValidCardNumber() {
        CardNumber cardNumber = new CardNumber("4111111111111111");

        assertThat(cardNumber.getValue()).isEqualTo("4111111111111111");
        assertThat(cardNumber.getLast4()).isEqualTo("1111");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "411111111111111",      // 15 digits (13-19 allowed)
            "41111111111111111111", // 20 digits (too long)
            "12345678901",          // 11 digits (too short)
            "1111111111111",        // 13 digits (minimum, valid)
            "12345678901234567890"  // 20 digits (invalid)
    })
    void shouldValidateCardNumberLength(String card) {
        if (card.length() >= 13 && card.length() <= 19) {
            CardNumber cardNumber = new CardNumber(card);
            assertThat(cardNumber.getValue()).isEqualTo(card);
        } else {
            assertThatThrownBy(() -> new CardNumber(card))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Card number must be 13-19 digits");
        }
    }

    @Test
    void shouldReturnLast4Digits() {
        CardNumber cardNumber = new CardNumber("4532015112830366");

        assertThat(cardNumber.getLast4()).isEqualTo("0366");
    }

    @Test
    void shouldReturnLast4DigitsWhenCardHasExactly4Digits() {
        CardNumber cardNumber = new CardNumber("1234567890123");

        assertThat(cardNumber.getLast4()).isEqualTo("0123");
    }
}
