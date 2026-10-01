package dev.forgepack.utils.internal.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.util.Base64;

import static org.assertj.core.api.Assertions.*;

class SecretEncryptorTest {

    private SecretEncryptor secretEncryptor;

    @BeforeEach
    void setUp() {
        secretEncryptor = new SecretEncryptor("MDEyMzQ1Njc4OUFCQ0RFRjAxMjM0NTY3ODlBQkNERUY=");
    }

    @Test
    void encrypt_shouldReturnBase64EncodedString() throws SecretEncryptor.SecretEncryptorException {
        String result = secretEncryptor.encrypt("hello world");
        assertThat(result).isNotNull().isNotBlank();
        assertThatCode(() -> Base64.getDecoder().decode(result)).doesNotThrowAnyException();
    }

    @Test
    void encryptDecrypt_roundTrip_shouldReturnOriginal() throws SecretEncryptor.SecretEncryptorException {
        String original = "my secret data";
        String decrypted = secretEncryptor.decrypt(secretEncryptor.encrypt(original));
        assertThat(decrypted).isEqualTo(original);
    }

    @Test
    void encrypt_calledTwice_shouldProduceDifferentCiphertexts() throws SecretEncryptor.SecretEncryptorException {
        String data = "same input";
        assertThat(secretEncryptor.encrypt(data)).isNotEqualTo(secretEncryptor.encrypt(data));
    }

    @Test
    void encrypt_withNullData_shouldThrowSecretEncryptorException() {
        assertThatExceptionOfType(SecretEncryptor.SecretEncryptorException.class)
                .isThrownBy(() -> secretEncryptor.encrypt(null))
                .withMessageContaining("null");
    }

    @Test
    void decrypt_withNullData_shouldThrowSecretEncryptorException() {
        assertThatExceptionOfType(SecretEncryptor.SecretEncryptorException.class)
                .isThrownBy(() -> secretEncryptor.decrypt(null));
    }

    @Test
    void decrypt_withEmptyString_shouldThrowSecretEncryptorException() {
        assertThatExceptionOfType(SecretEncryptor.SecretEncryptorException.class)
                .isThrownBy(() -> secretEncryptor.decrypt("   "));
    }

    @Test
    void decrypt_withInvalidBase64_shouldThrowSecretEncryptorException() {
        assertThatExceptionOfType(SecretEncryptor.SecretEncryptorException.class)
                .isThrownBy(() -> secretEncryptor.decrypt("not!!valid!!base64"));
    }

    @Test
    void generateKey_with128Bits_shouldReturnAES128Key() throws SecretEncryptor.SecretEncryptorException {
        SecretKey key = secretEncryptor.generateKey(128);
        assertThat(key.getAlgorithm()).isEqualTo("AES");
        assertThat(key.getEncoded()).hasSize(16);
    }

    @Test
    void generateKey_with192Bits_shouldReturnAES192Key() throws SecretEncryptor.SecretEncryptorException {
        SecretKey key = secretEncryptor.generateKey(192);
        assertThat(key.getEncoded()).hasSize(24);
    }

    @Test
    void generateKey_with256Bits_shouldReturnAES256Key() throws SecretEncryptor.SecretEncryptorException {
        SecretKey key = secretEncryptor.generateKey(256);
        assertThat(key.getEncoded()).hasSize(32);
    }

    @Test
    void generateKey_withInvalidLength_shouldThrowSecretEncryptorException() {
        assertThatExceptionOfType(SecretEncryptor.SecretEncryptorException.class)
                .isThrownBy(() -> secretEncryptor.generateKey(100))
                .withMessageContaining("Invalid key length");
    }

    @Test
    void generateKeyAsString_shouldReturnBase64EncodedKey() throws SecretEncryptor.SecretEncryptorException {
        String keyStr = secretEncryptor.generateKeyAsString(128);
        byte[] decoded = Base64.getDecoder().decode(keyStr);
        assertThat(decoded).hasSize(16);
    }

    @Test
    void isValidEncryptedData_withValidCiphertext_shouldReturnTrue() throws SecretEncryptor.SecretEncryptorException {
        assertThat(secretEncryptor.isValidEncryptedData(secretEncryptor.encrypt("valid"))).isTrue();
    }

    @Test
    void isValidEncryptedData_withGarbage_shouldReturnFalse() {
        assertThat(secretEncryptor.isValidEncryptedData("garbage!!")).isFalse();
    }

    @Test
    void isValidEncryptedData_withNull_shouldReturnFalse() {
        assertThat(secretEncryptor.isValidEncryptedData(null)).isFalse();
    }
}
