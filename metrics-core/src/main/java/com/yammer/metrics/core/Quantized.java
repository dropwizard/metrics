package com.yammer.metrics.core;

import com.yammer.metrics.stats.Snapshot;

/**
 * An object which can produce quantiles.
 */
public interface Quantized {
    /**
     * Returns a snapshot of the values.
     *
     * @return a snapshot of the values
     */
    Snapshot getSnapshot();
}
