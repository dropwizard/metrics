package com.yammer.metrics.core;

/**
 * Listeners for new metric events from the registry.  Listeners must be thread-safe.
 */
public interface MetricsRegistryListener {

  public void newMetric(MetricName name, Metric metric);

}
