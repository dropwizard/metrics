package com.codahale.metrics.jvm;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricSet;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.HashMap;
import java.util.Map;

/**
 * A block-level metric set including CPU metrics.
 */
public class CpuMetricSet implements MetricSet {

    public Map<String, Metric> getMetrics() {
        final Map<String, Metric> metrics = new HashMap<String, Metric>();
        final OperatingSystemMXBean mxBean = ManagementFactory.getOperatingSystemMXBean();

        metrics.put("numCores", new Gauge<Integer>(){
            public Integer getValue() {
                return mxBean.getAvailableProcessors();
            }
        });
        metrics.put("loadAverage", new Gauge<Double>(){
            public Double getValue() {
                return mxBean.getSystemLoadAverage();
            }
        });

        return metrics;
    }

}