package com.codahale.metrics.graphite;

import java.io.Closeable;
import java.io.IOException;

/**
 * Interface defining graphite client methods
 */
public interface GraphiteClient extends Closeable {

    /**
     * Connects to the server.
     *
     * @throws java.io.IOException if there is an error connecting
     */
    public void connect() throws IOException;

    /**
     * Sends the given measurement to the server.
     *
     * @param name      the name of the metric
     * @param value     the value of the metric
     * @param timestamp the timestamp of the metric
     * @throws java.io.IOException if there was an error sending the metric
     */
    public void send(String name, String value, long timestamp) throws IOException;


    /**
     * Sanitizes a string
     * @param s the string to sanitize
     * @return
     */
    public String sanitize(String s);
}
