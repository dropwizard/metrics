package io.dropwizard.metrics.jvm;

import org.junit.Before;
import org.junit.Test;

import io.dropwizard.metrics.jvm.MemoryUsageGaugeSet;

import io.dropwizard.metrics.Gauge;
import io.dropwizard.metrics.MetricName;

import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

    private static final MetricName TOTAL = MetricName.build("total");
    private static final MetricName HEAP = MetricName.build("heap");
    private static final MetricName NON_HEAP = MetricName.build("non-heap");
    private static final MetricName POOLS = MetricName.build("pools");
    private static final MetricName BIG_POOL = POOLS.resolve("Big-Pool");
    private static final MetricName WEIRD_POOL = POOLS.resolve("Weird-Pool");

    private static final MetricName TOTAL_MAX = TOTAL.resolve("max");
    private static final MetricName TOTAL_INIT = TOTAL.resolve("init");
    private static final MetricName TOTAL_USED = TOTAL.resolve("used");
    private static final MetricName TOTAL_COMMITTED = TOTAL.resolve("committed");

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
    public void hasASetOfGauges() throws Exception {
        assertThat(gauges.getMetrics().keySet())
                .containsOnly(
                        HEAP_INIT,
                        HEAP_COMMITTED,
                        HEAP_USED,
                        HEAP_USAGE,
                        HEAP_MAX,
                        NON_HEAP_INIT,
                        NON_HEAP_COMMITTED,
                        NON_HEAP_USED,
                        NON_HEAP_USAGE,
                        NON_HEAP_MAX,
                        TOTAL_INIT,
                        TOTAL_COMMITTED,
                        TOTAL_USED,
                        TOTAL_MAX,
                        BIG_POOL.resolve("init"),
                        BIG_POOL.resolve("committed"),
                        BIG_POOL.resolve("used"),
                        BIG_POOL.resolve("usage"),
                        BIG_POOL.resolve("max"),
                        // skip in non-collected pools - "pools.Big-Pool.used-after-gc",
                        WEIRD_POOL.resolve("init"),
                        WEIRD_POOL.resolve("committed"),
                        WEIRD_POOL.resolve("used"),
                        WEIRD_POOL.resolve("used-after-gc"),
                        WEIRD_POOL.resolve("usage"),
                        WEIRD_POOL.resolve("max"));
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
        final Gauge gauge = (Gauge) gauges.getMetrics().get(BIG_POOL.resolve("usage"));

        assertThat(gauge.getValue())
                .isEqualTo(0.75);
    }

    @Test
    public void hasAGaugeForWeirdMemoryPoolInit() throws Exception {
        final Gauge gauge = (Gauge) gauges.getMetrics().get(WEIRD_POOL.resolve("init"));

        assertThat(gauge.getValue())
                .isEqualTo(200L);
    }

    @Test
    public void hasAGaugeForWeirdMemoryPoolCommitted() throws Exception {
        final Gauge gauge = (Gauge) gauges.getMetrics().get(WEIRD_POOL.resolve("committed"));

        assertThat(gauge.getValue())
                .isEqualTo(100L);
    }

    @Test
    public void hasAGaugeForWeirdMemoryPoolUsed() throws Exception {
        final Gauge gauge = (Gauge) gauges.getMetrics().get(WEIRD_POOL.resolve("used"));

        assertThat(gauge.getValue())
                .isEqualTo(300L);
    }

    @Test
    public void hasAGaugeForWeirdMemoryPoolUsage() throws Exception {
        final Gauge gauge = (Gauge) gauges.getMetrics().get(WEIRD_POOL.resolve("usage"));

        assertThat(gauge.getValue())
                .isEqualTo(3.0);
    }

    @Test
    public void hasAGaugeForWeirdMemoryPoolMax() throws Exception {
        final Gauge gauge = (Gauge) gauges.getMetrics().get(WEIRD_POOL.resolve("max"));

        assertThat(gauge.getValue())
                .isEqualTo(-1L);
    }

    @Test
    public void hasAGaugeForWeirdCollectionPoolUsed() throws Exception {
        final Gauge gauge = (Gauge) gauges.getMetrics().get(WEIRD_POOL.resolve("used-after-gc"));

        assertThat(gauge.getValue())
                .isEqualTo(290L);
    }

    @Test
    public void autoDetectsMemoryUsageBeanAndMemoryPools() throws Exception {
        assertThat(new MemoryUsageGaugeSet().getMetrics().keySet())
                .isNotEmpty();
    }
}
