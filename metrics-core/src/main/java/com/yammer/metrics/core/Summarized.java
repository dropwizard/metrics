package com.yammer.metrics.core;

/**
 * An object which can produce statistical summaries.
 */
public interface Summarized {
    /**
     * Returns the largest recorded value.
     *
     * @return the largest recorded value
     */
    double max();

    /**
     * Returns the smallest recorded value.
     *
     * @return the smallest recorded value
     */
    double min();

    /**
     * Returns the arithmetic mean of all recorded values.
     *
     * @return the arithmetic mean of all recorded values
     */
    double mean();

    /**
     * Returns the standard deviation of all recorded values.
     *
     * @return the standard deviation of all recorded values
     */
    double stdDev();

}
