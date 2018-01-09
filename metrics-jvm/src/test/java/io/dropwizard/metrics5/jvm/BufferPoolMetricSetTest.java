package io.dropwizard.metrics5.jvm;

import io.dropwizard.metrics5.Gauge;
import io.dropwizard.metrics5.MetricName;
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

    private static final MetricName DIRECT = MetricName.build("direct");
    private static final MetricName MAPPED = MetricName.build("mapped");
    private static final MetricName DIRECT_COUNT = DIRECT.resolve("count");
    private static final MetricName DIRECT_CAPACITY = DIRECT.resolve("capacity");
    private static final MetricName DIRECT_USED = DIRECT.resolve("used");
    private static final MetricName MAPPED_COUNT = MAPPED.resolve("count");
    private static final MetricName MAPPED_CAPACITY = MAPPED.resolve("capacity");
    private static final MetricName MAPPED_USED = MAPPED.resolve("used");

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
                .containsOnly(DIRECT_COUNT,
                        DIRECT_USED,
                        DIRECT_CAPACITY,
                        MAPPED_COUNT,
                        MAPPED_USED,
                        MAPPED_CAPACITY);
    }

    @Test
    public void ignoresGaugesForObjectsWhichCannotBeFound() throws Exception {
        when(mBeanServer.getMBeanInfo(mapped)).thenThrow(new InstanceNotFoundException());

        assertThat(buffers.getMetrics().keySet())
                .containsOnly(DIRECT_COUNT,
                        DIRECT_USED,
                        DIRECT_CAPACITY);
    }

    @Test
    public void includesAGaugeForDirectCount() throws Exception {
        final Gauge gauge = (Gauge) buffers.getMetrics().get(DIRECT_COUNT);

        when(mBeanServer.getAttribute(direct, "Count")).thenReturn(100);

        assertThat(gauge.getValue())
                .isEqualTo(100);
    }

    @Test
    public void includesAGaugeForDirectMemoryUsed() throws Exception {
        final Gauge gauge = (Gauge) buffers.getMetrics().get(DIRECT_USED);

        when(mBeanServer.getAttribute(direct, "MemoryUsed")).thenReturn(100);

        assertThat(gauge.getValue())
                .isEqualTo(100);
    }

    @Test
    public void includesAGaugeForDirectCapacity() throws Exception {
        final Gauge gauge = (Gauge) buffers.getMetrics().get(DIRECT_CAPACITY);

        when(mBeanServer.getAttribute(direct, "TotalCapacity")).thenReturn(100);

        assertThat(gauge.getValue())
                .isEqualTo(100);
    }

    @Test
    public void includesAGaugeForMappedCount() throws Exception {
        final Gauge gauge = (Gauge) buffers.getMetrics().get(MAPPED_COUNT);

        when(mBeanServer.getAttribute(mapped, "Count")).thenReturn(100);

        assertThat(gauge.getValue())
                .isEqualTo(100);
    }

    @Test
    public void includesAGaugeForMappedMemoryUsed() throws Exception {
        final Gauge gauge = (Gauge) buffers.getMetrics().get(MAPPED_USED);

        when(mBeanServer.getAttribute(mapped, "MemoryUsed")).thenReturn(100);

        assertThat(gauge.getValue())
                .isEqualTo(100);
    }

    @Test
    public void includesAGaugeForMappedCapacity() throws Exception {
        final Gauge gauge = (Gauge) buffers.getMetrics().get(MAPPED_CAPACITY);

        when(mBeanServer.getAttribute(mapped, "TotalCapacity")).thenReturn(100);

        assertThat(gauge.getValue())
                .isEqualTo(100);
    }
}
