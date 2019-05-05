package com.codahale.metrics;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

/**
 * A meter metric which measures mean throughput and one-, five-, and fifteen-minute
 * moving average throughputs.
 *
 * @see MovingAverages
 */
public class Meter implements Metered {

    private final MovingAverages movingAverages;
    private final LongAdder count = new LongAdder();
    private final long startTime;
    private final Clock clock;

    /**
     * Creates a new {@link Meter}.
     *
     * @param movingAverages the {@link MovingAverages} implementation to use
     */
    public Meter(MovingAverages movingAverages) {
        this(movingAverages, Clock.defaultClock());
    }

    /**
     * Creates a new {@link Meter}.
     */
    public Meter() {
        this(Clock.defaultClock());
    }

    /**
     * Creates a new {@link Meter}.
     *
     * @param clock the clock to use for the meter ticks
     */
    public Meter(Clock clock) {
        this(new ExponentialMovingAverages(clock), clock);
    }

    /**
     * Creates a new {@link Meter}.
     *
     * @param movingAverages the {@link MovingAverages} implementation to use
     * @param clock          the clock to use for the meter ticks
     */
    public Meter(MovingAverages movingAverages, Clock clock) {
        this.movingAverages = movingAverages;
        this.clock = clock;
        this.startTime = this.clock.getTick();
    }

    /**
     * Mark the occurrence of an event.
     */
    public void mark() {
        mark(1);
    }

    /**
     * Mark the occurrence of a given number of events.
     *
     * @param n the number of events
     */
    public void mark(long n) {
        movingAverages.tickIfNecessary();
        count.add(n);
        movingAverages.update(n);
    }

    @Override
    public long getCount() {
        return count.sum();
    }

    @Override
    public double getFifteenMinuteRate() {
        movingAverages.tickIfNecessary();
        return movingAverages.getM15Rate();
    }

    @Override
    public double getFiveMinuteRate() {
        movingAverages.tickIfNecessary();
        return movingAverages.getM5Rate();
    }

    @Override
    public double getMeanRate() {
        if (getCount() == 0) {
            return 0.0;
        } else {
            final double elapsed = clock.getTick() - startTime;
            return getCount() / elapsed * TimeUnit.SECONDS.toNanos(1);
        }
    }

    @Override
    public double getOneMinuteRate() {
        movingAverages.tickIfNecessary();
        return movingAverages.getM1Rate();
    }
}
