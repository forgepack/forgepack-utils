package dev.forgepack.utils.internal.configuration;

import dev.forgepack.utils.api.service.ServiceEmail;
import dev.forgepack.utils.internal.service.ServiceEmailImpl;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class ConfigurationEmailTest {

    @Test
    void serviceEmailShouldCreateDefaultImplementation() {
        JavaMailSender mailSender = mock(JavaMailSender.class);

        ServiceEmail service = new ConfigurationEmail().serviceEmail(mailSender);

        assertThat(service).isInstanceOf(ServiceEmailImpl.class);
    }

    @Test
    void shouldCreateBeanOnlyWhenMailSenderExistsAndServiceIsMissing() throws NoSuchMethodException {
        assertThat(ConfigurationEmail.class).hasAnnotation(AutoConfiguration.class);
        assertThat(ConfigurationEmail.class).hasAnnotation(ConditionalOnBean.class);
        assertThat(ConfigurationEmail.class.getAnnotation(ConditionalOnBean.class).value())
                .containsExactly(JavaMailSender.class);
        assertThat(ConfigurationEmail.class
            .getDeclaredMethod("serviceEmail", JavaMailSender.class)
            .getAnnotation(ConditionalOnMissingBean.class))
            .isNotNull();
    }
}