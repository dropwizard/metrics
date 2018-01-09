package io.dropwizard.metrics5.jvm;

import io.dropwizard.metrics5.Gauge;
import io.dropwizard.metrics5.Metric;
import io.dropwizard.metrics5.MetricName;
import io.dropwizard.metrics5.MetricRegistry;
import io.dropwizard.metrics5.MetricSet;
import io.dropwizard.metrics5.RatioGauge;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * A set of gauges for JVM memory usage, including stats on heap vs. non-heap memory, plus
 * GC-specific memory pools.
 */
public class MemoryUsageGaugeSet implements MetricSet {
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]+");

    private final MemoryMXBean mxBean;
    private final List<MemoryPoolMXBean> memoryPools;

    public MemoryUsageGaugeSet() {
        this(ManagementFactory.getMemoryMXBean(), ManagementFactory.getMemoryPoolMXBeans());
    }

    public MemoryUsageGaugeSet(MemoryMXBean mxBean,
                               Collection<MemoryPoolMXBean> memoryPools) {
        this.mxBean = mxBean;
        this.memoryPools = new ArrayList<>(memoryPools);
    }

    @Override
    public Map<MetricName, Metric> getMetrics() {
        final Map<MetricName, Metric> gauges = new HashMap<>();

        gauges.put(MetricName.build("total.init"), (Gauge<Long>) () -> mxBean.getHeapMemoryUsage().getInit() +
                mxBean.getNonHeapMemoryUsage().getInit());
        gauges.put(MetricName.build("total.used"), (Gauge<Long>) () -> mxBean.getHeapMemoryUsage().getUsed() +
                mxBean.getNonHeapMemoryUsage().getUsed());
        gauges.put(MetricName.build("total.max"), (Gauge<Long>) () -> mxBean.getHeapMemoryUsage().getMax() +
                mxBean.getNonHeapMemoryUsage().getMax());
        gauges.put(MetricName.build("total.committed"), (Gauge<Long>) () -> mxBean.getHeapMemoryUsage().getCommitted() +
                mxBean.getNonHeapMemoryUsage().getCommitted());

        gauges.put(MetricName.build("heap.init"), (Gauge<Long>) () -> mxBean.getHeapMemoryUsage().getInit());
        gauges.put(MetricName.build("heap.used"), (Gauge<Long>) () -> mxBean.getHeapMemoryUsage().getUsed());
        gauges.put(MetricName.build("heap.max"), (Gauge<Long>) () -> mxBean.getHeapMemoryUsage().getMax());
        gauges.put(MetricName.build("heap.committed"), (Gauge<Long>) () -> mxBean.getHeapMemoryUsage().getCommitted());
        gauges.put(MetricName.build("heap.usage"), new RatioGauge() {
            @Override
            protected Ratio getRatio() {
                final MemoryUsage usage = mxBean.getHeapMemoryUsage();
                return Ratio.of(usage.getUsed(), usage.getMax());
            }
        });

        gauges.put(MetricName.build("non-heap.init"), (Gauge<Long>) () -> mxBean.getNonHeapMemoryUsage().getInit());
        gauges.put(MetricName.build("non-heap.used"), (Gauge<Long>) () -> mxBean.getNonHeapMemoryUsage().getUsed());
        gauges.put(MetricName.build("non-heap.max"), (Gauge<Long>) () -> mxBean.getNonHeapMemoryUsage().getMax());
        gauges.put(MetricName.build("non-heap.committed"), (Gauge<Long>) () -> mxBean.getNonHeapMemoryUsage().getCommitted());
        gauges.put(MetricName.build("non-heap.usage"), new RatioGauge() {
            @Override
            protected Ratio getRatio() {
                final MemoryUsage usage = mxBean.getNonHeapMemoryUsage();
                return Ratio.of(usage.getUsed(), usage.getMax());
            }
        });

        for (final MemoryPoolMXBean pool : memoryPools) {
            final String poolName = "pools." + WHITESPACE.matcher(pool.getName()).replaceAll("-");

            gauges.put(MetricRegistry.name(poolName, "usage"), new RatioGauge() {
                @Override
                protected Ratio getRatio() {
                    MemoryUsage usage = pool.getUsage();
                    return Ratio.of(usage.getUsed(),
                            usage.getMax() == -1 ? usage.getCommitted() : usage.getMax());
                }
            });

            gauges.put(MetricRegistry.name(poolName, "max"), (Gauge<Long>) () -> pool.getUsage().getMax());
            gauges.put(MetricRegistry.name(poolName, "used"), (Gauge<Long>) () -> pool.getUsage().getUsed());
            gauges.put(MetricRegistry.name(poolName, "committed"), (Gauge<Long>) () -> pool.getUsage().getCommitted());

            // Only register GC usage metrics if the memory pool supports usage statistics.
            if (pool.getCollectionUsage() != null) {
                gauges.put(MetricRegistry.name(poolName, "used-after-gc"), (Gauge<Long>) () ->
                        pool.getCollectionUsage().getUsed());
            }

            gauges.put(MetricRegistry.name(poolName, "init"), (Gauge<Long>) () -> pool.getUsage().getInit());
        }

        return Collections.unmodifiableMap(gauges);
    }
}
