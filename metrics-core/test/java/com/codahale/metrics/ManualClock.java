package com.codahale.metrics;

import java.util.concurrent.TimeUnit;

public class ManualClock extends Clock {
    long ticksInNanos = 0;

    public synchronized void addNanos(long nanos) {
        ticksInNanos += nanos;
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
        return TimeUnit.NANOSECONDS.toMillis(ticksInNanos);
    }
    
}
