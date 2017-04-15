package com.codahale.metrics.jvm;

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricSet;
import com.codahale.metrics.jvm.jvmstat.monitors.JvmStatsMetric;
import com.codahale.metrics.jvm.jvmstat.monitors.LoadedClassesMetric;
import com.codahale.metrics.jvm.jvmstat.monitors.TotalCompilationTasksMetric;

import java.util.HashMap;
import java.util.Map;

/**
 * Encompasses all the existing metrics into one single object
 * <p>
 * If you want to include all the monitored data you can register an instance
 * of this class. If you only need a concrete metric you can register the concrete
 * metric you are looking for.
 */
public class JvmStatMetricsSet implements MetricSet {
    private static final JvmStatsMetric[] METRICS = { new LoadedClassesMetric(), new TotalCompilationTasksMetric() };

    public Map<String, Metric> getMetrics() {
        Map<String, Metric> metrics = new HashMap<String, Metric>();

        for (JvmStatsMetric metric : METRICS) {
            metrics.put(metric.getMetricName(), metric);
        }

        return metrics;
    }
}
