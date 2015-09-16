package io.dropwizard.metrics.atsd;

import io.dropwizard.metrics.MetricName;

import java.io.Closeable;
import java.io.IOException;

public interface AtsdSender extends Closeable {
    /**
     * Connects to the server.
     *
     * @throws IllegalStateException if the client is already connected
     * @throws IOException           if there is an error connecting
     */
    void connect() throws IllegalStateException, IOException;

    /**
     * Sends the given measurement to the server.
     *
     * @param entity    the name of the entity
     * @param metric    the MetricName instance
     * @param value     the metric value
     * @param timestamp the timestamp of the metric
     * @throws IOException if there was an error sending the metric
     */
    void send(String entity, MetricName metric, String value, long timestamp)
            throws IOException;

    /**
     * Flushes buffer, if applicable
     *
     * @throws IOException
     */
    void flush() throws IOException;

    /**
     * Returns true if ready to send data
     */
    boolean isConnected();

    /**
     * Returns the number of failed writes to the server.
     *
     * @return the number of failed writes to the server
     */
    int getFailures();
}
