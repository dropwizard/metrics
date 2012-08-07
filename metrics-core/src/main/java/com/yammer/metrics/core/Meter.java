package com.yammer.metrics.core;

import com.yammer.metrics.stats.EWMA;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A meter metric which measures mean throughput and one-, five-, and fifteen-minute
 * exponentially-weighted moving average throughputs.
 *
 * @see <a href="http://en.wikipedia.org/wiki/Moving_average#Exponential_moving_average">EMA</a>
 */
public class Meter implements Metered {
    private static final long TICK_INTERVAL = TimeUnit.SECONDS.toNanos(5);

    private final EWMA m1Rate = EWMA.oneMinuteEWMA();
    private final EWMA m5Rate = EWMA.fiveMinuteEWMA();
    private final EWMA m15Rate = EWMA.fifteenMinuteEWMA();

    private final AtomicLong count = new AtomicLong();
    private final long startTime;
    private final AtomicLong lastTick;
    private final TimeUnit rateUnit;
    private final String eventType;
    private final Clock clock;

    /**
     * Creates a new {@link Meter}.
     *
     * @param eventType  the plural name of the event the meter is measuring (e.g., {@code
     *                   "requests"})
     * @param rateUnit   the rate unit of the new meter
     * @param clock      the clock to use for the meter ticks
     */
    Meter(String eventType, TimeUnit rateUnit, Clock clock) {
        this.rateUnit = rateUnit;
        this.eventType = eventType;
        this.clock = clock;
        this.startTime = this.clock.getTick();
        this.lastTick = new AtomicLong(startTime);
    }

    @Override
    public TimeUnit getRateUnit() {
        return rateUnit;
    }

    @Override
    public String getEventType() {
        return eventType;
    }

    /**
     * Updates the moving averages.
     */
    void tick() {
        m1Rate.tick();
        m5Rate.tick();
        m15Rate.tick();
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
        count.addAndGet(n);
        m1Rate.update(n);
        m5Rate.update(n);
        m15Rate.update(n);
    }

    private void tickIfNecessary() {
        final long oldTick = lastTick.get();
        final long newTick = clock.getTick();
        final long age = newTick - oldTick;
        if (age > TICK_INTERVAL && lastTick.compareAndSet(oldTick, newTick)) {
            final long requiredTicks = age / TICK_INTERVAL;
            for (long i = 0; i < requiredTicks; i++) {
                tick();
            }
        }
    }

    @Override
    public long getCount() {
        return count.get();
    }

    @Override
    public double getFifteenMinuteRate() {
        tickIfNecessary();
        return m15Rate.getRate(rateUnit);
    }

    @Override
    public double getFiveMinuteRate() {
        tickIfNecessary();
        return m5Rate.getRate(rateUnit);
    }

    @Override
    public double getMeanRate() {
        if (getCount() == 0) {
            return 0.0;
        } else {
            final long elapsed = (clock.getTick() - startTime);
            return convertNsRate(getCount() / (double) elapsed);
        }
    }

    @Override
    public double getOneMinuteRate() {
        tickIfNecessary();
        return m1Rate.getRate(rateUnit);
    }

    private double convertNsRate(double ratePerNs) {
        return ratePerNs * (double) rateUnit.toNanos(1);
    }
}
