package dev.forgepack.utils.internal.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Base64;

import static org.assertj.core.api.Assertions.*;

class E2EETest {

    private E2EE e2ee;

    @BeforeEach
    void setUp() {
        e2ee = new E2EE();
        ReflectionTestUtils.setField(e2ee, "configuredSecretKey", "testSecretKey1234567890123456");
    }

    @Test
    void encrypt_shouldReturnBase64EncodedString() throws E2EE.E2EEException {
        String result = e2ee.encrypt("hello world");
        assertThat(result).isNotNull().isNotBlank();
        assertThatCode(() -> Base64.getDecoder().decode(result)).doesNotThrowAnyException();
    }

    @Test
    void encryptDecrypt_roundTrip_shouldReturnOriginal() throws E2EE.E2EEException {
        String original = "my secret data";
        String decrypted = e2ee.decrypt(e2ee.encrypt(original));
        assertThat(decrypted).isEqualTo(original);
    }

    @Test
    void encrypt_calledTwice_shouldProduceDifferentCiphertexts() throws E2EE.E2EEException {
        String data = "same input";
        assertThat(e2ee.encrypt(data)).isNotEqualTo(e2ee.encrypt(data));
    }

    @Test
    void encrypt_withNullData_shouldThrowE2EEException() {
        assertThatExceptionOfType(E2EE.E2EEException.class)
                .isThrownBy(() -> e2ee.encrypt(null))
                .withMessageContaining("null");
    }

    @Test
    void decrypt_withNullData_shouldThrowE2EEException() {
        assertThatExceptionOfType(E2EE.E2EEException.class)
                .isThrownBy(() -> e2ee.decrypt(null));
    }

    @Test
    void decrypt_withEmptyString_shouldThrowE2EEException() {
        assertThatExceptionOfType(E2EE.E2EEException.class)
                .isThrownBy(() -> e2ee.decrypt("   "));
    }

    @Test
    void decrypt_withInvalidBase64_shouldThrowE2EEException() {
        assertThatExceptionOfType(E2EE.E2EEException.class)
                .isThrownBy(() -> e2ee.decrypt("not!!valid!!base64"));
    }

    @Test
    void generateKey_with128Bits_shouldReturnAES128Key() throws E2EE.E2EEException {
        SecretKey key = e2ee.generateKey(128);
        assertThat(key.getAlgorithm()).isEqualTo("AES");
        assertThat(key.getEncoded()).hasSize(16);
    }

    @Test
    void generateKey_with192Bits_shouldReturnAES192Key() throws E2EE.E2EEException {
        SecretKey key = e2ee.generateKey(192);
        assertThat(key.getEncoded()).hasSize(24);
    }

    @Test
    void generateKey_with256Bits_shouldReturnAES256Key() throws E2EE.E2EEException {
        SecretKey key = e2ee.generateKey(256);
        assertThat(key.getEncoded()).hasSize(32);
    }

    @Test
    void generateKey_withInvalidLength_shouldThrowE2EEException() {
        assertThatExceptionOfType(E2EE.E2EEException.class)
                .isThrownBy(() -> e2ee.generateKey(100))
                .withMessageContaining("Invalid key length");
    }

    @Test
    void generateKeyAsString_shouldReturnBase64EncodedKey() throws E2EE.E2EEException {
        String keyStr = e2ee.generateKeyAsString(128);
        byte[] decoded = Base64.getDecoder().decode(keyStr);
        assertThat(decoded).hasSize(16);
    }

    @Test
    void isValidEncryptedData_withValidCiphertext_shouldReturnTrue() throws E2EE.E2EEException {
        assertThat(e2ee.isValidEncryptedData(e2ee.encrypt("valid"))).isTrue();
    }

    @Test
    void isValidEncryptedData_withGarbage_shouldReturnFalse() {
        assertThat(e2ee.isValidEncryptedData("garbage!!")).isFalse();
    }

    @Test
    void isValidEncryptedData_withNull_shouldReturnFalse() {
        assertThat(e2ee.isValidEncryptedData(null)).isFalse();
    }
}
