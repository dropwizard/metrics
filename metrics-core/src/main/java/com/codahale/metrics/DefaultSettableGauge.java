package com.codahale.metrics;

/**
 * Similar to {@link Gauge}, but metric value is updated via calling {@link #setValue(T)} instead.
 */
public class DefaultSettableGauge<T> implements SettableGauge<T> {
    private volatile T value;

    /**
     * Create an instance with no default value.
     */
    public DefaultSettableGauge() {
        this(null);
    }

    /**
     * Create an instance with a default value.
     *
     * @param defaultValue default value
     */
    public DefaultSettableGauge(T defaultValue) {
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
     * Returns the current value.
     *
     * @return the current value
     */
    @Override
    public T getValue() {
        return value;
    }

}
