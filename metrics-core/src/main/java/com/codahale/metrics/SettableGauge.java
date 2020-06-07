package com.codahale.metrics;

/**
 * <p>
 * Similar to {@link Gauge}, but metric value is updated via calling {@link #setValue(T)} instead.
 * See {@link SimpleSettableGauge}.
 * </p>
 */
public interface SettableGauge<T> extends Gauge<T> {
    /**
     * Set the metric to a new value.
     */
    void setValue(T value);
}
