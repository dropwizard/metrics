package com.codahale.metrics.jvm;

import com.codahale.metrics.Gauge;
import org.junit.Before;
import org.junit.Test;

import com.sun.management.OperatingSystemMXBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("rawtypes")
public class CpuUsageGaugeSetTest {
    private final OperatingSystemMXBean oxMxBean = mock(OperatingSystemMXBean.class);
    private final CpuUsageGaugeSet gauges = new CpuUsageGaugeSet(oxMxBean);
    
    @Before
    public void setUp() {
        when(oxMxBean.getProcessCpuLoad()).thenReturn(10d);
        when(oxMxBean.getSystemCpuLoad()).thenReturn(20d);
        when(oxMxBean.getSystemLoadAverage()).thenReturn(30d);
        when(oxMxBean.getProcessCpuTime()).thenReturn(40L);
    }

    @Test
    public void hasASetOfGauges() {
        assertThat(gauges.getMetrics().keySet())
                .containsOnly(
                        "process-cpu-load-percentage",
                        "system-cpu-load-percentage",
                        "system-load-average",
                        "process-cpu-time");
    }

    @Test
    public void hasAGaugeForProcessCpuLoadPercentage() {
        final Gauge gauge = (Gauge) gauges.getMetrics().get("process-cpu-load-percentage");

        assertThat(gauge.getValue())
                .isEqualTo(10d);
    }
    
    @Test
    public void hasAGaugeForSystemCpuLoadPercentage() {
        final Gauge gauge = (Gauge) gauges.getMetrics().get("system-cpu-load-percentage");

        assertThat(gauge.getValue())
                .isEqualTo(20d);
    }
    
    @Test
    public void hasAGaugeForSystemLoadAverage() {
        final Gauge gauge = (Gauge) gauges.getMetrics().get("system-load-average");

        assertThat(gauge.getValue())
                .isEqualTo(30d);
    }
    
    @Test
    public void hasAGaugeForProcessCpuTime() {
        final Gauge gauge = (Gauge) gauges.getMetrics().get("process-cpu-time");

        assertThat(gauge.getValue())
                .isEqualTo(40L);
    }
}
