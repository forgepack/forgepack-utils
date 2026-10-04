package dev.forgepack.utils.internal.configuration;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import dev.forgepack.utils.internal.service.EncryptorServiceImpl;
import org.springframework.beans.factory.annotation.Value;

@AutoConfiguration
public class EncryptorAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public EncryptorServiceImpl encryptorService(@Value("${app.encryptor.secret}") String secret) {
        return new EncryptorServiceImpl(secret);
    }
}
