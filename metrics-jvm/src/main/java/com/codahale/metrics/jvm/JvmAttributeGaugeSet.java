package com.codahale.metrics.jvm;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricSet;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * A set of gauges for the JVM name, vendor, and uptime.
 */
public class JvmAttributeGaugeSet implements MetricSet {
    private final RuntimeMXBean runtime;

    /**
     * Creates a new set of gauges.
     */
    public JvmAttributeGaugeSet() {
        this(ManagementFactory.getRuntimeMXBean());
    }

    /**
     * Creates a new set of gauges with the given {@link RuntimeMXBean}.
     * @param runtime JVM management interface with access to system properties
     */
    public JvmAttributeGaugeSet(RuntimeMXBean runtime) {
        this.runtime = runtime;
    }

    @Override
    public Map<String, Metric> getMetrics() {
        final Map<String, Metric> gauges = new HashMap<>();

        gauges.put("name", (Gauge<String>) runtime::getName);
        gauges.put("vendor", (Gauge<String>) () -> String.format(Locale.US,
                             "%s %s %s (%s)",
                             runtime.getVmVendor(),
                             runtime.getVmName(),
                             runtime.getVmVersion(),
                             runtime.getSpecVersion()));
        gauges.put("uptime", (Gauge<Long>) runtime::getUptime);

        return Collections.unmodifiableMap(gauges);
    }
}
