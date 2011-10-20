package com.yammer.metrics.core;

public interface MetricsRegistryListener {

  public void newMetric(MetricName name, Metric metric);

}
