package dev.forgepack.utils.internal.configuration;

import dev.forgepack.utils.api.service.EmailService;
import dev.forgepack.utils.internal.service.EmailServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class EmailAutoConfigurationTest {

    @Test
    void serviceEmailShouldCreateDefaultImplementation() {
        JavaMailSender mailSender = mock(JavaMailSender.class);

        EmailService service = new EmailAutoConfiguration().serviceEmail(mailSender);

        assertThat(service).isInstanceOf(EmailServiceImpl.class);
    }

    @Test
    void shouldCreateBeanOnlyWhenMailSenderExistsAndServiceIsMissing() throws NoSuchMethodException {
        assertThat(EmailAutoConfiguration.class).hasAnnotation(AutoConfiguration.class);
        assertThat(EmailAutoConfiguration.class).hasAnnotation(ConditionalOnBean.class);
        assertThat(EmailAutoConfiguration.class.getAnnotation(ConditionalOnBean.class).value())
                .containsExactly(JavaMailSender.class);
        assertThat(EmailAutoConfiguration.class
            .getDeclaredMethod("serviceEmail", JavaMailSender.class)
            .getAnnotation(ConditionalOnMissingBean.class))
            .isNotNull();
    }
}