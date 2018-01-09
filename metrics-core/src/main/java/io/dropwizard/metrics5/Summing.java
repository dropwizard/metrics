package io.dropwizard.metrics5;

/**
 * An interface for metric types which aggregate a sum
 */
public interface Summing {

    /**
     * Return the current sum.
     *
     * @return the current sum
     */
    long getSum();
}
