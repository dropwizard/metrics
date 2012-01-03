package com.yammer.metrics.core;

/**
 * An object which can produce quantiles.
 */
public interface Quantized {
    /**
     * Returns the duration at the given quantile.
     *
     * @param quantile a quantile ({@code 0..1})
     * @return the duration at the given quantile
     */
    double quantile(double quantile);

    /**
     * Returns an array of durations at the given quantiles.
     *
     * @param quantiles one or more quantiles ({@code 0..1})
     * @return an array of durations at the given quantiles
     */
    Double[] quantiles(Double... quantiles);
}
