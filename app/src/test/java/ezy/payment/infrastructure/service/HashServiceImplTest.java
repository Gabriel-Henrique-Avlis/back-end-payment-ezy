package ezy.payment.infrastructure.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HashServiceImplTest {

    private HashServiceImpl hashService;

    @BeforeEach
    void setUp() {
        hashService = new HashServiceImpl();
    }

    @Test
    void shouldHashRequestConsistently() {
        String hash1 = hashService.hashRequest("John", "Doe", "12/25", "4111111111111111", "123");
        String hash2 = hashService.hashRequest("John", "Doe", "12/25", "4111111111111111", "123");

        assertThat(hash1).isEqualTo(hash2);
    }

    @Test
    void shouldChangeHashWhenFirstNameChanges() {
        String hash1 = hashService.hashRequest("John", "Doe", "12/25", "4111111111111111", "123");
        String hash2 = hashService.hashRequest("Jane", "Doe", "12/25", "4111111111111111", "123");

        assertThat(hash1).isNotEqualTo(hash2);
    }

    @Test
    void shouldChangeHashWhenCardNumberChanges() {
        String hash1 = hashService.hashRequest("John", "Doe", "12/25", "4111111111111111", "123");
        String hash2 = hashService.hashRequest("John", "Doe", "12/25", "5555555555554444", "123");

        assertThat(hash1).isNotEqualTo(hash2);
    }

    @Test
    void shouldChangeHashWhenCVVChanges() {
        String hash1 = hashService.hashRequest("John", "Doe", "12/25", "4111111111111111", "123");
        String hash2 = hashService.hashRequest("John", "Doe", "12/25", "4111111111111111", "456");

        assertThat(hash1).isNotEqualTo(hash2);
    }

    @Test
    void shouldReturnValidSHA256Hash() {
        String hash = hashService.hashRequest("John", "Doe", "12/25", "4111111111111111", "123");

        // SHA-256 produces 64 hex characters
        assertThat(hash).hasSize(64);
        assertThat(hash).matches("[a-f0-9]{64}");
    }
}
