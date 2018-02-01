package com.codahale.metrics;

import java.util.concurrent.TimeUnit;

import static java.util.Objects.requireNonNull;

@Deprecated
public abstract class CachedGauge<T> implements Gauge<T> {

    private final io.dropwizard.metrics5.CachedGauge<T> gauge;

    public CachedGauge(io.dropwizard.metrics5.CachedGauge<T> gauge) {
        this.gauge = requireNonNull(gauge);
    }

    protected CachedGauge(long timeout, TimeUnit timeoutUnit) {
        final CachedGauge<T> original = this;
        gauge = new io.dropwizard.metrics5.CachedGauge<T>(timeout, timeoutUnit) {
            @Override
            protected T loadValue() {
                return original.loadValue();
            }
        };
    }

    protected CachedGauge(Clock clock, long timeout, TimeUnit timeoutUnit) {
        final CachedGauge<T> original = this;
        gauge = new io.dropwizard.metrics5.CachedGauge<T>(clock.getDelegate(), timeout, timeoutUnit) {
            @Override
            protected T loadValue() {
                return original.loadValue();
            }
        };
    }

    protected abstract T loadValue();

    public T getValue() {
        return gauge.getValue();
    }

}
