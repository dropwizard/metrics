package com.yammer.metrics.jvm.tests;

import com.yammer.metrics.Gauge;
import com.yammer.metrics.jvm.GarbageCollectorMetricSet;
import org.junit.Before;
import org.junit.Test;

import java.lang.management.GarbageCollectorMXBean;
import java.util.Arrays;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GarbageCollectorMetricSetTest {
    private final GarbageCollectorMXBean gc = mock(GarbageCollectorMXBean.class);
    private final GarbageCollectorMetricSet metrics = new GarbageCollectorMetricSet(Arrays.asList(gc));

    @Before
    public void setUp() throws Exception {
        when(gc.getName()).thenReturn("PS OldGen");
        when(gc.getCollectionCount()).thenReturn(1L);
        when(gc.getCollectionTime()).thenReturn(2L);
    }

    @Test
    public void hasGaugesForGcCountsAndElapsedTimes() throws Exception {
        assertThat(metrics.getMetrics().keySet())
                .containsOnly("PS-OldGen.time", "PS-OldGen.count");
    }

    @Test
    public void hasAGaugeForGcCounts() throws Exception {
        final Gauge gauge = (Gauge) metrics.getMetrics().get("PS-OldGen.count");
        assertThat(gauge.getValue())
                .isEqualTo(1L);
    }

    @Test
    public void hasAGaugeForGcTimes() throws Exception {
        final Gauge gauge = (Gauge) metrics.getMetrics().get("PS-OldGen.time");
        assertThat(gauge.getValue())
                .isEqualTo(2L);
    }

    @Test
    public void autoDiscoversGCs() throws Exception {
        assertThat(new GarbageCollectorMetricSet().getMetrics().keySet())
                .isNotEmpty();
    }
}
