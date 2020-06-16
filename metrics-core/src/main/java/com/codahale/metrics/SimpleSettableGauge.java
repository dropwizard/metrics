package com.codahale.metrics;

/**
 * <p>
 * Similar to {@link Gauge}, but metric value is updated via calling {@link #setValue(T)} instead.
 * </p>
 */
public class SimpleSettableGauge<T> implements SettableGauge<T> {
    private volatile T value;

    /**
     * Create an instance with a default value.
     *
     * @param defaultValue default value
     */
    public SimpleSettableGauge(T defaultValue) {
        this.value = defaultValue;
    }

    /**
     * Set the metric to a new value.
     */
    @Override
    public void setValue(T value) {
        this.value = value;
    }

    /**
     * Returns the metric's current value.
     *
     * @return the metric's current value
     */
    @Override
    public T getValue() {
        return value;
    }

}
