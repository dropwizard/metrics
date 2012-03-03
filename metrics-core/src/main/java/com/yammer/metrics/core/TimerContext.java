package com.yammer.metrics.core;

import java.util.concurrent.TimeUnit;

/**
 * A timing context.
 *
 * @see Timer#time()
 */
public class TimerContext {
    private final Timer timer;
    private final Clock clock;
    private final long startTime;

    /**
     * Creates a new {@link TimerContext} with the current time as its starting value and with the
     * given {@link Timer}.
     *
     * @param timer the {@link Timer} to report the elapsed time to
     */
    TimerContext(Timer timer, Clock clock) {
        this.timer = timer;
        this.clock = clock;
        this.startTime = clock.tick();
    }

    /**
     * Stops recording the elapsed time and updates the timer.
     */
    public void stop() {
        timer.update(clock.tick() - startTime, TimeUnit.NANOSECONDS);
    }
}
