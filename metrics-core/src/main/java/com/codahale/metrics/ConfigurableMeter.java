package com.codahale.metrics;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static java.lang.Math.exp;

/**
 * A meter metric which measures mean throughput and exponentially-weighted moving averages over any
 * user-specified intervals.
 *
 * @see EWMA
 */
public class ConfigurableMeter
{
    private static final int INTERVAL = 5;
    private static final long TICK_INTERVAL = TimeUnit.SECONDS.toNanos(5);
    private final Map<Long, EWMA> ewmasByRate;

    private final LongAdder count = new LongAdder();
    private final long startTime;
    private final AtomicLong lastTick;
    private final Clock clock;

    /**
     * Creates a new {@link ConfigurableMeter} with the default clock.
     *
     * @param intervalUnit the time unit that all of the intervals will be expressed in
     * @param intervals the intervals over which this meter should calculate a rate
     */
    public ConfigurableMeter(TimeUnit intervalUnit, long[] intervals) {
        this(Clock.defaultClock(), intervalUnit, intervals);
    }

    /**
     * Creates a new {@link ConfigurableMeter}.
     *
     * @param clock the clock to use for the meter ticks
     * @param intervalUnit the time unit that all of the intervals will be expressed in
     * @param intervals the intervals over which this meter should calculate a rate
     */
    public ConfigurableMeter(Clock clock, TimeUnit intervalUnit, long[] intervals) {
        this.clock = clock;
        this.ewmasByRate = new HashMap<Long, EWMA>(intervals.length);
        for (long interval : intervals) {
            long intervalInSeconds = intervalUnit.toSeconds(interval);
            double alpha = 1 - exp(((double) -INTERVAL) / intervalInSeconds);
            ewmasByRate.put(intervalInSeconds, new EWMA(alpha, INTERVAL, TimeUnit.SECONDS));
        }
        this.startTime = this.clock.getTick();
        this.lastTick = new AtomicLong(startTime);
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
        tickIfNecessary();
        count.add(n);
        for (EWMA ewma : ewmasByRate.values())
            ewma.update(n);
    }

    protected void tickIfNecessary() {
        final long oldTick = lastTick.get();
        final long newTick = clock.getTick();
        final long age = newTick - oldTick;
        if (age > TICK_INTERVAL) {
            final long newIntervalStartTick = newTick - age % TICK_INTERVAL;
            if (lastTick.compareAndSet(oldTick, newIntervalStartTick)) {
                final long requiredTicks = age / TICK_INTERVAL;
                for (long i = 0; i < requiredTicks; i++) {
                    for (EWMA ewma : ewmasByRate.values())
                        ewma.tick();
                }
            }
        }
    }

    public long getCount() {
        return count.sum();
    }

    public double getMeanRate() {
        if (getCount() == 0) {
            return 0.0;
        } else {
            final double elapsed = (clock.getTick() - startTime);
            return getCount() / elapsed * TimeUnit.SECONDS.toNanos(1);
        }
    }

    /**
     * Gets the metered rate over the specified interval, which must match one of the intervals provided
     * when constructing this object.
     * @param interval the interval for which a rate should be returned
     * @param intervalUnit the unit of time that the interval is expressed in
     * @return the metered rate in terms of actions per second
     * @throws InvalidInterval if an interval is specified that does not match one of the intervals
     *         used when constructing this object
     */
    public double getRate(long interval, TimeUnit intervalUnit) throws InvalidInterval {
        tickIfNecessary();
        EWMA ewma = ewmasByRate.get(intervalUnit.toSeconds(interval));
        if (ewma == null)
            throw new InvalidInterval();
        return ewma.getRate(TimeUnit.SECONDS);
    }

    class InvalidInterval extends Exception { }
}