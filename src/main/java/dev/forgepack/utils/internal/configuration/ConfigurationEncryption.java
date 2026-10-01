package dev.forgepack.utils.internal.configuration;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import dev.forgepack.utils.internal.utils.SecretEncryptor;
import org.springframework.beans.factory.annotation.Value;

@AutoConfiguration
public class ConfigurationEncryption {

    @Bean
    @ConditionalOnMissingBean
    public SecretEncryptor secretEncryptor(@Value("${app.encryption.secret}") String secret) {
        return new SecretEncryptor(secret);
    }
}
