package com.codahale.metrics.jvm;

import com.codahale.metrics.Gauge;
import org.junit.Before;
import org.junit.Test;

import java.lang.management.GarbageCollectorMXBean;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
public class GarbageCollectorMetricSetTest {
    private final GarbageCollectorMXBean gc = mock(GarbageCollectorMXBean.class);
    private final GarbageCollectorMetricSet metrics = new GarbageCollectorMetricSet(Collections.singletonList(gc));

    @Before
    public void setUp() {
        when(gc.getName()).thenReturn("PS OldGen");
        when(gc.getCollectionCount()).thenReturn(1L);
        when(gc.getCollectionTime()).thenReturn(2L);
    }

    @Test
    public void hasGaugesForGcCountsAndElapsedTimes() {
        assertThat(metrics.getMetrics().keySet())
                .containsOnly("PS-OldGen.time", "PS-OldGen.count");
    }

    @Test
    public void hasAGaugeForGcCounts() {
        final Gauge<Long> gauge = (Gauge<Long>) metrics.getMetrics().get("PS-OldGen.count");
        assertThat(gauge.getValue())
                .isEqualTo(1L);
    }

    @Test
    public void hasAGaugeForGcTimes() {
        final Gauge<Long> gauge = (Gauge<Long>) metrics.getMetrics().get("PS-OldGen.time");
        assertThat(gauge.getValue())
                .isEqualTo(2L);
    }

    @Test
    public void autoDiscoversGCs() {
        assertThat(new GarbageCollectorMetricSet().getMetrics().keySet())
                .isNotEmpty();
    }
}
