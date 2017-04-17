package io.dropwizard.metrics;

import org.junit.Before;
import org.junit.Test;

import io.dropwizard.metrics.MetricRegistry;
import io.dropwizard.metrics.SharedMetricRegistries;

import static org.assertj.core.api.Assertions.assertThat;

public class SharedMetricRegistriesTest {
    @Before
    public void setUp() throws Exception {
        SharedMetricRegistries.clear();
    }

    @Test
    public void memorizesRegistriesByName() throws Exception {
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
    public void defaultRegistry () {
        // Verify that an error is thrown when default is not set
        try {
            SharedMetricRegistries.getDefault();
        } catch (final Exception e) {
            assertThat(e).isInstanceOf(IllegalStateException.class);
            assertThat(e.getMessage()).isEqualTo("Default registry has not been set.");
        }
    
        // Verify that default can be set
        final MetricRegistry registry = new MetricRegistry();
        SharedMetricRegistries.setDefault(registry);
        assertThat(SharedMetricRegistries.getDefault()).isEqualTo(registry);
    
        // Verify that default can not be set again
        try {
            SharedMetricRegistries.setDefault(new MetricRegistry());
        } catch (final Exception e) {
            assertThat(e).isInstanceOf(IllegalStateException.class);
            assertThat(e.getMessage()).isEqualTo("Default registry has already been set.");
        }
    }
}
