package com.yammer.metrics.core;

import java.util.concurrent.TimeUnit;

/**
 * A timing context.
 *
 * @see com.yammer.metrics.core.TimerMetric#time()
 */
public class TimerContext {
    private final TimerMetric timer;
    private final long startTime;

    /**
     * Creates a new {@link TimerContext} with the current time as its starting value and with the
     * given {@link com.yammer.metrics.core.TimerMetric}.
     *
     * @param timer the {@link com.yammer.metrics.core.TimerMetric} to report the elapsed time to
     */
    TimerContext(TimerMetric timer) {
        this.timer = timer;
        this.startTime = System.nanoTime();
    }

    /**
     * Stops recording the elapsed time and updates the timer.
     */
    public void stop() {
        timer.update(System.nanoTime() - startTime, TimeUnit.NANOSECONDS);
    }
}
