package dev.forgepack.utils.internal.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

/**
 * End-to-End Encryption (SecretEncryptor) utility class using AES encryption with secure practices.
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
public class SecretEncryptor {
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final String KEY_ALGORITHM = "AES";
    private static final int IV_LENGTH = 12;
    private static final int TAG_LENGTH_BITS = 128;
    private static final int MIN_ENCRYPTED_LENGTH = IV_LENGTH + TAG_LENGTH_BITS / 8;

    private static final Logger log = LoggerFactory.getLogger(SecretEncryptor.class);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final SecretKeySpec keySpec;

    /**
     * @param base64Key chave AES em Base64 (16, 24 ou 32 bytes após decodificar)
     */
    public SecretEncryptor(String base64Key) {
        if (base64Key == null || base64Key.isBlank()) {
            throw new IllegalArgumentException("Encryption key must be configured");
        }
        byte[] key;
        try {
            key = Base64.getDecoder().decode(base64Key.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Encryption key must be Base64-encoded", e);
        }
        if (key.length != 16 && key.length != 24 && key.length != 32) {
            throw new IllegalArgumentException("Encryption key must have 16, 24 or 32 bytes");
        }
        this.keySpec = new SecretKeySpec(key, KEY_ALGORITHM);
    }

    /**
    * Encrypts data using AES-GCM with a random IV for each operation.
     * The IV is prepended to the encrypted data for decryption.
     *
     * @param data The plaintext data to encrypt
     * @return Base64-encoded string containing IV + encrypted data
     * @throws SecretEncryptorException if encryption fails
     */
    public String encrypt(String data) throws SecretEncryptorException {
        if (data == null) {
            throw new SecretEncryptorException("Input data cannot be null");
        }

        try {
            // Generate random IV for this encryption
            byte[] iv = new byte[IV_LENGTH];
            SECURE_RANDOM.nextBytes(iv);

            // Initialize cipher
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, new GCMParameterSpec(TAG_LENGTH_BITS, iv));

            // Encrypt the data
            byte[] encryptedData = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));

            // Combine IV + encrypted data
            byte[] combined = new byte[IV_LENGTH + encryptedData.length];
            System.arraycopy(iv, 0, combined, 0, IV_LENGTH);
            System.arraycopy(encryptedData, 0, combined, IV_LENGTH, encryptedData.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            log.error("Encryption failed: {}", e.getMessage());
            throw new SecretEncryptorException("Failed to encrypt data", e);
        }
    }
    /**
     * Decrypts data that was encrypted with the encrypt method.
     * Extracts the IV from the beginning of the encrypted data.
     *
     * @param encryptedData Base64-encoded string containing IV + encrypted data
     * @return The original plaintext data
     * @throws SecretEncryptorException if decryption fails
     */
    public String decrypt(String encryptedData) throws SecretEncryptorException {
        if (encryptedData == null || encryptedData.isBlank()) {
            throw new SecretEncryptorException("Encrypted data cannot be null or empty");
        }

        try {
            // Decode the Base64 data
            byte[] combined = Base64.getDecoder().decode(encryptedData);

            // Validate minimum length (IV + at least 1 block of encrypted data)
            if (combined.length < MIN_ENCRYPTED_LENGTH) {
                throw new SecretEncryptorException("Invalid encrypted data: too short");
            }

            // Initialize cipher for decryption
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, new GCMParameterSpec(TAG_LENGTH_BITS, combined, 0, IV_LENGTH));
            byte[] decrypted = cipher.doFinal(combined, IV_LENGTH, combined.length - IV_LENGTH);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Decryption failed: {}", e.getMessage());
            throw new SecretEncryptorException("Failed to decrypt data", e);
        }
    }
    /**
     * Generates a new AES secret key with the specified key length.
     *
     * @param keyLength Key length in bits (128, 192, or 256)
     * @return A new SecretKey for AES encryption
     * @throws SecretEncryptorException if key generation fails
     */
    public SecretKey generateKey(int keyLength) throws SecretEncryptorException {
        if (keyLength != 128 && keyLength != 192 && keyLength != 256) {
            throw new SecretEncryptorException("Invalid key length. Must be 128, 192 or 256 bits");
        }
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(KEY_ALGORITHM);
            keyGenerator.init(keyLength, SECURE_RANDOM);
            return keyGenerator.generateKey();
        } catch (GeneralSecurityException e) {
            log.error("Key generation failed: {}", e.getMessage());
            throw new SecretEncryptorException("Failed to generate encryption key", e);
        }
    }

    /**
     * Generates a random secure key for encryption as Base64 string.
     * Useful for generating new keys to be stored in configuration at app.encryption.secret.
     *
     * @param keyLength Key length in bits
     * @return Base64-encoded random key
     * @throws SecretEncryptorException if key generation fails
     */
    public String generateKeyAsString(int keyLength) throws SecretEncryptorException {
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
     * Custom exception for SecretEncryptor operations
     */
    public static class SecretEncryptorException extends Exception {
        public SecretEncryptorException(String message) {
            super(message);
        }

        public SecretEncryptorException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
