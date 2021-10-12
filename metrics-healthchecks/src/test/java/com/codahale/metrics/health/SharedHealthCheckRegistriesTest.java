package com.codahale.metrics.health;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

public class SharedHealthCheckRegistriesTest {

    @BeforeEach
    public void setUp() {
        SharedHealthCheckRegistries.setDefaultRegistryName(new AtomicReference<>());
        SharedHealthCheckRegistries.clear();
    }

    @Test
    public void savesCreatedRegistry() {
        final HealthCheckRegistry one = SharedHealthCheckRegistries.getOrCreate("db");
        final HealthCheckRegistry two = SharedHealthCheckRegistries.getOrCreate("db");

        assertThat(one).isSameAs(two);
    }

    @Test
    public void returnsSetOfCreatedRegistries() {
        SharedHealthCheckRegistries.getOrCreate("db");

        assertThat(SharedHealthCheckRegistries.names()).containsOnly("db");
    }

    @Test
    public void registryCanBeRemoved() {
        final HealthCheckRegistry first = SharedHealthCheckRegistries.getOrCreate("db");
        SharedHealthCheckRegistries.remove("db");

        assertThat(SharedHealthCheckRegistries.names()).isEmpty();
        assertThat(SharedHealthCheckRegistries.getOrCreate("db")).isNotEqualTo(first);
    }

    @Test
    public void registryCanBeCleared() {
        SharedHealthCheckRegistries.getOrCreate("db");
        SharedHealthCheckRegistries.getOrCreate("web");

        SharedHealthCheckRegistries.clear();

        assertThat(SharedHealthCheckRegistries.names()).isEmpty();
    }

    @Test
    public void defaultRegistryIsNotSetByDefault() {
        assertThatIllegalStateException().isThrownBy(SharedHealthCheckRegistries::getDefault)
                .withMessage("Default registry name has not been set.");
    }

    @Test
    public void defaultRegistryCanBeSet() {
        HealthCheckRegistry registry = SharedHealthCheckRegistries.setDefault("default");

        assertThat(SharedHealthCheckRegistries.getDefault()).isEqualTo(registry);
    }

    @Test
    public void specificRegistryCanBeSetAsDefault() {
        HealthCheckRegistry registry = new HealthCheckRegistry();
        SharedHealthCheckRegistries.setDefault("default", registry);

        assertThat(SharedHealthCheckRegistries.getDefault()).isEqualTo(registry);
    }

    @Test
    public void unableToSetDefaultRegistryTwice() {
        assertThatIllegalStateException().isThrownBy(() -> {
            SharedHealthCheckRegistries.setDefault("default");
            SharedHealthCheckRegistries.setDefault("default");
        }).withMessage("Default health check registry is already set.");
    }

    @Test
    public void unableToSetCustomDefaultRegistryTwice() {
        assertThatIllegalStateException().isThrownBy(() -> {
            SharedHealthCheckRegistries.setDefault("default", new HealthCheckRegistry());
            SharedHealthCheckRegistries.setDefault("default", new HealthCheckRegistry());
        }).withMessage("Default health check registry is already set.");
    }
}
