package com.yammer.metrics.core;

/**
 * A tag interface to indicate that a class is a metric.
 */
public interface Metric {
    <T> void processWith(MetricsProcessor<T> reporter, MetricName name, T context) throws Exception;
}
