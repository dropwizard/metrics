package com.codahale.metrics;

/**
 * <p>
 * Similar to {@link Gauge}, but metric value is updated via calling {@link #setValue(T)} instead.
 * Thread-safety is ensured via storing metric value as a volatile member variable.
 * </p>
 */
public class SimpleSettableGauge<T> implements SettableGauge<T> {
    /**
     * Current value.
     * Volatile so that assignment is thread-safe.
     * <a href="http://docs.oracle.com/javase/specs/jls/se14/html/jls-17.html#jls-17.7">See 17.7</a>
     */
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
