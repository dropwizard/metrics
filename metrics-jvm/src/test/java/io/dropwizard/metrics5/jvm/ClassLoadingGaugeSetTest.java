package io.dropwizard.metrics5.jvm;

import io.dropwizard.metrics5.Gauge;
import io.dropwizard.metrics5.MetricName;
import org.junit.Before;
import org.junit.Test;

import java.lang.management.ClassLoadingMXBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("rawtypes")
public class ClassLoadingGaugeSetTest {

    private final ClassLoadingMXBean cl = mock(ClassLoadingMXBean.class);
    private final ClassLoadingGaugeSet gauges = new ClassLoadingGaugeSet(cl);

    @Before
    public void setUp() {
        when(cl.getTotalLoadedClassCount()).thenReturn(2L);
        when(cl.getUnloadedClassCount()).thenReturn(1L);
    }

    @Test
    public void loadedGauge() {
        final Gauge gauge = (Gauge) gauges.getMetrics().get(MetricName.build("loaded"));
        assertThat(gauge.getValue()).isEqualTo(2L);
    }

    @Test
    public void unLoadedGauge() {
        final Gauge gauge = (Gauge) gauges.getMetrics().get(MetricName.build("unloaded"));
        assertThat(gauge.getValue()).isEqualTo(1L);
    }

}
