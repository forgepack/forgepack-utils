package dev.forgepack.utils.internal.configuration;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import dev.forgepack.utils.internal.service.ServiceSecretEncryptor;
import org.springframework.beans.factory.annotation.Value;

@AutoConfiguration
public class ConfigurationEncryption {

    @Bean
    @ConditionalOnMissingBean
    public ServiceSecretEncryptor serviceSecretEncryptor(@Value("${app.encryption.secret}") String secret) {
        return new ServiceSecretEncryptor(secret);
    }
}
