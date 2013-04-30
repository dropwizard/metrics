package com.codahale.metrics.jvm;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricSet;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.regex.Pattern;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * A set of gauges for the counts and elapsed times of garbage collections.
 */
public class GarbageCollectorMetricSet implements MetricSet {
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]+");

    private final List<GarbageCollectorMXBean> garbageCollectors;
    private final String metricNamePrefix;

    /**
     * Creates a new set of gauges for all discoverable garbage collectors.
     */
    public GarbageCollectorMetricSet() {
        this(ManagementFactory.getGarbageCollectorMXBeans(), null);
    }

    /**
     * Creates a new set of gauges for all discoverable garbage collectors.
     * 
     * @param metricNamePrefix     prefix for metric names, can be <code>null</code>
     */
    public GarbageCollectorMetricSet(String metricNamePrefix) {
        this(ManagementFactory.getGarbageCollectorMXBeans(), metricNamePrefix);
    }

    /**
     * Creates a new set of gauges for the given collection of garbage collectors.
     *
     * @param garbageCollectors    the garbage collectors
     */
    public GarbageCollectorMetricSet(Collection<GarbageCollectorMXBean> garbageCollectors) {
        this(garbageCollectors, null);
    }

    /**
     * Creates a new set of gauges for the given collection of garbage collectors.
     *
     * @param garbageCollectors    the garbage collectors
     * @param metricNamePrefix     prefix for metric names, can be <code>null</code>
     */
    public GarbageCollectorMetricSet(Collection<GarbageCollectorMXBean> garbageCollectors, String metricNamePrefix) {
        super();
        this.garbageCollectors = new ArrayList<GarbageCollectorMXBean>(garbageCollectors);
        this.metricNamePrefix = metricNamePrefix;
    }

    @Override
    public Map<String, Metric> getMetrics() {
        final Map<String, Metric> gauges = new HashMap<String, Metric>();
        for (final GarbageCollectorMXBean gc : garbageCollectors) {
            final String name = WHITESPACE.matcher(gc.getName()).replaceAll("-");
            gauges.put(name(metricNamePrefix, name, "count"), new Gauge<Long>() {
                @Override
                public Long getValue() {
                    return gc.getCollectionCount();
                }
            });

            gauges.put(name(metricNamePrefix, name, "time"), new Gauge<Long>() {
                @Override
                public Long getValue() {
                    return gc.getCollectionTime();
                }
            });
        }
        return Collections.unmodifiableMap(gauges);
    }
}
