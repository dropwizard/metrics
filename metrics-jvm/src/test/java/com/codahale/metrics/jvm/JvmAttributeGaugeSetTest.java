package com.codahale.metrics.jvm;

import com.codahale.metrics.Gauge;
import org.junit.Before;
import org.junit.Test;

import java.lang.management.RuntimeMXBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
public class JvmAttributeGaugeSetTest {
    private final RuntimeMXBean runtime = mock(RuntimeMXBean.class);
    private final JvmAttributeGaugeSet gauges = new JvmAttributeGaugeSet(runtime);

    @Before
    public void setUp() {
        when(runtime.getName()).thenReturn("9928@example.com");

        when(runtime.getVmVendor()).thenReturn("Oracle Corporation");
        when(runtime.getVmName()).thenReturn("Java HotSpot(TM) 64-Bit Server VM");
        when(runtime.getVmVersion()).thenReturn("23.7-b01");
        when(runtime.getSpecVersion()).thenReturn("1.7");
        when(runtime.getUptime()).thenReturn(100L);
    }

    @Test
    public void hasASetOfGauges() {
        assertThat(gauges.getMetrics().keySet())
                .containsOnly("vendor", "name", "uptime");
    }

    @Test
    public void hasAGaugeForTheJVMName() {
        final Gauge<String> gauge = (Gauge<String>) gauges.getMetrics().get("name");

        assertThat(gauge.getValue())
                .isEqualTo("9928@example.com");
    }

    @Test
    public void hasAGaugeForTheJVMVendor() {
        final Gauge<String> gauge = (Gauge<String>) gauges.getMetrics().get("vendor");

        assertThat(gauge.getValue())
                .isEqualTo("Oracle Corporation Java HotSpot(TM) 64-Bit Server VM 23.7-b01 (1.7)");
    }

    @Test
    public void hasAGaugeForTheJVMUptime() {
        final Gauge<Long> gauge = (Gauge<Long>) gauges.getMetrics().get("uptime");

        assertThat(gauge.getValue())
                .isEqualTo(100L);
    }

    @Test
    public void autoDiscoversTheRuntimeBean() {
        final Gauge<Long> gauge = (Gauge<Long>) new JvmAttributeGaugeSet().getMetrics().get("uptime");

        assertThat(gauge.getValue()).isPositive();
    }
}
