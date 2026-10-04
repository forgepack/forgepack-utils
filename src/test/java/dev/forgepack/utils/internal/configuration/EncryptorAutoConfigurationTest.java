package dev.forgepack.utils.internal.configuration;

import dev.forgepack.utils.api.service.EncryptorService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;

import static org.assertj.core.api.Assertions.assertThat;

class EncryptorAutoConfigurationTest {

    private static final String ENCRYPTION_SECRET = "MDEyMzQ1Njc4OUFCQ0RFRjAxMjM0NTY3ODlBQkNERUY=";

    @Test
    void encryptorServiceShouldCreateDefaultImplementation() {
        EncryptorService encryptorService = new EncryptorAutoConfiguration()
                .encryptorService(ENCRYPTION_SECRET);

        assertThat(encryptorService).isInstanceOf(EncryptorService.class);
    }

    @Test
    void shouldCreateEncryptorBeanOnlyWhenItIsMissing() throws NoSuchMethodException {
        assertThat(EncryptorAutoConfiguration.class).hasAnnotation(AutoConfiguration.class);
        assertThat(EncryptorAutoConfiguration.class
                .getDeclaredMethod("encryptorService", String.class)
                .getAnnotation(ConditionalOnMissingBean.class))
                .isNotNull();
    }
}