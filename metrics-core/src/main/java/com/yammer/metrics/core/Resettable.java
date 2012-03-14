package com.yammer.metrics.core;

/**
 * An interface to indicate that a metric is resettable
 */
public interface Resettable {
    /**
     * Resets the internal counter to zero.
     */
    void reset();
}
