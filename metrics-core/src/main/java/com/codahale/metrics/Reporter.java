package com.codahale.metrics;

import java.io.Closeable;

/**
 * Base interface for all reporter implementations.
 */
public interface Reporter extends Closeable {

    /**
     * Stops the reporter.
     */
    void stop();
}
