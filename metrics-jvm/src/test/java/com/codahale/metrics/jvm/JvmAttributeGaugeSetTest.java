package com.codahale.metrics.jvm;

import com.codahale.metrics.Gauge;
import org.junit.Before;
import org.junit.Test;

import java.lang.management.RuntimeMXBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
public class JvmAttributeGaugeSetTest {
    private final RuntimeMXBean runtime = mock(RuntimeMXBean.class);
    private final JvmAttributeGaugeSet gauges = new JvmAttributeGaugeSet(runtime);

    @Before
    public void setUp() throws Exception {
        when(runtime.getName()).thenReturn("9928@example.com");

        when(runtime.getVmVendor()).thenReturn("Oracle Corporation");
        when(runtime.getVmName()).thenReturn("Java HotSpot(TM) 64-Bit Server VM");
        when(runtime.getVmVersion()).thenReturn("23.7-b01");
        when(runtime.getSpecVersion()).thenReturn("1.7");
        when(runtime.getUptime()).thenReturn(100L);
    }

    @Test
    public void hasASetOfGauges() throws Exception {
        assertThat(gauges.getMetrics().keySet())
                .containsOnly("vendor", "name", "uptime", "processCpuLoad");
    }

    @Test
    public void hasAGaugeForTheJVMName() throws Exception {
        final Gauge<String> gauge = (Gauge<String>) gauges.getMetrics().get("name");

        assertThat(gauge.getValue())
                .isEqualTo("9928@example.com");
    }

    @Test
    public void hasAGaugeForTheJVMVendor() throws Exception {
        final Gauge<String> gauge = (Gauge<String>) gauges.getMetrics().get("vendor");

        assertThat(gauge.getValue())
                .isEqualTo("Oracle Corporation Java HotSpot(TM) 64-Bit Server VM 23.7-b01 (1.7)");
    }

    @Test
    public void hasAGaugeForTheJVMUptime() throws Exception {
        final Gauge<Long> gauge = (Gauge<Long>) gauges.getMetrics().get("uptime");

        assertThat(gauge.getValue())
                .isEqualTo(100L);
    }

    @Test
    public void autoDiscoversTheRuntimeBean() throws Exception {
        final Gauge<Long> gauge = (Gauge<Long>) new JvmAttributeGaugeSet().getMetrics().get("uptime");

        assertThat(gauge.getValue()).isPositive();
    }
    
    @Test
    public void hasAGuageForProcessCpuLoad() throws Exception{
    	Gauge<Double> gauge = (Gauge<Double>) new JvmAttributeGaugeSet().getMetrics().get("processCpuLoad");
    	assertNotNull(gauge);
    	assertThat(gauge.getValue()).isNotNull();
    }
}
