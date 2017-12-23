package com.codahale.metrics.jvm;

import com.codahale.metrics.Gauge;
import org.junit.Before;
import org.junit.Test;

import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("rawtypes")
public class MemoryUsageGaugeSetTest {
    private final MemoryUsage heap = mock(MemoryUsage.class);
    private final MemoryUsage nonHeap = mock(MemoryUsage.class);
    private final MemoryUsage pool = mock(MemoryUsage.class);
    private final MemoryUsage weirdPool = mock(MemoryUsage.class);
    private final MemoryUsage weirdCollection = mock(MemoryUsage.class);
    private final MemoryMXBean mxBean = mock(MemoryMXBean.class);
    private final MemoryPoolMXBean memoryPool = mock(MemoryPoolMXBean.class);
    private final MemoryPoolMXBean weirdMemoryPool = mock(MemoryPoolMXBean.class);

    private final MemoryUsageGaugeSet gauges = new MemoryUsageGaugeSet(mxBean,
            Arrays.asList(memoryPool,
                    weirdMemoryPool));

    @Before
    public void setUp() {
        when(heap.getCommitted()).thenReturn(10L);
        when(heap.getInit()).thenReturn(20L);
        when(heap.getUsed()).thenReturn(30L);
        when(heap.getMax()).thenReturn(40L);

        when(nonHeap.getCommitted()).thenReturn(1L);
        when(nonHeap.getInit()).thenReturn(2L);
        when(nonHeap.getUsed()).thenReturn(3L);
        when(nonHeap.getMax()).thenReturn(4L);

        when(pool.getCommitted()).thenReturn(100L);
        when(pool.getInit()).thenReturn(200L);
        when(pool.getUsed()).thenReturn(300L);
        when(pool.getMax()).thenReturn(400L);

        when(weirdPool.getCommitted()).thenReturn(100L);
        when(weirdPool.getInit()).thenReturn(200L);
        when(weirdPool.getUsed()).thenReturn(300L);
        when(weirdPool.getMax()).thenReturn(-1L);

        when(weirdCollection.getUsed()).thenReturn(290L);

        when(mxBean.getHeapMemoryUsage()).thenReturn(heap);
        when(mxBean.getNonHeapMemoryUsage()).thenReturn(nonHeap);

        when(memoryPool.getUsage()).thenReturn(pool);
        // Mock that "Big Pool" is a non-collected pool therefore doesn't
        // have collection usage statistics.
        when(memoryPool.getCollectionUsage()).thenReturn(null);
        when(memoryPool.getName()).thenReturn("Big Pool");

        when(weirdMemoryPool.getUsage()).thenReturn(weirdPool);
        when(weirdMemoryPool.getCollectionUsage()).thenReturn(weirdCollection);
        when(weirdMemoryPool.getName()).thenReturn("Weird Pool");
    }

    @Test
    public void hasASetOfGauges() {
        assertThat(gauges.getMetrics().keySet())
                .containsOnly(
                        "heap.init",
                        "heap.committed",
                        "heap.used",
                        "heap.usage",
                        "heap.max",
                        "non-heap.init",
                        "non-heap.committed",
                        "non-heap.used",
                        "non-heap.usage",
                        "non-heap.max",
                        "total.init",
                        "total.committed",
                        "total.used",
                        "total.max",
                        "pools.Big-Pool.init",
                        "pools.Big-Pool.committed",
                        "pools.Big-Pool.used",
                        "pools.Big-Pool.usage",
                        "pools.Big-Pool.max",
                        // skip in non-collected pools - "pools.Big-Pool.used-after-gc",
                        "pools.Weird-Pool.init",
                        "pools.Weird-Pool.committed",
                        "pools.Weird-Pool.used",
                        "pools.Weird-Pool.used-after-gc",
                        "pools.Weird-Pool.usage",
                        "pools.Weird-Pool.max");
    }

    @Test
    public void hasAGaugeForTotalCommitted() {
        final Gauge gauge = (Gauge) gauges.getMetrics().get("total.committed");

        assertThat(gauge.getValue())
                .isEqualTo(11L);
    }

    @Test
    public void hasAGaugeForTotalInit() {
        final Gauge gauge = (Gauge) gauges.getMetrics().get("total.init");

        assertThat(gauge.getValue())
                .isEqualTo(22L);
    }

    @Test
    public void hasAGaugeForTotalUsed() {
        final Gauge gauge = (Gauge) gauges.getMetrics().get("total.used");

        assertThat(gauge.getValue())
                .isEqualTo(33L);
    }

    @Test
    public void hasAGaugeForTotalMax() {
        final Gauge gauge = (Gauge) gauges.getMetrics().get("total.max");

        assertThat(gauge.getValue())
                .isEqualTo(44L);
    }

    @Test
    public void hasAGaugeForHeapCommitted() {
        final Gauge gauge = (Gauge) gauges.getMetrics().get("heap.committed");

        assertThat(gauge.getValue())
                .isEqualTo(10L);
    }

    @Test
    public void hasAGaugeForHeapInit() {
        final Gauge gauge = (Gauge) gauges.getMetrics().get("heap.init");

        assertThat(gauge.getValue())
                .isEqualTo(20L);
    }

    @Test
    public void hasAGaugeForHeapUsed() {
        final Gauge gauge = (Gauge) gauges.getMetrics().get("heap.used");

        assertThat(gauge.getValue())
                .isEqualTo(30L);
    }

    @Test
    public void hasAGaugeForHeapMax() {
        final Gauge gauge = (Gauge) gauges.getMetrics().get("heap.max");

        assertThat(gauge.getValue())
                .isEqualTo(40L);
    }

    @Test
    public void hasAGaugeForHeapUsage() {
        final Gauge gauge = (Gauge) gauges.getMetrics().get("heap.usage");

        assertThat(gauge.getValue())
                .isEqualTo(0.75);
    }

    @Test
    public void hasAGaugeForNonHeapCommitted() {
        final Gauge gauge = (Gauge) gauges.getMetrics().get("non-heap.committed");

        assertThat(gauge.getValue())
                .isEqualTo(1L);
    }

    @Test
    public void hasAGaugeForNonHeapInit() {
        final Gauge gauge = (Gauge) gauges.getMetrics().get("non-heap.init");

        assertThat(gauge.getValue())
                .isEqualTo(2L);
    }

    @Test
    public void hasAGaugeForNonHeapUsed() {
        final Gauge gauge = (Gauge) gauges.getMetrics().get("non-heap.used");

        assertThat(gauge.getValue())
                .isEqualTo(3L);
    }

    @Test
    public void hasAGaugeForNonHeapMax() {
        final Gauge gauge = (Gauge) gauges.getMetrics().get("non-heap.max");

        assertThat(gauge.getValue())
                .isEqualTo(4L);
    }

    @Test
    public void hasAGaugeForNonHeapUsage() {
        final Gauge gauge = (Gauge) gauges.getMetrics().get("non-heap.usage");

        assertThat(gauge.getValue())
                .isEqualTo(0.75);
    }

    @Test
    public void hasAGaugeForMemoryPoolUsage() {
        final Gauge gauge = (Gauge) gauges.getMetrics().get("pools.Big-Pool.usage");

        assertThat(gauge.getValue())
                .isEqualTo(0.75);
    }

    @Test
    public void hasAGaugeForWeirdMemoryPoolInit() {
        final Gauge gauge = (Gauge) gauges.getMetrics().get("pools.Weird-Pool.init");

        assertThat(gauge.getValue())
                .isEqualTo(200L);
    }

    @Test
    public void hasAGaugeForWeirdMemoryPoolCommitted() {
        final Gauge gauge = (Gauge) gauges.getMetrics().get("pools.Weird-Pool.committed");

        assertThat(gauge.getValue())
                .isEqualTo(100L);
    }

    @Test
    public void hasAGaugeForWeirdMemoryPoolUsed() {
        final Gauge gauge = (Gauge) gauges.getMetrics().get("pools.Weird-Pool.used");

        assertThat(gauge.getValue())
                .isEqualTo(300L);
    }

    @Test
    public void hasAGaugeForWeirdMemoryPoolUsage() {
        final Gauge gauge = (Gauge) gauges.getMetrics().get("pools.Weird-Pool.usage");

        assertThat(gauge.getValue())
                .isEqualTo(3.0);
    }

    @Test
    public void hasAGaugeForWeirdMemoryPoolMax() {
        final Gauge gauge = (Gauge) gauges.getMetrics().get("pools.Weird-Pool.max");

        assertThat(gauge.getValue())
                .isEqualTo(-1L);
    }

    @Test
    public void hasAGaugeForWeirdCollectionPoolUsed() {
        final Gauge gauge = (Gauge) gauges.getMetrics().get("pools.Weird-Pool.used-after-gc");

        assertThat(gauge.getValue())
                .isEqualTo(290L);
    }

    @Test
    public void autoDetectsMemoryUsageBeanAndMemoryPools() {
        assertThat(new MemoryUsageGaugeSet().getMetrics().keySet())
                .isNotEmpty();
    }
}
