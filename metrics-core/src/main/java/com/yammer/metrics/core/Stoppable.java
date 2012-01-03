package com.yammer.metrics.core;

/**
 * Interface for {@link Metric} instances that can be stopped.
 */
public interface Stoppable {
    /**
     * Stop the instance.
     */
    void stop();
}
