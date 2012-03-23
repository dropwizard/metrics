package com.yammer.metrics.core;

import com.yammer.metrics.annotation.Publish;
import com.yammer.metrics.stats.Snapshot;

/**
 * An object which samples values.
 */
public interface Sampling {
    /**
     * Returns a snapshot of the values.
     *
     * @return a snapshot of the values
     */
    Snapshot getSnapshot();

    /**
     * Returns the median value in the distribution.
     *
     * @return the median value in the distribution
     */
    double getMedian();

    /**
     * Returns the value at the 75th percentile in the distribution.
     *
     * @return the value at the 75th percentile in the distribution
     */
    double get75thPercentile();

    /**
     * Returns the value at the 95th percentile in the distribution.
     *
     * @return the value at the 95th percentile in the distribution
     */
    double get95thPercentile();

    /**
     * Returns the value at the 98th percentile in the distribution.
     *
     * @return the value at the 98th percentile in the distribution
     */
    double get98thPercentile();

    /**
     * Returns the value at the 99th percentile in the distribution.
     *
     * @return the value at the 99th percentile in the distribution
     */
    double get99thPercentile();

    /**
     * Returns the value at the 99.9th percentile in the distribution.
     *
     * @return the value at the 99.9th percentile in the distribution
     */
    double get999thPercentile();
}
