package com.codahale.metrics;

import java.util.concurrent.TimeUnit;

import static java.util.Objects.requireNonNull;

@Deprecated
public class SlidingTimeWindowReservoir implements Reservoir {

    private io.dropwizard.metrics5.SlidingTimeWindowReservoir delegate;

    public SlidingTimeWindowReservoir(long window, TimeUnit windowUnit) {
        this(new io.dropwizard.metrics5.SlidingTimeWindowReservoir(window, windowUnit));
    }

    public SlidingTimeWindowReservoir(long window, TimeUnit windowUnit, Clock clock) {
        this(new io.dropwizard.metrics5.SlidingTimeWindowReservoir(window, windowUnit, clock.getDelegate()));
    }

    public SlidingTimeWindowReservoir(io.dropwizard.metrics5.SlidingTimeWindowReservoir delegate) {
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
