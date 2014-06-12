package com.codahale.metrics.jvm;

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.MetricSet;

import java.util.HashMap;
import java.util.Map;

/**
 * Memory Usage, GC, Threads, Files, CPU, Runtime metric sets.
 *
 * User: wendel.schultz
 * Date: 6/11/14
 */
public class StandardSystemMetricSet  implements MetricSet {

    @Override
    public Map<String, Metric> getMetrics() {
        final Map<String, Metric> metrics = new HashMap<String, Metric>();
        metrics.put("MemoryUsage", new MemoryUsageGaugeSet());
        metrics.put("GC", new GarbageCollectorMetricSet());
        metrics.put("Threads", new ThreadStatesGaugeSet());
        metrics.put("Files.usedToOpenRatio", new FileDescriptorRatioGauge());
        metrics.put("CPU", new CpuMetricSet());
        metrics.put("Runtime", new RuntimeMetricSet());

        return metrics;
    }

    public static void standardRegistration(MetricRegistry registry){
        registry.register("JVM", new StandardSystemMetricSet());
    }
}
