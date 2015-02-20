package com.codahale.metrics;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A {@link Gauge} implementation which caches its value for a period of time.
 *
 * @param <T>    the type of the gauge's value
 */
public abstract class CachedGauge<T> implements Gauge<T> {
    private final Clock clock;
    private final long timeoutNS;

    // Value 0 means "not yet initialized".
    private volatile long reloadAt;
    private volatile T value;

    /**
     * Creates a new cached gauge with the given timeout period.
     *
     * @param timeout        the timeout
     * @param timeoutUnit    the unit of {@code timeout}
     */
    protected CachedGauge(long timeout, TimeUnit timeoutUnit) {
        this(Clock.defaultClock(), timeout, timeoutUnit);
    }

    /**
     * Creates a new cached gauge with the given clock and timeout period.
     *
     * @param clock          the clock used to calculate the timeout
     * @param timeout        the timeout
     * @param timeoutUnit    the unit of {@code timeout}
     */
    protected CachedGauge(Clock clock, long timeout, TimeUnit timeoutUnit) {
        if (timeout <= 0) {
            throw new IllegalArgumentException("Timeout must be > 0");
        }
        this.clock = clock;
        this.timeoutNS = timeoutUnit.toNanos(timeout);
    }

    /**
     * Loads the value and returns it.
     *
     * @return the new value
     */
    protected abstract T loadValue();

    @Override
    public T getValue() {
        // Variant of Double Checked Locking.
        long nanos = reloadAt;
        long now = clock.getTick();
        if (nanos == 0 || now - nanos >= 0) {
            synchronized (this) {
                if (nanos == reloadAt) {
                    T t = loadValue();
                    value = t;
                    nanos = now + this.timeoutNS;
                    // In the very unlikely event that nanos is 0, set it to 1;
                    // no one will notice 1 ns of tardiness.
                    reloadAt = (nanos == 0) ? 1 : nanos;
                    return t;
                }
            }
        }
        return value;
    }
}
