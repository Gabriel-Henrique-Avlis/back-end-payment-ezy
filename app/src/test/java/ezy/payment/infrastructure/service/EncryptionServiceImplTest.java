package ezy.payment.infrastructure.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EncryptionServiceImplTest {

    private EncryptionServiceImpl encryptionService;

    @BeforeEach
    void setUp() {
        encryptionService = new EncryptionServiceImpl("n8f9k2m5L7jR3wQ6bN1vX4yP9sH8dF2a");
    }

    @Test
    void shouldEncryptCardNumberNotEqualToPlaintext() {
        String cardNumber = "4111111111111111";

        String encrypted = encryptionService.encrypt(cardNumber);

        assertThat(encrypted).isNotEqualTo(cardNumber);
        assertThat(encrypted).doesNotContain("4111111111111111");
        assertThat(encrypted).doesNotContain("411111");
        assertThat(encrypted).isNotBlank();
    }

    @Test
    void shouldDecryptCardToOriginalValue() {
        String cardNumber = "4111111111111111";

        String encrypted = encryptionService.encrypt(cardNumber);
        String decrypted = encryptionService.decrypt(encrypted);

        assertThat(decrypted).isEqualTo(cardNumber);
    }

    @Test
    void shouldProduceDifferentCiphertextForSamePlaintextDueToRandomIV() {
        String cardNumber = "4111111111111111";

        String encrypted1 = encryptionService.encrypt(cardNumber);
        String encrypted2 = encryptionService.encrypt(cardNumber);

        assertThat(encrypted1).isNotEqualTo(encrypted2);
    }

    @Test
    void shouldEncryptDifferentCardNumbersToNotEqual() {
        String encrypted1 = encryptionService.encrypt("4111111111111111");
        String encrypted2 = encryptionService.encrypt("5555555555554444");

        assertThat(encrypted1).isNotEqualTo(encrypted2);
    }
}
