package com.codahale.metrics;

/**
 * An object which samples values.
 */
public interface DoubleSampling {
    /**
     * Returns a snapshot of the values.
     *
     * @return a snapshot of the values
     */
    DoubleSnapshot getSnapshot();
}
