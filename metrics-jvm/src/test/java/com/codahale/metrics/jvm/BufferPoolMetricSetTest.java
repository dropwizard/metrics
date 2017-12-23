package com.codahale.metrics.jvm;

import com.codahale.metrics.Gauge;
import org.junit.Before;
import org.junit.Test;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("rawtypes")
public class BufferPoolMetricSetTest {
    private final MBeanServer mBeanServer = mock(MBeanServer.class);
    private final BufferPoolMetricSet buffers = new BufferPoolMetricSet(mBeanServer);

    private ObjectName mapped;
    private ObjectName direct;

    @Before
    public void setUp() throws Exception {
        this.mapped = new ObjectName("java.nio:type=BufferPool,name=mapped");
        this.direct = new ObjectName("java.nio:type=BufferPool,name=direct");

    }

    @Test
    public void includesGaugesForDirectAndMappedPools() {
        assertThat(buffers.getMetrics().keySet())
                .containsOnly("direct.count",
                        "mapped.used",
                        "mapped.capacity",
                        "direct.capacity",
                        "mapped.count",
                        "direct.used");
    }

    @Test
    public void ignoresGaugesForObjectsWhichCannotBeFound() throws Exception {
        when(mBeanServer.getMBeanInfo(mapped)).thenThrow(new InstanceNotFoundException());

        assertThat(buffers.getMetrics().keySet())
                .containsOnly("direct.count",
                        "direct.capacity",
                        "direct.used");
    }

    @Test
    public void includesAGaugeForDirectCount() throws Exception {
        final Gauge gauge = (Gauge) buffers.getMetrics().get("direct.count");

        when(mBeanServer.getAttribute(direct, "Count")).thenReturn(100);

        assertThat(gauge.getValue())
                .isEqualTo(100);
    }

    @Test
    public void includesAGaugeForDirectMemoryUsed() throws Exception {
        final Gauge gauge = (Gauge) buffers.getMetrics().get("direct.used");

        when(mBeanServer.getAttribute(direct, "MemoryUsed")).thenReturn(100);

        assertThat(gauge.getValue())
                .isEqualTo(100);
    }

    @Test
    public void includesAGaugeForDirectCapacity() throws Exception {
        final Gauge gauge = (Gauge) buffers.getMetrics().get("direct.capacity");

        when(mBeanServer.getAttribute(direct, "TotalCapacity")).thenReturn(100);

        assertThat(gauge.getValue())
                .isEqualTo(100);
    }

    @Test
    public void includesAGaugeForMappedCount() throws Exception {
        final Gauge gauge = (Gauge) buffers.getMetrics().get("mapped.count");

        when(mBeanServer.getAttribute(mapped, "Count")).thenReturn(100);

        assertThat(gauge.getValue())
                .isEqualTo(100);
    }

    @Test
    public void includesAGaugeForMappedMemoryUsed() throws Exception {
        final Gauge gauge = (Gauge) buffers.getMetrics().get("mapped.used");

        when(mBeanServer.getAttribute(mapped, "MemoryUsed")).thenReturn(100);

        assertThat(gauge.getValue())
                .isEqualTo(100);
    }

    @Test
    public void includesAGaugeForMappedCapacity() throws Exception {
        final Gauge gauge = (Gauge) buffers.getMetrics().get("mapped.capacity");

        when(mBeanServer.getAttribute(mapped, "TotalCapacity")).thenReturn(100);

        assertThat(gauge.getValue())
                .isEqualTo(100);
    }
}
