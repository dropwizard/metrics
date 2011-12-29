package com.yammer.metrics.core;

import java.util.concurrent.TimeUnit;

/**
 * A timing context.
 *
 * @see Timer#time()
 */
public class TimerContext {
    private final Timer timer;
    private final long startTime;

    /**
     * Creates a new {@link TimerContext} with the current time as its starting value and with the
     * given {@link Timer}.
     *
     * @param timer the {@link Timer} to report the elapsed time to
     */
    TimerContext(Timer timer) {
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
