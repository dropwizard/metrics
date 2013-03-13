package com.yammer.metrics;

import java.util.Map;

/**
 * A set of named metrics.
 *
 * @see MetricRegistry#registerAll(MetricSet)
 * @see MetricRegistry#registerAll(String,MetricSet)
 */
public interface MetricSet {
    /**
     * A map of metric names to metrics.
     *
     * @return the metrics
     */
    Map<String, Metric> getMetrics();
}
