package dev.forgepack.utils.internal.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.forgepack.utils.api.exception.EncryptorException;
import javax.crypto.SecretKey;
import java.util.Base64;

import static org.assertj.core.api.Assertions.*;

class EncryptorServiceTest {

    private EncryptorServiceImpl encryptorService;

    @BeforeEach
    void setUp() {
        encryptorService = new EncryptorServiceImpl("MDEyMzQ1Njc4OUFCQ0RFRjAxMjM0NTY3ODlBQkNERUY=");
    }

    @Test
    void encrypt_shouldReturnBase64EncodedString() throws EncryptorException {
        String result = encryptorService.encrypt("hello world");
        assertThat(result).isNotNull().isNotBlank();
        assertThatCode(() -> Base64.getDecoder().decode(result)).doesNotThrowAnyException();
    }

    @Test
    void encryptDecrypt_roundTrip_shouldReturnOriginal() throws EncryptorException {
        String original = "my secret data";
        String decrypted = encryptorService.decrypt(encryptorService.encrypt(original));
        assertThat(decrypted).isEqualTo(original);
    }

    @Test
    void encrypt_calledTwice_shouldProduceDifferentCiphertexts() throws EncryptorException {
        String data = "same input";
        assertThat(encryptorService.encrypt(data)).isNotEqualTo(encryptorService.encrypt(data));
    }

    @Test
    void encrypt_withNullData_shouldThrowEncryptorServiceException() {
        assertThatExceptionOfType(EncryptorException.class)
                .isThrownBy(() -> encryptorService.encrypt(null))
                .withMessageContaining("null");
    }

    @Test
    void decrypt_withNullData_shouldThrowEncryptorServiceException() {
        assertThatExceptionOfType(EncryptorException.class)
                .isThrownBy(() -> encryptorService.decrypt(null));
    }

    @Test
    void decrypt_withEmptyString_shouldThrowEncryptorServiceException() {
        assertThatExceptionOfType(EncryptorException.class)
                .isThrownBy(() -> encryptorService.decrypt("   "));
    }

    @Test
    void decrypt_withInvalidBase64_shouldThrowEncryptorServiceException() {
        assertThatExceptionOfType(EncryptorException.class)
                .isThrownBy(() -> encryptorService.decrypt("not!!valid!!base64"));
    }

    @Test
    void generateKey_with128Bits_shouldReturnAES128Key() throws EncryptorException {
        SecretKey key = encryptorService.generateKey(128);
        assertThat(key.getAlgorithm()).isEqualTo("AES");
        assertThat(key.getEncoded()).hasSize(16);
    }

    @Test
    void generateKey_with192Bits_shouldReturnAES192Key() throws EncryptorException {
        SecretKey key = encryptorService.generateKey(192);
        assertThat(key.getEncoded()).hasSize(24);
    }

    @Test
    void generateKey_with256Bits_shouldReturnAES256Key() throws EncryptorException {
        SecretKey key = encryptorService.generateKey(256);
        assertThat(key.getEncoded()).hasSize(32);
    }

    @Test
    void generateKey_withInvalidLength_shouldThrowEncryptorServiceException() {
        assertThatExceptionOfType(EncryptorException.class)
                .isThrownBy(() -> encryptorService.generateKey(100))
                .withMessageContaining("Invalid key length");
    }

    @Test
    void generateKeyAsString_shouldReturnBase64EncodedKey() throws EncryptorException {
        String keyStr = encryptorService.generateKeyAsString(128);
        byte[] decoded = Base64.getDecoder().decode(keyStr);
        assertThat(decoded).hasSize(16);
    }

    @Test
    void isValidEncryptedData_withValidCiphertext_shouldReturnTrue() throws EncryptorException {
        assertThat(encryptorService.isValidEncryptedData(encryptorService.encrypt("valid"))).isTrue();
    }

    @Test
    void isValidEncryptedData_withGarbage_shouldReturnFalse() {
        assertThat(encryptorService.isValidEncryptedData("garbage!!")).isFalse();
    }

    @Test
    void isValidEncryptedData_withNull_shouldReturnFalse() {
        assertThat(encryptorService.isValidEncryptedData(null)).isFalse();
    }
}
