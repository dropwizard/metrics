package com.codahale.metrics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

public class SharedMetricRegistriesTest {

    @BeforeEach
    public void setUp() {
        SharedMetricRegistries.setDefaultRegistryName(new AtomicReference<>());
        SharedMetricRegistries.clear();
    }

    @Test
    public void memorizesRegistriesByName() {
        final MetricRegistry one = SharedMetricRegistries.getOrCreate("one");
        final MetricRegistry two = SharedMetricRegistries.getOrCreate("one");

        assertThat(one)
                .isSameAs(two);
    }

    @Test
    public void hasASetOfNames() {
        SharedMetricRegistries.getOrCreate("one");

        assertThat(SharedMetricRegistries.names())
                .containsOnly("one");
    }

    @Test
    public void removesRegistries() {
        final MetricRegistry one = SharedMetricRegistries.getOrCreate("one");
        SharedMetricRegistries.remove("one");

        assertThat(SharedMetricRegistries.names())
                .isEmpty();

        final MetricRegistry two = SharedMetricRegistries.getOrCreate("one");
        assertThat(two)
                .isNotSameAs(one);
    }

    @Test
    public void clearsRegistries() {
        SharedMetricRegistries.getOrCreate("one");
        SharedMetricRegistries.getOrCreate("two");

        SharedMetricRegistries.clear();

        assertThat(SharedMetricRegistries.names())
                .isEmpty();
    }

    @Test
    public void errorsWhenDefaultUnset() {
        assertThatIllegalStateException().isThrownBy(SharedMetricRegistries::getDefault)
                .withMessage("Default registry name has not been set.");
    }

    @Test
    public void createsDefaultRegistries() {
        final String defaultName = "default";
        final MetricRegistry registry = SharedMetricRegistries.setDefault(defaultName);
        assertThat(registry).isNotNull();
        assertThat(SharedMetricRegistries.getDefault()).isEqualTo(registry);
        assertThat(SharedMetricRegistries.getOrCreate(defaultName)).isEqualTo(registry);
    }

    @Test
    public void errorsWhenDefaultAlreadySet() {
        SharedMetricRegistries.setDefault("foobah");
        assertThatIllegalStateException().isThrownBy(() -> SharedMetricRegistries.setDefault("borg"))
                .withMessage("Default metric registry name is already set.");
    }

    @Test
    public void setsDefaultExistingRegistries() {
        final String defaultName = "default";
        final MetricRegistry registry = new MetricRegistry();
        assertThat(SharedMetricRegistries.setDefault(defaultName, registry)).isEqualTo(registry);
        assertThat(SharedMetricRegistries.getDefault()).isEqualTo(registry);
        assertThat(SharedMetricRegistries.getOrCreate(defaultName)).isEqualTo(registry);
    }
}
