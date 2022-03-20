package com.codahale.metrics;

/**
 * An abstraction for how time passes. It is passed to {@link Timer} to track timing.
 */
public abstract class Clock {
    /**
     * Returns the current time tick.
     *
     * @return time tick in nanoseconds
     */
    public abstract long getTick();

    /**
     * Returns the current time in milliseconds.
     *
     * @return time in milliseconds
     */
    public long getTime() {
        return System.currentTimeMillis();
    }

    /**
     * The default clock to use.
     *
     * @return the default {@link Clock} instance
     * @see Clock.UserTimeClock
     */
    public static Clock defaultClock() {
        return UserTimeClockHolder.DEFAULT;
    }

}
