package com.yammer.metrics.core;

import java.util.concurrent.TimeUnit;

public class TimerContext implements AutoCloseable {
    public static TimerContext time(TimerMetric timer) {
        return new TimerContext(timer);
    }

    private final TimerMetric timer;
    private final long startTime;
    
    public TimerContext(TimerMetric timer) {
        this.timer = timer;
        this.startTime = System.nanoTime();
    }

    @Override
    public void close() {
        timer.update(System.nanoTime() - startTime, TimeUnit.NANOSECONDS);
    }
}
