package dev.forgepack.utils.internal.configuration;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;

import dev.forgepack.utils.api.service.ServiceEmail;
import dev.forgepack.utils.internal.service.ServiceEmailImpl;

@AutoConfiguration
@ConditionalOnBean(JavaMailSender.class)
public class ConfigurationEmail {
    
    @Bean
    @ConditionalOnMissingBean(ServiceEmail.class)
    public ServiceEmail serviceEmail(JavaMailSender mailSender) {
        return new ServiceEmailImpl(mailSender);
    }
}
