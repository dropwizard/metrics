package io.dropwizard.metrics.jvm;

import org.junit.Before;
import org.junit.Test;

import io.dropwizard.metrics.jvm.ClassLoadingGaugeSet;

import io.dropwizard.metrics.Gauge;
import io.dropwizard.metrics.MetricName;

import java.lang.management.ClassLoadingMXBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ClassLoadingGaugeSetTest {

    private final ClassLoadingMXBean cl = mock(ClassLoadingMXBean.class);
    private final ClassLoadingGaugeSet gauges = new ClassLoadingGaugeSet(cl);

    @Before
    public void setUp() throws Exception {
        when(cl.getTotalLoadedClassCount()).thenReturn(2L);
        when(cl.getUnloadedClassCount()).thenReturn(1L);
    }

    @Test
    public void loadedGauge() throws Exception {
        final Gauge gauge = (Gauge) gauges.getMetrics().get(MetricName.build("loaded"));
        assertThat(gauge.getValue()).isEqualTo(2L);
    }

    @Test
    public void unLoadedGauge() throws Exception {
        final Gauge gauge = (Gauge) gauges.getMetrics().get(MetricName.build("unloaded"));
        assertThat(gauge.getValue()).isEqualTo(1L);
    }

}
