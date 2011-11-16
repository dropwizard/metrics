package com.yammer.metrics.core;

public interface Summarized {

    /**
     * Returns the largest recorded value.
     *
     * @return the largest recorded value
     */
    public double max();

    /**
     * Returns the smallest recorded value.
     *
     * @return the smallest recorded value
     */
    public double min();

    /**
     * Returns the arithmetic mean of all recorded values.
     *
     * @return the arithmetic mean of all recorded values
     */
    public double mean();

    /**
     * Returns the standard deviation of all recorded values.
     *
     * @return the standard deviation of all recorded values
     */
    public double stdDev();

}