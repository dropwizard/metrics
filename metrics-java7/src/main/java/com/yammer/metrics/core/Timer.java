package com.yammer.metrics.core;

public class Timer extends TimerContext implements AutoCloseable {
    public static Timer time(TimerMetric timer) {
        return new Timer(timer);
    }
    
    public Timer(TimerMetric timer) {
        super(timer);
    }

    @Override
    public void close() {
        stop();
    }
}
