package com.codahale.metrics;

import static java.util.Objects.requireNonNull;

@Deprecated
public class Histogram implements Metric, Sampling, Counting {

    private final io.dropwizard.metrics5.Histogram delegate;

    public Histogram(io.dropwizard.metrics5.Histogram delegate) {
        this.delegate = requireNonNull(delegate);
    }

    public Histogram(Reservoir reservoir) {
        this.delegate = new io.dropwizard.metrics5.Histogram(reservoir.getDelegate());
    }

    public void update(int value) {
        delegate.update(value);
    }

    public void update(long value) {
        delegate.update(value);
    }

    @Override
    public long getCount() {
        return delegate.getCount();
    }

    @Override
    public Snapshot getSnapshot() {
        return Snapshot.of(delegate.getSnapshot());
    }

    @Override
    public io.dropwizard.metrics5.Histogram getDelegate() {
        return delegate;
    }
}
