package com.codahale.metrics;

import static java.util.Objects.requireNonNull;

@Deprecated
public class SlidingWindowReservoir implements Reservoir {

    private io.dropwizard.metrics5.SlidingWindowReservoir delegate;

    public SlidingWindowReservoir(int size) {
        this(new io.dropwizard.metrics5.SlidingWindowReservoir(size));
    }

    public SlidingWindowReservoir(io.dropwizard.metrics5.SlidingWindowReservoir delegate) {
        this.delegate = requireNonNull(delegate);
    }

    @Override
    public io.dropwizard.metrics5.Reservoir getDelegate() {
        return delegate;
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public void update(long value) {
        delegate.update(value);
    }

    @Override
    public Snapshot getSnapshot() {
        return Snapshot.of(delegate.getSnapshot());
    }
}
