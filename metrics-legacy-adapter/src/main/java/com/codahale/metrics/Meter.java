package com.codahale.metrics;

import static java.util.Objects.requireNonNull;

@Deprecated
public class Meter implements Metered {

    private final io.dropwizard.metrics5.Meter delegate;

    public Meter() {
        this(new io.dropwizard.metrics5.Meter());
    }

    public Meter(Clock clock) {
        this(new io.dropwizard.metrics5.Meter(clock.getDelegate()));
    }

    public Meter(io.dropwizard.metrics5.Meter delegate) {
        this.delegate = requireNonNull(delegate);
    }

    public void mark() {
        delegate.mark();
    }

    public void mark(long n) {
        delegate.mark(n);
    }

    @Override
    public long getCount() {
        return delegate.getCount();
    }

    @Override
    public double getFifteenMinuteRate() {
        return delegate.getFifteenMinuteRate();
    }

    @Override
    public double getFiveMinuteRate() {
        return delegate.getFiveMinuteRate();
    }

    @Override
    public double getMeanRate() {
        return delegate.getMeanRate();
    }

    @Override
    public double getOneMinuteRate() {
        return delegate.getOneMinuteRate();
    }

    @Override
    public io.dropwizard.metrics5.Meter getDelegate() {
        return delegate;
    }
}
