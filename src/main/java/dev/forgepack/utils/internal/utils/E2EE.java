package dev.forgepack.utils.internal.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

/**
 * End-to-End Encryption (E2EE) utility class using AES encryption with secure practices.
 *
 * Security Features:
 * - Random IV for each encryption operation
 * - Configurable encryption key via environment variables
 * - Secure exception handling
 * - Base64 encoding for safe text transmission
 *
 * @author Marcelo Ribeiro Gadelha
 * Website: www.gadelha.eti.br
 **/

@Service
public class E2EE {
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String KEY_ALGORITHM = "AES";
    private static final int IV_LENGTH = 16; // 128 bits for AES
    private static final int KEY_LENGTH = 16; // 128 bits for AES

    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.encryption.secret:defaultSecretKey1234567890123456}")
    private String configuredSecretKey;

    /**
     * Gets the encryption key, ensuring it's exactly 16 bytes for AES-128.
     * If configured key is longer, it's truncated. If shorter, it's padded.
     */
    private byte[] getSecretKeyBytes() {
        byte[] keyBytes = configuredSecretKey.getBytes(StandardCharsets.UTF_8);
        return Arrays.copyOf(keyBytes, KEY_LENGTH);
    }

    /**
     * Encrypts data using AES-CBC with a random IV for each operation.
     * The IV is prepended to the encrypted data for decryption.
     *
     * @param data The plaintext data to encrypt
     * @return Base64-encoded string containing IV + encrypted data
     * @throws E2EEException if encryption fails
     */
    public String encrypt(String data) throws E2EEException {
        if (data == null) {
            throw new E2EEException("Input data cannot be null");
        }

        try {
            // Generate random IV for this encryption
            byte[] iv = new byte[IV_LENGTH];
            secureRandom.nextBytes(iv);

            // Initialize cipher
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(getSecretKeyBytes(), KEY_ALGORITHM);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

            // Encrypt the data
            byte[] encryptedData = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));

            // Combine IV + encrypted data
            byte[] combined = new byte[IV_LENGTH + encryptedData.length];
            System.arraycopy(iv, 0, combined, 0, IV_LENGTH);
            System.arraycopy(encryptedData, 0, combined, IV_LENGTH, encryptedData.length);

            return Base64.getEncoder().encodeToString(combined);

        } catch (Exception e) {
//            log.error("Encryption failed: {}", e.getMessage());
            throw new E2EEException("Failed to encrypt data", e);
        }
    }
    /**
     * Decrypts data that was encrypted with the encrypt method.
     * Extracts the IV from the beginning of the encrypted data.
     *
     * @param encryptedData Base64-encoded string containing IV + encrypted data
     * @return The original plaintext data
     * @throws E2EEException if decryption fails
     */
    public String decrypt(String encryptedData) throws E2EEException {
        if (encryptedData == null || encryptedData.trim().isEmpty()) {
            throw new E2EEException("Encrypted data cannot be null or empty");
        }

        try {
            // Decode the Base64 data
            byte[] combined = Base64.getDecoder().decode(encryptedData);

            // Validate minimum length (IV + at least 1 block of encrypted data)
            if (combined.length < IV_LENGTH + 16) {
                throw new E2EEException("Invalid encrypted data: too short");
            }

            // Extract IV and encrypted data
            byte[] iv = new byte[IV_LENGTH];
            byte[] encrypted = new byte[combined.length - IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, IV_LENGTH);
            System.arraycopy(combined, IV_LENGTH, encrypted, 0, encrypted.length);
            // Initialize cipher for decryption
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(getSecretKeyBytes(), KEY_ALGORITHM);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            // Decrypt the data
            byte[] decrypted = cipher.doFinal(encrypted);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
//            log.error("Decryption failed: {}", e.getMessage());
            throw new E2EEException("Failed to decrypt data", e);
        }
    }
    /**
     * Generates a new AES secret key with the specified key length.
     *
     * @param keyLength Key length in bits (128, 192, or 256)
     * @return A new SecretKey for AES encryption
     * @throws E2EEException if key generation fails
     */
    public SecretKey generateKey(int keyLength) throws E2EEException {
        try {
            if (keyLength != 128 && keyLength != 192 && keyLength != 256) {
                throw new E2EEException("Invalid key length. Must be 128, 192, or 256 bits");
            }

            KeyGenerator keyGenerator = KeyGenerator.getInstance(KEY_ALGORITHM);
            keyGenerator.init(keyLength);
            return keyGenerator.generateKey();

        } catch (NoSuchAlgorithmException e) {
//            log.error("Key generation failed: {}", e.getMessage());
            throw new E2EEException("Failed to generate encryption key", e);
        }
    }

    /**
     * Generates a random secure key for encryption as Base64 string.
     * Useful for generating new keys to be stored in configuration.
     *
     * @param keyLength Key length in bits
     * @return Base64-encoded random key
     * @throws E2EEException if key generation fails
     */
    public String generateKeyAsString(int keyLength) throws E2EEException {
        SecretKey key = generateKey(keyLength);
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    /**
     * Validates if the provided encrypted data can be decrypted successfully.
     *
     * @param encryptedData The encrypted data to validate
     * @return true if data can be decrypted, false otherwise
     */
    public boolean isValidEncryptedData(String encryptedData) {
        try {
            decrypt(encryptedData);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Custom exception for E2EE operations
     */
    public static class E2EEException extends Exception {
        public E2EEException(String message) {
            super(message);
        }

        public E2EEException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
