package com.codahale.metrics;

import java.io.Closeable;
import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

@Deprecated
public class Timer implements Metered, Sampling {

    private final io.dropwizard.metrics5.Timer delegate;

    public static class Context implements Closeable {

        private final io.dropwizard.metrics5.Timer.Context context;

        private Context(io.dropwizard.metrics5.Timer.Context context) {
            this.context = context;
        }

        public long stop() {
            return context.stop();
        }

        @Override
        public void close() {
            context.close();
        }
    }

    public Timer() {
        this(new io.dropwizard.metrics5.Timer());
    }

    public Timer(Reservoir reservoir) {
        this(reservoir, Clock.defaultClock());
    }

    public Timer(Reservoir reservoir, Clock clock) {
        this(new io.dropwizard.metrics5.Timer(reservoir.getDelegate(), clock.getDelegate()));
    }

    public Timer(io.dropwizard.metrics5.Timer delegate) {
        this.delegate = requireNonNull(delegate);
    }

    @Override
    public Snapshot getSnapshot() {
        return Snapshot.of(delegate.getSnapshot());
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

    public void update(long duration, TimeUnit unit) {
        delegate.update(duration, unit);
    }

    public void update(Duration duration) {
        delegate.update(duration);
    }

    public <T> T time(Callable<T> event) throws Exception {
        return delegate.time(event);
    }

    public void time(Runnable event) {
        delegate.time(event);
    }

    public <T> T timeSupplier(Supplier<T> event) {
        return delegate.timeSupplier(event);
    }

    public Context time() {
        return new Context(delegate.time());
    }

    public io.dropwizard.metrics5.Timer getDelegate() {
        return delegate;
    }


}
