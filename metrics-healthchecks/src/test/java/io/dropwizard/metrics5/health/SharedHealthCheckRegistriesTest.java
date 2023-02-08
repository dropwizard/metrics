package io.dropwizard.metrics5.health;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SharedHealthCheckRegistriesTest {

    @BeforeEach
    void setUp() {
        SharedHealthCheckRegistries.setDefaultRegistryName(new AtomicReference<>());
        SharedHealthCheckRegistries.clear();
    }

    @Test
    void savesCreatedRegistry() {
        final HealthCheckRegistry one = SharedHealthCheckRegistries.getOrCreate("db");
        final HealthCheckRegistry two = SharedHealthCheckRegistries.getOrCreate("db");

        assertThat(one).isSameAs(two);
    }

    @Test
    void returnsSetOfCreatedRegistries() {
        SharedHealthCheckRegistries.getOrCreate("db");

        assertThat(SharedHealthCheckRegistries.names()).containsOnly("db");
    }

    @Test
    void registryCanBeRemoved() {
        final HealthCheckRegistry first = SharedHealthCheckRegistries.getOrCreate("db");
        SharedHealthCheckRegistries.remove("db");

        assertThat(SharedHealthCheckRegistries.names()).isEmpty();
        assertThat(SharedHealthCheckRegistries.getOrCreate("db")).isNotEqualTo(first);
    }

    @Test
    void registryCanBeCleared() {
        SharedHealthCheckRegistries.getOrCreate("db");
        SharedHealthCheckRegistries.getOrCreate("web");

        SharedHealthCheckRegistries.clear();

        assertThat(SharedHealthCheckRegistries.names()).isEmpty();
    }

    @Test
    void defaultRegistryIsNotSetByDefault() {
        Throwable exception = assertThrows(IllegalStateException.class, () -> {

            SharedHealthCheckRegistries.getDefault();
        });
        assertTrue(exception.getMessage().contains("Default registry name has not been set."));
    }

    @Test
    void defaultRegistryCanBeSet() {
        HealthCheckRegistry registry = SharedHealthCheckRegistries.setDefault("default");

        assertThat(SharedHealthCheckRegistries.getDefault()).isEqualTo(registry);
    }

    @Test
    void specificRegistryCanBeSetAsDefault() {
        HealthCheckRegistry registry = new HealthCheckRegistry();
        SharedHealthCheckRegistries.setDefault("default", registry);

        assertThat(SharedHealthCheckRegistries.getDefault()).isEqualTo(registry);
    }

    @Test
    void unableToSetDefaultRegistryTwice() {
        Throwable exception = assertThrows(IllegalStateException.class, () -> {

            SharedHealthCheckRegistries.setDefault("default");
            SharedHealthCheckRegistries.setDefault("default");
        });
        assertTrue(exception.getMessage().contains("Default health check registry is already set."));
    }

    @Test
    void unableToSetCustomDefaultRegistryTwice() {
        Throwable exception = assertThrows(IllegalStateException.class, () -> {

            SharedHealthCheckRegistries.setDefault("default", new HealthCheckRegistry());
            SharedHealthCheckRegistries.setDefault("default", new HealthCheckRegistry());
        });
        assertTrue(exception.getMessage().contains("Default health check registry is already set."));
    }
}
