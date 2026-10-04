package dev.forgepack.utils.api.service;

import javax.crypto.SecretKey;
import dev.forgepack.utils.api.exception.EncryptorException;

/**
 * End-to-End Encryption (EncryptorService) utility class using AES encryption with secure practices.
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
public interface EncryptorService {

    /**
     * @param base64Key chave AES em Base64 (16, 24 ou 32 bytes após decodificar)
     */

    /**
    * Encrypts data using AES-GCM with a random IV for each operation.
     * The IV is prepended to the encrypted data for decryption.
     *
     * @param data The plaintext data to encrypt
     * @return Base64-encoded string containing IV + encrypted data
     * @throws EncryptorException if encryption fails
     */
    public String encrypt(String data) throws EncryptorException;
    
    /**
     * Decrypts data that was encrypted with the encrypt method.
     * Extracts the IV from the beginning of the encrypted data.
     *
     * @param encryptedData Base64-encoded string containing IV + encrypted data
     * @return The original plaintext data
     * @throws EncryptorException if decryption fails
     */
    public String decrypt(String encryptedData) throws EncryptorException;
    
    /**
     * Generates a new AES secret key with the specified key length.
     *
     * @param keyLength Key length in bits (128, 192, or 256)
     * @return A new SecretKey for AES encryption
     * @throws EncryptorException if key generation fails
     */
    public SecretKey generateKey(int keyLength) throws EncryptorException;

    /**
     * Generates a random secure key for encryption as Base64 string.
     * Useful for generating new keys to be stored in configuration at app.encryption.secret.
     *
     * @param keyLength Key length in bits
     * @return Base64-encoded random key
     * @throws EncryptorException if key generation fails
     */
    public String generateKeyAsString(int keyLength) throws EncryptorException;

    /**
     * Validates if the provided encrypted data can be decrypted successfully.
     *
     * @param encryptedData The encrypted data to validate
     * @return true if data can be decrypted, false otherwise
     */
    public boolean isValidEncryptedData(String encryptedData);
}
