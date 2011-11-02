package com.yammer.metrics.core;

/**
 * Listeners for events from the registry.  Listeners must be thread-safe.
 */
public interface MetricsRegistryListener {

    public void metricAdded(MetricName name, Metric metric);

    public void metricRemoved(MetricName name, Metric metric);

}
