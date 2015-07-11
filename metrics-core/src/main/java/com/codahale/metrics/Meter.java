package com.codahale.metrics;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A meter metric which measures mean throughput and one-, five-, and fifteen-minute
 * exponentially-weighted moving average throughputs.
 *
 * @see EWMA
 */
public class Meter extends ConfigurableMeter implements Metered {

    /**
     * Creates a new {@link Meter}.
     */
    public Meter() {
        this(Clock.defaultClock());
    }

    /**
     * Creates a new {@link Meter}.
     *
     * @param clock      the clock to use for the meter ticks
     */
    public Meter(Clock clock) {
        super(clock, TimeUnit.MINUTES, new long[] {1, 5, 15});
    }

    @Override
    public double getFifteenMinuteRate() {
        try {
            return getRate(15, TimeUnit.MINUTES);
        } catch (InvalidInterval exc) {
            return 0.0; // not possible
        }
    }

    @Override
    public double getFiveMinuteRate() {
        try {
            return getRate(5, TimeUnit.MINUTES);
        } catch (InvalidInterval exc) {
            return 0.0; // not possible
        }
    }

    @Override
    public double getOneMinuteRate() {
        try {
            return getRate(1, TimeUnit.MINUTES);
        } catch (InvalidInterval exc) {
            return 0.0; // not possible
        }
    }
}
