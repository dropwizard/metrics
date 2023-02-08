package io.dropwizard.metrics5;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicReference;

class SharedMetricRegistriesTest {

    @BeforeEach
    void setUp() {
        SharedMetricRegistries.setDefaultRegistryName(new AtomicReference<>());
        SharedMetricRegistries.clear();
    }

    @Test
    void memorizesRegistriesByName() {
        final MetricRegistry one = SharedMetricRegistries.getOrCreate("one");
        final MetricRegistry two = SharedMetricRegistries.getOrCreate("one");

        assertThat(one)
                .isSameAs(two);
    }

    @Test
    void hasASetOfNames() {
        SharedMetricRegistries.getOrCreate("one");

        assertThat(SharedMetricRegistries.names())
                .containsOnly("one");
    }

    @Test
    void removesRegistries() {
        final MetricRegistry one = SharedMetricRegistries.getOrCreate("one");
        SharedMetricRegistries.remove("one");

        assertThat(SharedMetricRegistries.names())
                .isEmpty();

        final MetricRegistry two = SharedMetricRegistries.getOrCreate("one");
        assertThat(two)
                .isNotSameAs(one);
    }

    @Test
    void clearsRegistries() {
        SharedMetricRegistries.getOrCreate("one");
        SharedMetricRegistries.getOrCreate("two");

        SharedMetricRegistries.clear();

        assertThat(SharedMetricRegistries.names())
                .isEmpty();
    }

    @Test
    void errorsWhenDefaultUnset() {
        Throwable exception = assertThrows(IllegalStateException.class, () -> {
            SharedMetricRegistries.getDefault();
        });
        assertTrue(exception.getMessage().contains("Default registry name has not been set."));
    }

    @Test
    void createsDefaultRegistries() {
        final String defaultName = "default";
        final MetricRegistry registry = SharedMetricRegistries.setDefault(defaultName);
        assertThat(registry).isNotNull();
        assertThat(SharedMetricRegistries.getDefault()).isEqualTo(registry);
        assertThat(SharedMetricRegistries.getOrCreate(defaultName)).isEqualTo(registry);
    }

    @Test
    void errorsWhenDefaultAlreadySet() {
        Throwable exception = assertThrows(IllegalStateException.class, () -> {
            SharedMetricRegistries.setDefault("foobah");
            SharedMetricRegistries.setDefault("borg");
        });
        assertTrue(exception.getMessage().contains("Default metric registry name is already set."));
    }

    @Test
    void setsDefaultExistingRegistries() {
        final String defaultName = "default";
        final MetricRegistry registry = new MetricRegistry();
        assertThat(SharedMetricRegistries.setDefault(defaultName, registry)).isEqualTo(registry);
        assertThat(SharedMetricRegistries.getDefault()).isEqualTo(registry);
        assertThat(SharedMetricRegistries.getOrCreate(defaultName)).isEqualTo(registry);
    }
}
