package com.codahale.metrics.jvm;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricName;

import org.junit.Before;
import org.junit.Test;

import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.Arrays;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MemoryUsageGaugeSetTest {
    private final MemoryUsage heap = mock(MemoryUsage.class);
    private final MemoryUsage nonHeap = mock(MemoryUsage.class);
    private final MemoryUsage pool = mock(MemoryUsage.class);
    private final MemoryUsage weirdPool = mock(MemoryUsage.class);
    private final MemoryMXBean mxBean = mock(MemoryMXBean.class);
    private final MemoryPoolMXBean memoryPool = mock(MemoryPoolMXBean.class);
    private final MemoryPoolMXBean weirdMemoryPool = mock(MemoryPoolMXBean.class);

    private final MemoryUsageGaugeSet gauges = new MemoryUsageGaugeSet(mxBean,
                                                                       Arrays.asList(memoryPool,
                                                                                     weirdMemoryPool));

    private static final MetricName TOTAL = MetricName.build("total");
    private static final MetricName HEAP = MetricName.build("heap");
    private static final MetricName NON_HEAP = MetricName.build("non-heap");
    private static final MetricName POOLS = MetricName.build("pools");

    private static final MetricName TOTAL_MAX = TOTAL.resolve("max");
    private static final MetricName TOTAL_INIT = TOTAL.resolve("init");
    private static final MetricName TOTAL_USED = TOTAL.resolve("used");
    private static final MetricName TOTAL_COMMITTED = TOTAL.resolve("committed");
    private static final MetricName POOLS_BIG_POOL_USAGE = POOLS.resolve("Big-Pool.usage");
    private static final MetricName POOLS_WEIRD_POOL_USAGE = POOLS.resolve("Weird-Pool.usage");
    private static final MetricName HEAP_INIT = HEAP.resolve("init");
    private static final MetricName HEAP_COMMITTED = HEAP.resolve("committed");
    private static final MetricName HEAP_USAGE = HEAP.resolve("usage");
    private static final MetricName HEAP_USED = HEAP.resolve("used");
    private static final MetricName HEAP_MAX = HEAP.resolve("max");
    private static final MetricName NON_HEAP_USAGE = NON_HEAP.resolve("usage");
    private static final MetricName NON_HEAP_MAX = NON_HEAP.resolve("max");
    private static final MetricName NON_HEAP_USED = NON_HEAP.resolve("used");
    private static final MetricName NON_HEAP_INIT = NON_HEAP.resolve("init");
    private static final MetricName NON_HEAP_COMMITTED = NON_HEAP.resolve("committed");

    @Before
    public void setUp() throws Exception {
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

        when(mxBean.getHeapMemoryUsage()).thenReturn(heap);
        when(mxBean.getNonHeapMemoryUsage()).thenReturn(nonHeap);

        when(memoryPool.getUsage()).thenReturn(pool);
        when(memoryPool.getName()).thenReturn("Big Pool");

        when(weirdMemoryPool.getUsage()).thenReturn(weirdPool);
        when(weirdMemoryPool.getName()).thenReturn("Weird Pool");
    }

    @Test
    public void hasASetOfGauges() throws Exception {
        assertThat(gauges.getMetrics().keySet())
                .containsOnly(
                        TOTAL_MAX,
                        TOTAL_INIT,
                        TOTAL_USED,
                        TOTAL_COMMITTED,
                        POOLS_BIG_POOL_USAGE,
                        POOLS_WEIRD_POOL_USAGE,
                        HEAP_INIT,
                        HEAP_COMMITTED,
                        HEAP_USAGE,
                        HEAP_USED,
                        HEAP_MAX,
                        NON_HEAP_USAGE,
                        NON_HEAP_MAX,
                        NON_HEAP_USED,
                        NON_HEAP_INIT,
                        NON_HEAP_COMMITTED);
    }

    @Test
    public void hasAGaugeForTotalCommitted() throws Exception {
        final Gauge gauge = (Gauge) gauges.getMetrics().get(TOTAL_COMMITTED);

        assertThat(gauge.getValue())
                .isEqualTo(11L);
    }

    @Test
    public void hasAGaugeForTotalInit() throws Exception {
        final Gauge gauge = (Gauge) gauges.getMetrics().get(TOTAL_INIT);

        assertThat(gauge.getValue())
                .isEqualTo(22L);
    }

    @Test
    public void hasAGaugeForTotalUsed() throws Exception {
        final Gauge gauge = (Gauge) gauges.getMetrics().get(TOTAL_USED);

        assertThat(gauge.getValue())
                .isEqualTo(33L);
    }

    @Test
    public void hasAGaugeForTotalMax() throws Exception {
        final Gauge gauge = (Gauge) gauges.getMetrics().get(TOTAL_MAX);

        assertThat(gauge.getValue())
                .isEqualTo(44L);
    }

    @Test
    public void hasAGaugeForHeapCommitted() throws Exception {
        final Gauge gauge = (Gauge) gauges.getMetrics().get(HEAP_COMMITTED);

        assertThat(gauge.getValue())
                .isEqualTo(10L);
    }

    @Test
    public void hasAGaugeForHeapInit() throws Exception {
        final Gauge gauge = (Gauge) gauges.getMetrics().get(HEAP_INIT);

        assertThat(gauge.getValue())
                .isEqualTo(20L);
    }

    @Test
    public void hasAGaugeForHeapUsed() throws Exception {
        final Gauge gauge = (Gauge) gauges.getMetrics().get(HEAP_USED);

        assertThat(gauge.getValue())
                .isEqualTo(30L);
    }

    @Test
    public void hasAGaugeForHeapMax() throws Exception {
        final Gauge gauge = (Gauge) gauges.getMetrics().get(HEAP_MAX);

        assertThat(gauge.getValue())
                .isEqualTo(40L);
    }

    @Test
    public void hasAGaugeForHeapUsage() throws Exception {
        final Gauge gauge = (Gauge) gauges.getMetrics().get(HEAP_USAGE);

        assertThat(gauge.getValue())
                .isEqualTo(0.75);
    }

    @Test
    public void hasAGaugeForNonHeapCommitted() throws Exception {
        final Gauge gauge = (Gauge) gauges.getMetrics().get(NON_HEAP_COMMITTED);

        assertThat(gauge.getValue())
                .isEqualTo(1L);
    }

    @Test
    public void hasAGaugeForNonHeapInit() throws Exception {
        final Gauge gauge = (Gauge) gauges.getMetrics().get(NON_HEAP_INIT);

        assertThat(gauge.getValue())
                .isEqualTo(2L);
    }

    @Test
    public void hasAGaugeForNonHeapUsed() throws Exception {
        final Gauge gauge = (Gauge) gauges.getMetrics().get(NON_HEAP_USED);

        assertThat(gauge.getValue())
                .isEqualTo(3L);
    }

    @Test
    public void hasAGaugeForNonHeapMax() throws Exception {
        final Gauge gauge = (Gauge) gauges.getMetrics().get(NON_HEAP_MAX);

        assertThat(gauge.getValue())
                .isEqualTo(4L);
    }

    @Test
    public void hasAGaugeForNonHeapUsage() throws Exception {
        final Gauge gauge = (Gauge) gauges.getMetrics().get(NON_HEAP_USAGE);

        assertThat(gauge.getValue())
                .isEqualTo(0.75);
    }

    @Test
    public void hasAGaugeForMemoryPoolUsage() throws Exception {
        final Gauge gauge = (Gauge) gauges.getMetrics().get(POOLS_BIG_POOL_USAGE);

        assertThat(gauge.getValue())
                .isEqualTo(0.75);
    }

    @Test
    public void hasAGaugeForWeirdMemoryPoolUsage() throws Exception {
        final Gauge gauge = (Gauge) gauges.getMetrics().get(POOLS_WEIRD_POOL_USAGE);

        assertThat(gauge.getValue())
                .isEqualTo(3.0);
    }

    @Test
    public void autoDetectsMemoryUsageBeanAndMemoryPools() throws Exception {
        assertThat(new MemoryUsageGaugeSet().getMetrics().keySet())
                .isNotEmpty();
    }
}
