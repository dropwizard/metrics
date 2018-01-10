package com.codahale.metrics;

import static java.util.Objects.requireNonNull;

@Deprecated
public class ExponentiallyDecayingReservoir implements Reservoir {

    private final io.dropwizard.metrics5.ExponentiallyDecayingReservoir delegate;

    public ExponentiallyDecayingReservoir() {
        this(new io.dropwizard.metrics5.ExponentiallyDecayingReservoir());
    }

    public ExponentiallyDecayingReservoir(int size, double alpha) {
        this(new io.dropwizard.metrics5.ExponentiallyDecayingReservoir(size, alpha));
    }

    public ExponentiallyDecayingReservoir(int size, double alpha, Clock clock) {
        this(new io.dropwizard.metrics5.ExponentiallyDecayingReservoir(size, alpha, clock.getDelegate()));
    }

    public ExponentiallyDecayingReservoir(io.dropwizard.metrics5.ExponentiallyDecayingReservoir delegate) {
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
