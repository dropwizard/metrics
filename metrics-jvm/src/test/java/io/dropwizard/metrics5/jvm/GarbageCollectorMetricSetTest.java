package io.dropwizard.metrics5.jvm;

import io.dropwizard.metrics5.Gauge;
import io.dropwizard.metrics5.MetricName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.management.GarbageCollectorMXBean;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
class GarbageCollectorMetricSetTest {
    private final GarbageCollectorMXBean gc = mock(GarbageCollectorMXBean.class);
    private final GarbageCollectorMetricSet metrics = new GarbageCollectorMetricSet(Collections.singletonList(gc));

    private static final MetricName PS_OLDGEN_TIME = MetricName.build("PS-OldGen.time");
    private static final MetricName PS_OLDGEN_COUNT = MetricName.build("PS-OldGen.count");

    @BeforeEach
    void setUp() {
        when(gc.getName()).thenReturn("PS OldGen");
        when(gc.getCollectionCount()).thenReturn(1L);
        when(gc.getCollectionTime()).thenReturn(2L);
    }

    @Test
    void hasGaugesForGcCountsAndElapsedTimes() {
        assertThat(metrics.getMetrics().keySet())
                .containsOnly(PS_OLDGEN_TIME, PS_OLDGEN_COUNT);
    }

    @Test
    void hasAGaugeForGcCounts() {
        final Gauge<Long> gauge = (Gauge<Long>) metrics.getMetrics().get(PS_OLDGEN_COUNT);
        assertThat(gauge.getValue())
                .isEqualTo(1L);
    }

    @Test
    void hasAGaugeForGcTimes() {
        final Gauge<Long> gauge = (Gauge<Long>) metrics.getMetrics().get(PS_OLDGEN_TIME);
        assertThat(gauge.getValue())
                .isEqualTo(2L);
    }

    @Test
    void autoDiscoversGCs() {
        assertThat(new GarbageCollectorMetricSet().getMetrics().keySet())
                .isNotEmpty();
    }
}
