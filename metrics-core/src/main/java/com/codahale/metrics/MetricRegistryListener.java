package com.codahale.metrics;

import java.util.EventListener;

/**
 * Listeners for events from the registry.  Listeners must be thread-safe.
 */
public interface MetricRegistryListener extends EventListener {
    /**
     * A no-op implementation of {@link MetricRegistryListener}.
     */
    abstract class Base implements MetricRegistryListener {
        @Override
        public void onGaugeAdded(String name, Gauge<?> gauge) {
        }

        @Override
        public void onGaugeRemoved(String name, Gauge<?> metric) {
        }

        @Override
        public void onCounterAdded(String name, Counter counter) {
        }

        @Override
        public void onCounterRemoved(String name, Counter metric) {
        }

        @Override
        public void onHistogramAdded(String name, Histogram histogram) {
        }

        @Override
        public void onHistogramRemoved(String name, Histogram metric) {
        }

        @Override
        public void onMeterAdded(String name, Meter meter) {
        }

        @Override
        public void onMeterRemoved(String name, Meter metric) {
        }

        @Override
        public void onTimerAdded(String name, Timer timer) {
        }

        @Override
        public void onTimerRemoved(String name, Timer metric) {
        }
    }

    /**
     * Called when a {@link Gauge} is added to the registry.
     *
     * @param name  the gauge's name
     * @param gauge the gauge
     */
    void onGaugeAdded(String name, Gauge<?> gauge);

    /**
     * Called when a {@link Gauge} is removed from the registry.
     *
     * @param name the gauge's name
     * @param metric
     */
    void onGaugeRemoved(String name, Gauge<?> metric);

    /**
     * Called when a {@link Counter} is added to the registry.
     *
     * @param name    the counter's name
     * @param counter the counter
     */
    void onCounterAdded(String name, Counter counter);

    /**
     * Called when a {@link Counter} is removed from the registry.
     *
     * @param name the counter's name
     * @param metric
     */
    void onCounterRemoved(String name, Counter metric);

    /**
     * Called when a {@link Histogram} is added to the registry.
     *
     * @param name      the histogram's name
     * @param histogram the histogram
     */
    void onHistogramAdded(String name, Histogram histogram);

    /**
     * Called when a {@link Histogram} is removed from the registry.
     *
     * @param name the histogram's name
     * @param metric
     */
    void onHistogramRemoved(String name, Histogram metric);

    /**
     * Called when a {@link Meter} is added to the registry.
     *
     * @param name  the meter's name
     * @param meter the meter
     */
    void onMeterAdded(String name, Meter meter);

    /**
     * Called when a {@link Meter} is removed from the registry.
     *
     * @param name the meter's name
     * @param metric
     */
    void onMeterRemoved(String name, Meter metric);

    /**
     * Called when a {@link Timer} is added to the registry.
     *
     * @param name  the timer's name
     * @param timer the timer
     */
    void onTimerAdded(String name, Timer timer);

    /**
     * Called when a {@link Timer} is removed from the registry.
     *
     * @param name the timer's name
     * @param metric
     */
    void onTimerRemoved(String name, Timer metric);
}
