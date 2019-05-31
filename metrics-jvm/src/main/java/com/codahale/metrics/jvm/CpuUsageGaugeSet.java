package com.codahale.metrics.jvm;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricSet;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A set of gauges for JVM memory CPU usage
 */
public class CpuUsageGaugeSet implements MetricSet {
    private final OperatingSystemMXBean operatingSystemMXBean;

    public CpuUsageGaugeSet() {
        this(ManagementFactory.getOperatingSystemMXBean());
    }
    
    public CpuUsageGaugeSet(OperatingSystemMXBean operatingSystemMXBean) {
        this.operatingSystemMXBean = operatingSystemMXBean;
    }

    @Override
    public Map<String, Metric> getMetrics() {
        if (!(operatingSystemMXBean instanceof com.sun.management.OperatingSystemMXBean)) {
            return Collections.emptyMap();
        }
        final com.sun.management.OperatingSystemMXBean osMxBean =
            (com.sun.management.OperatingSystemMXBean) operatingSystemMXBean;
        final Map<String, Metric> gauges = new HashMap<>();

        gauges.put("process-cpu-load-percentage", (Gauge<Double>) () -> osMxBean.getProcessCpuLoad());
        gauges.put("system-cpu-load-percentage", (Gauge<Double>) () -> osMxBean.getSystemCpuLoad());
        gauges.put("system-load-average", (Gauge<Double>) () -> osMxBean.getSystemLoadAverage());
        gauges.put("process-cpu-time", (Gauge<Long>) () -> osMxBean.getProcessCpuTime());
        
        return Collections.unmodifiableMap(gauges);
    }
}
