package io.dropwizard.metrics5.jvm;

import io.dropwizard.metrics5.Gauge;
import io.dropwizard.metrics5.MetricName;
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

    private static final MetricName TOTAL = MetricName.build("total");
    private static final MetricName HEAP = MetricName.build("heap");
    private static final MetricName NON_HEAP = MetricName.build("non-heap");
    private static final MetricName POOLS = MetricName.build("pools");

    private static final MetricName TOTAL_MAX = TOTAL.resolve("max");
    private static final MetricName TOTAL_INIT = TOTAL.resolve("init");
    private static final MetricName TOTAL_USED = TOTAL.resolve("used");
    private static final MetricName TOTAL_COMMITTED = TOTAL.resolve("committed");
    private static final MetricName POOLS_BIG_POOL_USAGE = POOLS.resolve("Big-Pool.usage");
    private static final MetricName POOLS_BIG_POOL_USED = POOLS.resolve("Big-Pool.used");
    private static final MetricName POOLS_BIG_POOL_INIT = POOLS.resolve("Big-Pool.init");
    private static final MetricName POOLS_BIG_POOL_COMMITED = POOLS.resolve("Big-Pool.committed");
    private static final MetricName POOLS_BIG_POOL_MAX = POOLS.resolve("Big-Pool.max");
    private static final MetricName POOLS_WEIRD_POOL_USAGE = POOLS.resolve("Weird-Pool.usage");
    private static final MetricName POOLS_WEIRD_POOL_INIT = POOLS.resolve("Weird-Pool.init");
    private static final MetricName POOLS_WEIRD_POOL_MAX = POOLS.resolve("Weird-Pool.max");
    private static final MetricName POOLS_WEIRD_POOL_USED = POOLS.resolve("Weird-Pool.used");
    private static final MetricName POOLS_WEIRD_POOL_USED_AFTER_GC = POOLS.resolve("Weird-Pool.used-after-gc");
    private static final MetricName POOLS_WEIRD_POOL_COMMITTED = POOLS.resolve("Weird-Pool.committed");
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
                        TOTAL_MAX,
                        TOTAL_INIT,
                        TOTAL_USED,
                        TOTAL_COMMITTED,
                        HEAP_INIT,
                        HEAP_COMMITTED,
                        HEAP_USAGE,
                        HEAP_USED,
                        HEAP_MAX,
                        NON_HEAP_USAGE,
                        NON_HEAP_MAX,
                        NON_HEAP_USED,
                        NON_HEAP_INIT,
                        NON_HEAP_COMMITTED,
                        POOLS_BIG_POOL_INIT,
                        POOLS_BIG_POOL_COMMITED,
                        POOLS_BIG_POOL_USED,
                        POOLS_BIG_POOL_USAGE,
                        POOLS_BIG_POOL_MAX,
                        // skip in non-collected pools - "pools.Big-Pool.used-after-gc",
                        POOLS_WEIRD_POOL_USAGE,
                        POOLS_WEIRD_POOL_USED,
                        POOLS_WEIRD_POOL_INIT,
                        POOLS_WEIRD_POOL_MAX,
                        POOLS_WEIRD_POOL_COMMITTED,
                        POOLS_WEIRD_POOL_USED_AFTER_GC);
    }

    @Test
    public void hasAGaugeForTotalCommitted() {
        final Gauge gauge = (Gauge) gauges.getMetrics().get(TOTAL_COMMITTED);

        assertThat(gauge.getValue())
                .isEqualTo(11L);
    }

    @Test
    public void hasAGaugeForTotalInit() {
        final Gauge gauge = (Gauge) gauges.getMetrics().get(TOTAL_INIT);

        assertThat(gauge.getValue())
                .isEqualTo(22L);
    }

    @Test
    public void hasAGaugeForTotalUsed() {
        final Gauge gauge = (Gauge) gauges.getMetrics().get(TOTAL_USED);

        assertThat(gauge.getValue())
                .isEqualTo(33L);
    }

    @Test
    public void hasAGaugeForTotalMax() {
        final Gauge gauge = (Gauge) gauges.getMetrics().get(TOTAL_MAX);

        assertThat(gauge.getValue())
                .isEqualTo(44L);
    }

    @Test
    public void hasAGaugeForHeapCommitted() {
        final Gauge gauge = (Gauge) gauges.getMetrics().get(HEAP_COMMITTED);

        assertThat(gauge.getValue())
                .isEqualTo(10L);
    }

    @Test
    public void hasAGaugeForHeapInit() {
        final Gauge gauge = (Gauge) gauges.getMetrics().get(HEAP_INIT);

        assertThat(gauge.getValue())
                .isEqualTo(20L);
    }

    @Test
    public void hasAGaugeForHeapUsed() {
        final Gauge gauge = (Gauge) gauges.getMetrics().get(HEAP_USED);

        assertThat(gauge.getValue())
                .isEqualTo(30L);
    }

    @Test
    public void hasAGaugeForHeapMax() {
        final Gauge gauge = (Gauge) gauges.getMetrics().get(HEAP_MAX);

        assertThat(gauge.getValue())
                .isEqualTo(40L);
    }

    @Test
    public void hasAGaugeForHeapUsage() {
        final Gauge gauge = (Gauge) gauges.getMetrics().get(HEAP_USAGE);

        assertThat(gauge.getValue())
                .isEqualTo(0.75);
    }

    @Test
    public void hasAGaugeForNonHeapCommitted() {
        final Gauge gauge = (Gauge) gauges.getMetrics().get(NON_HEAP_COMMITTED);

        assertThat(gauge.getValue())
                .isEqualTo(1L);
    }

    @Test
    public void hasAGaugeForNonHeapInit() {
        final Gauge gauge = (Gauge) gauges.getMetrics().get(NON_HEAP_INIT);

        assertThat(gauge.getValue())
                .isEqualTo(2L);
    }

    @Test
    public void hasAGaugeForNonHeapUsed() {
        final Gauge gauge = (Gauge) gauges.getMetrics().get(NON_HEAP_USED);

        assertThat(gauge.getValue())
                .isEqualTo(3L);
    }

    @Test
    public void hasAGaugeForNonHeapMax() {
        final Gauge gauge = (Gauge) gauges.getMetrics().get(NON_HEAP_MAX);

        assertThat(gauge.getValue())
                .isEqualTo(4L);
    }

    @Test
    public void hasAGaugeForNonHeapUsage() {
        final Gauge gauge = (Gauge) gauges.getMetrics().get(NON_HEAP_USAGE);

        assertThat(gauge.getValue())
                .isEqualTo(0.75);
    }

    @Test
    public void hasAGaugeForMemoryPoolUsage() {
        final Gauge gauge = (Gauge) gauges.getMetrics().get(POOLS_BIG_POOL_USAGE);

        assertThat(gauge.getValue())
                .isEqualTo(0.75);
    }

    @Test
    public void hasAGaugeForWeirdMemoryPoolInit() {
        final Gauge gauge = (Gauge) gauges.getMetrics().get(POOLS_WEIRD_POOL_INIT);

        assertThat(gauge.getValue())
                .isEqualTo(200L);
    }

    @Test
    public void hasAGaugeForWeirdMemoryPoolCommitted() {
        final Gauge gauge = (Gauge) gauges.getMetrics().get(POOLS_WEIRD_POOL_COMMITTED);

        assertThat(gauge.getValue())
                .isEqualTo(100L);
    }

    @Test
    public void hasAGaugeForWeirdMemoryPoolUsed() {
        final Gauge gauge = (Gauge) gauges.getMetrics().get(POOLS_WEIRD_POOL_USED);

        assertThat(gauge.getValue())
                .isEqualTo(300L);
    }

    @Test
    public void hasAGaugeForWeirdMemoryPoolUsage() {
        final Gauge gauge = (Gauge) gauges.getMetrics().get(POOLS_WEIRD_POOL_USAGE);

        assertThat(gauge.getValue())
                .isEqualTo(3.0);
    }

    @Test
    public void hasAGaugeForWeirdMemoryPoolMax() {
        final Gauge gauge = (Gauge) gauges.getMetrics().get(POOLS_WEIRD_POOL_MAX);

        assertThat(gauge.getValue())
                .isEqualTo(-1L);
    }

    @Test
    public void hasAGaugeForWeirdCollectionPoolUsed() {
        final Gauge gauge = (Gauge) gauges.getMetrics().get(POOLS_WEIRD_POOL_USED_AFTER_GC);

        assertThat(gauge.getValue())
                .isEqualTo(290L);
    }

    @Test
    public void autoDetectsMemoryUsageBeanAndMemoryPools() {
        assertThat(new MemoryUsageGaugeSet().getMetrics().keySet())
                .isNotEmpty();
    }
}
