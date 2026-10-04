package dev.forgepack.utils.internal.service;

import dev.forgepack.utils.api.exception.EncryptorException;
import dev.forgepack.utils.api.service.EncryptorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

public class EncryptorServiceImpl implements EncryptorService {
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final String KEY_ALGORITHM = "AES";
    private static final int IV_LENGTH = 12;
    private static final int TAG_LENGTH_BITS = 128;
    private static final int MIN_ENCRYPTED_LENGTH = IV_LENGTH + TAG_LENGTH_BITS / 8;

    private static final Logger log = LoggerFactory.getLogger(EncryptorServiceImpl.class);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final SecretKeySpec keySpec;

    /**
     * @param base64Key chave AES em Base64 (16, 24 ou 32 bytes após decodificar)
     */
    public EncryptorServiceImpl(String base64Key) {
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

    public String encrypt(String data) throws EncryptorException {
        if (data == null) {
            throw new EncryptorException("Input data cannot be null");
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
            throw new EncryptorException("Failed to encrypt data", e);
        }
    }

    public String decrypt(String encryptedData) throws EncryptorException {
        if (encryptedData == null || encryptedData.isBlank()) {
            throw new EncryptorException("Encrypted data cannot be null or empty");
        }

        try {
            // Decode the Base64 data
            byte[] combined = Base64.getDecoder().decode(encryptedData);

            // Validate minimum length (IV + at least 1 block of encrypted data)
            if (combined.length < MIN_ENCRYPTED_LENGTH) {
                throw new EncryptorException("Invalid encrypted data: too short");
            }

            // Initialize cipher for decryption
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, new GCMParameterSpec(TAG_LENGTH_BITS, combined, 0, IV_LENGTH));
            byte[] decrypted = cipher.doFinal(combined, IV_LENGTH, combined.length - IV_LENGTH);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Decryption failed: {}", e.getMessage());
            throw new EncryptorException("Failed to decrypt data", e);
        }
    }

    public SecretKey generateKey(int keyLength) throws EncryptorException {
        if (keyLength != 128 && keyLength != 192 && keyLength != 256) {
            throw new EncryptorException("Invalid key length. Must be 128, 192 or 256 bits");
        }
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(KEY_ALGORITHM);
            keyGenerator.init(keyLength, SECURE_RANDOM);
            return keyGenerator.generateKey();
        } catch (GeneralSecurityException e) {
            log.error("Key generation failed: {}", e.getMessage());
            throw new EncryptorException("Failed to generate encryption key", e);
        }
    }

    public String generateKeyAsString(int keyLength) throws EncryptorException {
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
}
