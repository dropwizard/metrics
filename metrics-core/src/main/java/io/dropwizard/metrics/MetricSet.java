package io.dropwizard.metrics;

import io.dropwizard.metrics.Metric;
import io.dropwizard.metrics.MetricRegistry;
import io.dropwizard.metrics.MetricSet;

import java.util.Map;

/**
 * A set of named metrics.
 *
 * @see MetricRegistry#registerAll(MetricSet)
 */
public interface MetricSet extends Metric {
    /**
     * A map of metric names to metrics.
     *
     * @return the metrics
     */
    Map<MetricName, Metric> getMetrics();
}
