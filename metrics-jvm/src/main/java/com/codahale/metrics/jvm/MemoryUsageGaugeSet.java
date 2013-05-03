package com.codahale.metrics.jvm;

import static com.codahale.metrics.MetricRegistry.name;

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

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricSet;
import com.codahale.metrics.NoCopyGauge;
import com.codahale.metrics.NoCopyMetric;
import com.codahale.metrics.NoCopyRatioGauge;

/**
 * A set of gauges for JVM memory usage, including stats on heap vs. non-heap memory, plus
 * GC-specific memory pools.
 */
public class MemoryUsageGaugeSet implements MetricSet {
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]+");

    private final MemoryMXBean mxBean;
    private final List<MemoryPoolMXBean> memoryPools;

    public MemoryUsageGaugeSet() {
        this(ManagementFactory.getMemoryMXBean(),
             ManagementFactory.getMemoryPoolMXBeans());
    }

    public MemoryUsageGaugeSet(MemoryMXBean mxBean,
                               Collection<MemoryPoolMXBean> memoryPools) {
        this.mxBean = mxBean;
        this.memoryPools = new ArrayList<MemoryPoolMXBean>(memoryPools);
    }

    @Override
    public Map<String, Metric> getMetrics() {
        final Map<String, Metric> gauges = new HashMap<String, Metric>();

        gauges.put("total.init", new NoCopyGauge<Long>() {
            @Override
            public Long getValue() {
                return mxBean.getHeapMemoryUsage().getInit() +
                        mxBean.getNonHeapMemoryUsage().getInit();
            }
        });

        gauges.put("total.used", new NoCopyGauge<Long>() {
            @Override
            public Long getValue() {
                return mxBean.getHeapMemoryUsage().getUsed() +
                        mxBean.getNonHeapMemoryUsage().getUsed();
            }
        });

        gauges.put("total.max", new NoCopyGauge<Long>() {
            @Override
            public Long getValue() {
                return mxBean.getHeapMemoryUsage().getMax() +
                        mxBean.getNonHeapMemoryUsage().getMax();
            }
        });

        gauges.put("total.committed", new NoCopyGauge<Long>() {
            @Override
            public Long getValue() {
                return mxBean.getHeapMemoryUsage().getCommitted() +
                        mxBean.getNonHeapMemoryUsage().getCommitted();
            }
        });


        gauges.put("heap.init", new NoCopyGauge<Long>() {
            @Override
            public Long getValue() {
                return mxBean.getHeapMemoryUsage().getInit();
            }
        });

        gauges.put("heap.used", new NoCopyGauge<Long>() {
            @Override
            public Long getValue() {
                return mxBean.getHeapMemoryUsage().getUsed();
            }
        });

        gauges.put("heap.max", new NoCopyGauge<Long>() {
            @Override
            public Long getValue() {
                return mxBean.getHeapMemoryUsage().getMax();
            }
        });

        gauges.put("heap.committed", new NoCopyGauge<Long>() {
            @Override
            public Long getValue() {
                return mxBean.getHeapMemoryUsage().getCommitted();
            }
        });

        gauges.put("heap.usage", new NoCopyRatioGauge() {
            @Override
            protected Ratio getRatio() {
                final MemoryUsage usage = mxBean.getHeapMemoryUsage();
                return Ratio.of(usage.getUsed(), usage.getMax());
            }
        });

        gauges.put("non-heap.init", new NoCopyGauge<Long>() {
            @Override
            public Long getValue() {
                return mxBean.getNonHeapMemoryUsage().getInit();
            }
        });

        gauges.put("non-heap.used", new NoCopyGauge<Long>() {
            @Override
            public Long getValue() {
                return mxBean.getNonHeapMemoryUsage().getUsed();
            }
        });

        gauges.put("non-heap.max", new NoCopyGauge<Long>() {
            @Override
            public Long getValue() {
                return mxBean.getNonHeapMemoryUsage().getMax();
            }
        });

        gauges.put("non-heap.committed", new NoCopyGauge<Long>() {
            @Override
            public Long getValue() {
                return mxBean.getNonHeapMemoryUsage().getCommitted();
            }
        });

        gauges.put("non-heap.usage", new NoCopyRatioGauge() {
            @Override
            protected Ratio getRatio() {
                final MemoryUsage usage = mxBean.getNonHeapMemoryUsage();
                return Ratio.of(usage.getUsed(), usage.getMax());
            }
        });

        for (final MemoryPoolMXBean pool : memoryPools) {
            gauges.put(name("pools",
                            WHITESPACE.matcher(pool.getName()).replaceAll("-"),
                            "usage"),
                       new NoCopyRatioGauge() {
                          @Override
                           protected Ratio getRatio() {
                               final long max = pool.getUsage().getMax() == -1 ?
                                       pool.getUsage().getCommitted() :
                                       pool.getUsage().getMax();
                               return Ratio.of(pool.getUsage().getUsed(), max);
                           }
                       });
        }

        return Collections.unmodifiableMap(gauges);
    }
}
