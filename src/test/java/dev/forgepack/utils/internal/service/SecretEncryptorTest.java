package dev.forgepack.utils.internal.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.util.Base64;

import static org.assertj.core.api.Assertions.*;

class ServiceSecretEncryptorTest {

    private ServiceSecretEncryptor serviceSecretEncryptor;

    @BeforeEach
    void setUp() {
        serviceSecretEncryptor = new ServiceSecretEncryptor("MDEyMzQ1Njc4OUFCQ0RFRjAxMjM0NTY3ODlBQkNERUY=");
    }

    @Test
    void encrypt_shouldReturnBase64EncodedString() throws ServiceSecretEncryptor.ServiceSecretEncryptorException {
        String result = serviceSecretEncryptor.encrypt("hello world");
        assertThat(result).isNotNull().isNotBlank();
        assertThatCode(() -> Base64.getDecoder().decode(result)).doesNotThrowAnyException();
    }

    @Test
    void encryptDecrypt_roundTrip_shouldReturnOriginal() throws ServiceSecretEncryptor.ServiceSecretEncryptorException {
        String original = "my secret data";
        String decrypted = serviceSecretEncryptor.decrypt(serviceSecretEncryptor.encrypt(original));
        assertThat(decrypted).isEqualTo(original);
    }

    @Test
    void encrypt_calledTwice_shouldProduceDifferentCiphertexts() throws ServiceSecretEncryptor.ServiceSecretEncryptorException {
        String data = "same input";
        assertThat(serviceSecretEncryptor.encrypt(data)).isNotEqualTo(serviceSecretEncryptor.encrypt(data));
    }

    @Test
    void encrypt_withNullData_shouldThrowServiceSecretEncryptorException() {
        assertThatExceptionOfType(ServiceSecretEncryptor.ServiceSecretEncryptorException.class)
                .isThrownBy(() -> serviceSecretEncryptor.encrypt(null))
                .withMessageContaining("null");
    }

    @Test
    void decrypt_withNullData_shouldThrowServiceSecretEncryptorException() {
        assertThatExceptionOfType(ServiceSecretEncryptor.ServiceSecretEncryptorException.class)
                .isThrownBy(() -> serviceSecretEncryptor.decrypt(null));
    }

    @Test
    void decrypt_withEmptyString_shouldThrowServiceSecretEncryptorException() {
        assertThatExceptionOfType(ServiceSecretEncryptor.ServiceSecretEncryptorException.class)
                .isThrownBy(() -> serviceSecretEncryptor.decrypt("   "));
    }

    @Test
    void decrypt_withInvalidBase64_shouldThrowServiceSecretEncryptorException() {
        assertThatExceptionOfType(ServiceSecretEncryptor.ServiceSecretEncryptorException.class)
                .isThrownBy(() -> serviceSecretEncryptor.decrypt("not!!valid!!base64"));
    }

    @Test
    void generateKey_with128Bits_shouldReturnAES128Key() throws ServiceSecretEncryptor.ServiceSecretEncryptorException {
        SecretKey key = serviceSecretEncryptor.generateKey(128);
        assertThat(key.getAlgorithm()).isEqualTo("AES");
        assertThat(key.getEncoded()).hasSize(16);
    }

    @Test
    void generateKey_with192Bits_shouldReturnAES192Key() throws ServiceSecretEncryptor.ServiceSecretEncryptorException {
        SecretKey key = serviceSecretEncryptor.generateKey(192);
        assertThat(key.getEncoded()).hasSize(24);
    }

    @Test
    void generateKey_with256Bits_shouldReturnAES256Key() throws ServiceSecretEncryptor.ServiceSecretEncryptorException {
        SecretKey key = serviceSecretEncryptor.generateKey(256);
        assertThat(key.getEncoded()).hasSize(32);
    }

    @Test
    void generateKey_withInvalidLength_shouldThrowServiceSecretEncryptorException() {
        assertThatExceptionOfType(ServiceSecretEncryptor.ServiceSecretEncryptorException.class)
                .isThrownBy(() -> serviceSecretEncryptor.generateKey(100))
                .withMessageContaining("Invalid key length");
    }

    @Test
    void generateKeyAsString_shouldReturnBase64EncodedKey() throws ServiceSecretEncryptor.ServiceSecretEncryptorException {
        String keyStr = serviceSecretEncryptor.generateKeyAsString(128);
        byte[] decoded = Base64.getDecoder().decode(keyStr);
        assertThat(decoded).hasSize(16);
    }

    @Test
    void isValidEncryptedData_withValidCiphertext_shouldReturnTrue() throws ServiceSecretEncryptor.ServiceSecretEncryptorException {
        assertThat(serviceSecretEncryptor.isValidEncryptedData(serviceSecretEncryptor.encrypt("valid"))).isTrue();
    }

    @Test
    void isValidEncryptedData_withGarbage_shouldReturnFalse() {
        assertThat(serviceSecretEncryptor.isValidEncryptedData("garbage!!")).isFalse();
    }

    @Test
    void isValidEncryptedData_withNull_shouldReturnFalse() {
        assertThat(serviceSecretEncryptor.isValidEncryptedData(null)).isFalse();
    }
}
