package com.yammer.metrics.core;

public class TimerContext extends TimerMetric.Context implements AutoCloseable {
    public static TimerContext time(TimerMetric timer) {
        return new TimerContext(timer);
    }
    
    public TimerContext(TimerMetric timer) {
        super(timer);
    }

    @Override
    public void close() {
        stop();
    }
}
