package com.codahale.metrics;

import java.util.concurrent.TimeUnit;

public class ManualClock extends Clock {
    private final long initialTicksInNanos;
    long ticksInNanos;

    public ManualClock(long initialTicksInNanos) {
        this.initialTicksInNanos = initialTicksInNanos;
        this.ticksInNanos = initialTicksInNanos;
    }

    public ManualClock() {
        this(0L);
    }

    public synchronized void addNanos(long nanos) {
        ticksInNanos += nanos;
    }

    public synchronized void addSeconds(long seconds) {
        ticksInNanos += TimeUnit.SECONDS.toNanos(seconds);
    }

    public synchronized void addMillis(long millis) {
        ticksInNanos += TimeUnit.MILLISECONDS.toNanos(millis);
    }

    public synchronized void addHours(long hours) {
        ticksInNanos += TimeUnit.HOURS.toNanos(hours);
    }

    @Override
    public synchronized long getTick() {
        return ticksInNanos;
    }

    @Override
    public synchronized long getTime() {
        return TimeUnit.NANOSECONDS.toMillis(ticksInNanos - initialTicksInNanos);
    }

}
