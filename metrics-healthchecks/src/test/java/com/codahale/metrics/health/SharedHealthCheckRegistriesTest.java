package com.codahale.metrics.health;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

public class SharedHealthCheckRegistriesTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() {
        SharedHealthCheckRegistries.setDefaultRegistryName(new AtomicReference<String>());
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
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Default registry name has not been set.");

        SharedHealthCheckRegistries.getDefault();
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
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Default health check registry is already set.");

        SharedHealthCheckRegistries.setDefault("default");
        SharedHealthCheckRegistries.setDefault("default");
    }

    @Test
    public void unableToSetCustomDefaultRegistryTwice() {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Default health check registry is already set.");

        SharedHealthCheckRegistries.setDefault("default", new HealthCheckRegistry());
        SharedHealthCheckRegistries.setDefault("default", new HealthCheckRegistry());
    }
}
