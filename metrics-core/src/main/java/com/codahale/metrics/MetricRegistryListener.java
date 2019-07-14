package com.codahale.metrics;

import java.util.EventListener;

/**
 * Listener for events from the registry.
 * Each on*Added method returns a meter corresponding to the one passed in, making it possible to wrap/proxy a meter
 * before it's added to the registry. A common use case for this is interoperability with another metrics library.
 *
 * Listeners must be thread-safe.
 */
public interface MetricRegistryListener extends EventListener {
    /**
     * Called when a {@link Gauge} is added to the registry.
     *
     * @param name the gauge's name
     * @param gauge the gauge
     * @return Original or another gauge
     */
    default Gauge<?> onGaugeAdded(String name, Gauge<?> gauge) {
        return gauge;
    }

    /**
     * Called when a {@link Gauge} is removed from the registry.
     *
     * @param name the gauge's name
     */
    default void onGaugeRemoved(String name) {
    }

    /**
     * Called when a {@link Counter} is added to the registry.
     *
     * @param name    the counter's name
     * @param counter the counter
     * @return Original or another counter
     */
    default Counter onCounterAdded(String name, Counter counter) {
        return counter;
    }

    /**
     * Called when a {@link Counter} is removed from the registry.
     *
     * @param name the counter's name
     */
    default void onCounterRemoved(String name) {
    }

    /**
     * Called when a {@link Histogram} is added to the registry.
     *
     * @param name      the histogram's name
     * @param histogram the histogram
     * @return Original or another histogram
     */
    default Histogram onHistogramAdded(String name, Histogram histogram) {
        return histogram;
    }

    /**
     * Called when a {@link Histogram} is removed from the registry.
     *
     * @param name the histogram's name
     */
    default void onHistogramRemoved(String name) {
    }

    /**
     * Called when a {@link Meter} is added to the registry.
     *
     * @param name  the meter's name
     * @param meter the meter
     * @return Original or another meter
     */
    default Meter onMeterAdded(String name, Meter meter) {
        return meter;
    }

    /**
     * Called when a {@link Meter} is removed from the registry.
     *
     * @param name the meter's name
     */
    default void onMeterRemoved(String name) {
    }

    /**
     * Called when a {@link Timer} is added to the registry.
     *
     * @param name  the timer's name
     * @param timer the timer
     * @return Original or another timer
     */
    default Timer onTimerAdded(String name, Timer timer) {
        return timer;
    }

    /**
     * Called when a {@link Timer} is removed from the registry.
     *
     * @param name the timer's name
     */
    default void onTimerRemoved(String name) {
    }
}
