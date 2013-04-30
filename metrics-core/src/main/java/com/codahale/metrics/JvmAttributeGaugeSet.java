package com.codahale.metrics;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * A set of gauges for the JVM name, vendor, and uptime.
 */
public class JvmAttributeGaugeSet implements MetricSet {
    private final RuntimeMXBean runtime;
    private final String metricNamePrefix;

    /**
     * Creates a new set of gauges.
     */
    public JvmAttributeGaugeSet() {
        this(ManagementFactory.getRuntimeMXBean(), null);
    }

    /**
     * Creates a new set of gauges.
     * 
     * @param metricNamePrefix prefix for metric names, can be <code>null</code>
     */
    public JvmAttributeGaugeSet(String metricNamePrefix) {
        this(ManagementFactory.getRuntimeMXBean(), metricNamePrefix);
    }

    /**
     * Creates a new set of gauges with the given {@link RuntimeMXBean}.
     */
    public JvmAttributeGaugeSet(RuntimeMXBean runtime) {
        this(runtime, null);
    }
    
    /**
     * Creates a new set of gauges with the given {@link RuntimeMXBean}.
     * 
     * @param metricNamePrefix prefix for metric names, can be <code>null</code>
     */
    public JvmAttributeGaugeSet(RuntimeMXBean runtime, String metricNamePrefix) {
        super();
        this.runtime = runtime;
        this.metricNamePrefix = metricNamePrefix;
    }

    @Override
    public Map<String, Metric> getMetrics() {
        final Map<String, Metric> gauges = new HashMap<String, Metric>();

        gauges.put(name(metricNamePrefix, "name"), new Gauge<String>() {
            @Override
            public String getValue() {
                return runtime.getName();
            }
        });

        gauges.put(name(metricNamePrefix, "vendor"), new Gauge<String>() {
            @Override
            public String getValue() {
                return String.format(Locale.US,
                                     "%s %s %s (%s)",
                                     runtime.getVmVendor(),
                                     runtime.getVmName(),
                                     runtime.getVmVersion(),
                                     runtime.getSpecVersion());
            }
        });

        gauges.put(name(metricNamePrefix, "uptime"), new Gauge<Long>() {
            @Override
            public Long getValue() {
                return runtime.getUptime();
            }
        });

        return Collections.unmodifiableMap(gauges);
    }
}
