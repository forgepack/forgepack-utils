package dev.forgepack.utils.internal.configuration;

import dev.forgepack.utils.api.service.EmailService;
import dev.forgepack.utils.internal.service.EmailServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.mail.autoconfigure.MailSenderAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class EmailAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    EmailAutoConfiguration.class,
                    MailSenderAutoConfiguration.class
            ));

    @Test
    void serviceEmailShouldCreateDefaultImplementation() {
        JavaMailSender mailSender = mock(JavaMailSender.class);

        EmailService service = new EmailAutoConfiguration().serviceEmail(mailSender);

        assertThat(service).isInstanceOf(EmailServiceImpl.class);
    }

    @Test
    void shouldRegisterEmailServiceImplementationWhenMailSenderIsConfigured() {
        contextRunner
                .withPropertyValues("spring.mail.host=localhost")
                .run(context -> {
                    assertThat(context).hasSingleBean(JavaMailSender.class);
                    assertThat(context).hasSingleBean(EmailService.class);
                    assertThat(context).hasSingleBean(EmailServiceImpl.class);
                });
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