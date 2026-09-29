package dev.forgepack.utils.internal.configuration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigurationAutomaticTest {

    @Test
    void shouldRegisterForgepackUtilsComponentScan() {
        ConfigurationAutomatic configuration = new ConfigurationAutomatic();

        assertThat(configuration).isNotNull();
        assertThat(ConfigurationAutomatic.class).hasAnnotation(AutoConfiguration.class);
        assertThat(ConfigurationAutomatic.class.getAnnotation(ComponentScan.class).value())
                .containsExactly("dev.forgepack.utils");
    }
}