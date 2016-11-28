package io.dropwizard.metrics;

/**
 * An interface for metric types which have counts.
 */
public interface Counting {
    /**
     * Returns the current count.
     *
     * @return the current count
     */
    long getCount();
}
