package ezy.payment.infrastructure.service;

import ezy.payment.domain.service.DomainEncryptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class EncryptionServiceImpl implements DomainEncryptionService {

    private static final Logger logger = LoggerFactory.getLogger(EncryptionServiceImpl.class);
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int IV_SIZE = 12;
    private static final int TAG_SIZE = 128;

    private final SecretKey key;
    private final SecureRandom random = new SecureRandom();

    public EncryptionServiceImpl(@Value("${encryption.key}") String base64Key) {
        byte[] keyBytes = base64Key.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        this.key = new SecretKeySpec(keyBytes, "AES");
    }

    @Override
    public String encrypt(String plaintext) {
        try {
            byte[] iv = new byte[IV_SIZE];
            random.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_SIZE, iv));
            byte[] encrypted = cipher.doFinal(plaintext.getBytes());

            byte[] result = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, result, 0, iv.length);
            System.arraycopy(encrypted, 0, result, iv.length, encrypted.length);

            return Base64.getEncoder().encodeToString(result);
        } catch (Exception e) {
            logger.error("Encryption operation failed", e);
            throw new SecurityException("Encryption operation failed");
        }
    }

    @Override
    public String decrypt(String encrypted) {
        try {
            byte[] data = Base64.getDecoder().decode(encrypted);
            byte[] iv = new byte[IV_SIZE];
            System.arraycopy(data, 0, iv, 0, IV_SIZE);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_SIZE, iv));
            byte[] decrypted = cipher.doFinal(data, IV_SIZE, data.length - IV_SIZE);

            return new String(decrypted);
        } catch (Exception e) {
            logger.error("Decryption operation failed", e);
            throw new SecurityException("Decryption operation failed");
        }
    }
}

