package com.codahale.metrics;

/**
 * An interface for metric types which have enable/disable feature.
 */
public interface Toggleable {

    /**
     * Enable and disable this metric
     * @param enabled new value for enabled
     */
    void setEnabled(boolean enabled);
}
