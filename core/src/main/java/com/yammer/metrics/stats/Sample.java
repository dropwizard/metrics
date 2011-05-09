package com.yammer.metrics.stats;

import java.util.List;

/**
 * A statistically representative sample of a data stream.
 *
 * @author coda
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
     * Returns a copy of the sample's values.
     *
     * @return a copy of the sample's values
     */
    List<Long> values();
}
