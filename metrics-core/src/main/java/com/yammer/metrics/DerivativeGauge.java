package com.yammer.metrics;

/**
 * A gauge whose value is derived from the value of another gauge.
 *
 * @param <F> the base gauge's value type
 * @param <T> the derivative type
 */
public abstract class DerivativeGauge<F, T> implements Gauge<T> {
    private final Gauge<F> base;

    /**
     * Creates a new derivative with the given base gauge.
     *
     * @param base the gauge from which to derive this gauge's value
     */
    protected DerivativeGauge(Gauge<F> base) {
        this.base = base;
    }

    @Override
    public T getValue() {
        return transform(base.getValue());
    }

    /**
     * Transforms the value of the base gauge to the value of this gauge.
     *
     * @param value the value of the base gauge
     * @return this gauge's value
     */
    protected abstract T transform(F value);
}
