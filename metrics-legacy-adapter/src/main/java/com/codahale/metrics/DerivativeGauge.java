package com.codahale.metrics;

@Deprecated
public abstract class DerivativeGauge<F, T> implements Gauge<T> {

    private final io.dropwizard.metrics5.DerivativeGauge<F, T> delegate;

    protected DerivativeGauge(Gauge<F> base) {
        DerivativeGauge<F, T> original = this;
        delegate = new io.dropwizard.metrics5.DerivativeGauge<F, T>(base.getDelegate()) {
            @Override
            protected T transform(F value) {
                return original.transform(base.getValue());
            }
        };
    }

    protected abstract T transform(F value);

    @Override
    public T getValue() {
        return delegate.getValue();
    }
}
