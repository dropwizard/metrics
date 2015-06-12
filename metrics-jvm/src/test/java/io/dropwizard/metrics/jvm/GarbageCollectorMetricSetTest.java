package io.dropwizard.metrics.jvm;

import org.junit.Before;
import org.junit.Test;

import io.dropwizard.metrics.jvm.GarbageCollectorMetricSet;

import io.dropwizard.metrics.Gauge;
import io.dropwizard.metrics.MetricName;

import java.lang.management.GarbageCollectorMXBean;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GarbageCollectorMetricSetTest {
    private final GarbageCollectorMXBean gc = mock(GarbageCollectorMXBean.class);
    private final GarbageCollectorMetricSet metrics = new GarbageCollectorMetricSet(Arrays.asList(gc));

    private static final MetricName PS_OLDGEN_TIME = MetricName.build("PS-OldGen.time");
    private static final MetricName PS_OLDGEN_COUNT = MetricName.build("PS-OldGen.count");
    
    @Before
    public void setUp() throws Exception {
        when(gc.getName()).thenReturn("PS OldGen");
        when(gc.getCollectionCount()).thenReturn(1L);
        when(gc.getCollectionTime()).thenReturn(2L);
    }

    @Test
    public void hasGaugesForGcCountsAndElapsedTimes() throws Exception {
        assertThat(metrics.getMetrics().keySet())
                .containsOnly(PS_OLDGEN_TIME, PS_OLDGEN_COUNT);
    }

    @Test
    public void hasAGaugeForGcCounts() throws Exception {
        final Gauge gauge = (Gauge) metrics.getMetrics().get(PS_OLDGEN_COUNT);
        assertThat(gauge.getValue())
                .isEqualTo(1L);
    }

    @Test
    public void hasAGaugeForGcTimes() throws Exception {
        final Gauge gauge = (Gauge) metrics.getMetrics().get(PS_OLDGEN_TIME);
        assertThat(gauge.getValue())
                .isEqualTo(2L);
    }

    @Test
    public void autoDiscoversGCs() throws Exception {
        assertThat(new GarbageCollectorMetricSet().getMetrics().keySet())
                .isNotEmpty();
    }
}
