package com.codahale.metrics;

import org.junit.After;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

@SuppressWarnings("deprecation")
public class SharedMetricRegistriesTest {

    @After
    public void tearDown() throws Exception {
        SharedMetricRegistries.clear();
    }

    @Test
    public void testGetOrCreateMetricRegistry() {
        SharedMetricRegistries.getOrCreate("get-or-create").counter("test-counter");

        assertThat(SharedMetricRegistries.getOrCreate("get-or-create").getCounters())
                .containsOnlyKeys("test-counter");
    }

    @Test
    public void testAddMetricRegistry() {
        MetricRegistry metricRegistry = new MetricRegistry();
        metricRegistry.histogram("test-histogram");
        SharedMetricRegistries.add("add", metricRegistry);

        assertThat(SharedMetricRegistries.getOrCreate("add").getHistograms())
                .containsOnlyKeys("test-histogram");
    }

    @Test
    public void testNames() {
        SharedMetricRegistries.add("registry-1", new MetricRegistry());
        SharedMetricRegistries.add("registry-2", new MetricRegistry());
        SharedMetricRegistries.add("registry-3", new MetricRegistry());

        assertThat(SharedMetricRegistries.names()).containsOnly("registry-1", "registry-2", "registry-3");
    }

    @Test
    public void testTryGetDefaultRegistry() {
        assertThat(SharedMetricRegistries.tryGetDefault()).isNull();
    }

    @Test
    public void testGetDefaultRegistry() {
        assertThatIllegalStateException().isThrownBy(SharedMetricRegistries::getDefault);
    }
}
