package com.yammer.metrics;

/**
 * An object which can produce statistical summaries.
 */
public interface Summarizable {
    /**
     * Returns the largest recorded value.
     *
     * @return the largest recorded value
     */
    double getMax();

    /**
     * Returns the smallest recorded value.
     *
     * @return the smallest recorded value
     */
    double getMin();

    /**
     * Returns the arithmetic mean of all recorded values.
     *
     * @return the arithmetic mean of all recorded values
     */
    double getMean();

    /**
     * Returns the standard deviation of all recorded values.
     *
     * @return the standard deviation of all recorded values
     */
    double getStdDev();

    /**
     * Returns the sum of all recorded values.
     *
     * @return the sum of all recorded values
     */
    double getSum();

}
