package com.yammer.metrics.stats;

/**
 * A statistically representative sample of a data stream.
 */
public interface Sample {
    /**
     * Clears all recorded values.
     */
    void clear();

    /**
     * Returns the number of values recorded.
     *
     * @return the number of values recorded
     */
    int size();

    /**
     * Adds a new recorded value to the sample.
     *
     * @param value a new recorded value
     */
    void update(long value);

    /**
     * Returns a snapshot of the sample's values.
     *
     * @return a snapshot of the sample's values
     */
    Snapshot getSnapshot();
}
