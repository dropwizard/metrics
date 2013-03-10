package com.yammer.metrics;

import java.util.EventListener;

/**
 * Listeners for events from the registry.  Listeners must be thread-safe.
 */
public interface MetricRegistryListener extends EventListener {
    void onGaugeAdded(String name, Gauge<?> gauge);

    void onGaugeRemoved(String name);

    void onCounterAdded(String name, Counter counter);

    void onCounterRemoved(String name);

    void onHistogramAdded(String name, Histogram histogram);

    void onHistogramRemoved(String name);

    void onMeterAdded(String name, Meter meter);

    void onMeterRemoved(String name);

    void onTimerAdded(String name, Timer timer);

    void onTimerRemoved(String name);
}
