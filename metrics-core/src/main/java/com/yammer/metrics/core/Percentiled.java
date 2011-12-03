package com.yammer.metrics.core;

public interface Percentiled {

    /**
     * Returns the duration at the given percentile.
     *
     * @param percentile a percentile ({@code 0..1})
     * @return the duration at the given percentile
     */
    public double percentile(double percentile);

    /**
     * Returns an array of durations at the given percentiles.
     *
     * @param percentiles one or more percentiles ({@code 0..1})
     * @return an array of durations at the given percentiles
     */
    public Double[] percentiles(Double... percentiles);

}
