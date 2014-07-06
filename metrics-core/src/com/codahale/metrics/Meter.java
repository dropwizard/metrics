package com.codahale.metrics;



/**
 * A meter metric which measures mean throughput and one-, five-, and fifteen-minute,
 * as well as one- and three-hour exponentially-weighted moving average throughputs.
 *
 * @see EWMA
 */
public abstract class Meter implements Metered {

    /**
     * Mark the occurrence of an event.
     */
    public abstract void mark();

    /**
     * Mark the occurrence of a given number of events.
     *
     * @param n the number of events
     */
    public abstract void mark(long n);

    @Override
    public abstract long getCount();

    @Override
    public abstract double getFifteenMinuteRate();

    @Override
    public abstract double getFiveMinuteRate();

    @Override
    public abstract double getOneHourRate();

    @Override
    public abstract double getThreeHourRate();
    
    @Override
    public abstract double getMeanRate();

    @Override
    public abstract double getOneMinuteRate();
}
