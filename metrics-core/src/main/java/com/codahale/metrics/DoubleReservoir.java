package com.codahale.metrics;

/**
 * A statistically representative reservoir of a data stream.
 */
public interface DoubleReservoir {
    /**
     * Returns the number of values recorded.
     *
     * @return the number of values recorded
     */
    int size();

    /**
     * Adds a new recorded value to the reservoir.
     *
     * @param value a new recorded value
     */
    void update(double value);

    /**
     * Returns a snapshot of the reservoir's values.
     *
     * @return a snapshot of the reservoir's values
     */
    DoubleSnapshot getSnapshot();
}
