package com.codahale.metrics;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class SharedMetricRegistriesTest {
    @Before
    public void setUp() throws Exception {
        // Unset the defaultRegistryName field between tests for better isolation.
        final Field field = SharedMetricRegistries.class.getDeclaredField("defaultRegistryName");
        field.setAccessible(true);
        final Field modfiers = Field.class.getDeclaredField("modifiers");
        modfiers.setAccessible(true);
        modfiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(null, null);
        SharedMetricRegistries.clear();
    }

    @Test
    public void memoizesRegistriesByName() throws Exception {
        final MetricRegistry one = SharedMetricRegistries.getOrCreate("one");
        final MetricRegistry two = SharedMetricRegistries.getOrCreate("one");

        assertThat(one)
                .isSameAs(two);
    }

    @Test
    public void hasASetOfNames() throws Exception {
        SharedMetricRegistries.getOrCreate("one");

        assertThat(SharedMetricRegistries.names())
                .containsOnly("one");
    }

    @Test
    public void removesRegistries() throws Exception {
        final MetricRegistry one = SharedMetricRegistries.getOrCreate("one");
        SharedMetricRegistries.remove("one");

        assertThat(SharedMetricRegistries.names())
                .isEmpty();

        final MetricRegistry two = SharedMetricRegistries.getOrCreate("one");
        assertThat(two)
                .isNotSameAs(one);
    }

    @Test
    public void clearsRegistries() throws Exception {
        SharedMetricRegistries.getOrCreate("one");
        SharedMetricRegistries.getOrCreate("two");

        SharedMetricRegistries.clear();

        assertThat(SharedMetricRegistries.names())
                .isEmpty();
    }

    @Test
    public void errorsWhenDefaultUnset() throws Exception {
        try {
            SharedMetricRegistries.getDefault();
        } catch (final Exception e) {
            assertThat(e).isInstanceOf(IllegalStateException.class);
            assertThat(e.getMessage()).isEqualTo("Default registry name has not been set.");
        }
    }

    @Test
    public void createsDefaultRegistries() throws Exception {
        final String defaultName = "default";
        final MetricRegistry registry = SharedMetricRegistries.setDefault(defaultName);
        assertThat(registry).isNotNull();
        assertThat(SharedMetricRegistries.getDefault()).isEqualTo(registry);
        assertThat(SharedMetricRegistries.getOrCreate(defaultName)).isEqualTo(registry);
    }

    @Test
    public void errorsWhenDefaultAlreadySet() throws Exception {
        try {
            SharedMetricRegistries.setDefault("foobah");
            SharedMetricRegistries.setDefault("borg");
        } catch (final Exception e) {
            assertThat(e).isInstanceOf(IllegalStateException.class);
            assertThat(e.getMessage()).isEqualTo("Default metric registry name is already set.");
        }
    }
}
