package com.yammer.metrics.core;

import java.util.concurrent.TimeUnit;

/**
 * Interface to indicate a metric that involves timing.
 */
public interface Timeable {
    /**
     * Adds a recorded duration.
     *
     * @param duration the length of the duration
     * @param unit     the scale unit of {@code duration}
     */
    public void update(long duration, TimeUnit unit);

}
