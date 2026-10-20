package dev.forgepack.utils.internal.configuration;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.mail.autoconfigure.MailSenderAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;

import dev.forgepack.utils.api.service.EmailService;
import dev.forgepack.utils.internal.service.EmailServiceImpl;

@AutoConfiguration(after = MailSenderAutoConfiguration.class)
@ConditionalOnBean(JavaMailSender.class)
public class EmailAutoConfiguration {
    
    @Bean
    @ConditionalOnMissingBean(EmailService.class)
    public EmailServiceImpl serviceEmail(JavaMailSender mailSender) {
        return new EmailServiceImpl(mailSender);
    }
}
