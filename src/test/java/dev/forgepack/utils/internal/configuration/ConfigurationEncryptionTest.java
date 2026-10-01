package dev.forgepack.utils.internal.configuration;

import dev.forgepack.utils.internal.service.ServiceSecretEncryptor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigurationEncryptionTest {

    private static final String ENCRYPTION_SECRET = "MDEyMzQ1Njc4OUFCQ0RFRjAxMjM0NTY3ODlBQkNERUY=";

    @Test
    void serviceSecretEncryptorShouldCreateDefaultImplementation() {
        ServiceSecretEncryptor serviceSecretEncryptor = new ConfigurationEncryption()
                .serviceSecretEncryptor(ENCRYPTION_SECRET);

        assertThat(serviceSecretEncryptor).isInstanceOf(ServiceSecretEncryptor.class);
    }

    @Test
    void shouldCreateEncryptionBeanOnlyWhenItIsMissing() throws NoSuchMethodException {
        assertThat(ConfigurationEncryption.class).hasAnnotation(AutoConfiguration.class);
        assertThat(ConfigurationEncryption.class
                .getDeclaredMethod("serviceSecretEncryptor", String.class)
                .getAnnotation(ConditionalOnMissingBean.class))
                .isNotNull();
    }
}