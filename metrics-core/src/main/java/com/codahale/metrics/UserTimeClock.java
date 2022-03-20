package com.codahale.metrics;

/**
 * A clock implementation which returns the current time in epoch nanoseconds.
 */
public class UserTimeClock extends Clock {
    @Override
    public long getTick() {
        return System.nanoTime();
    }
}