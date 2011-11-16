package com.yammer.metrics.core;

import java.util.EventListener;

/**
 * Listeners for events from the registry.  Listeners must be thread-safe.
 */
public interface MetricsRegistryListener extends EventListener {

    /**
     * Called when a metric has been added to the {@link MetricsRegistry}.
     *
     * @param name the name of the {@link Metric}
     * @param metric the {@link Metric}
     */
    public void onMetricAdded(MetricName name,
                              Metric metric);

    /**
     * Called when a metric has been removed from the {@link MetricsRegistry}.
     *
     * @param name the name of the {@link com.yammer.metrics.core.Metric}
     *
     */
    public void onMetricRemoved(MetricName name);
}
