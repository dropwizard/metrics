package io.dropwizard.metrics5.influxdb;

import java.io.Closeable;
import java.io.IOException;

public interface InfluxDbSender extends Closeable {

    /**
     * Connects to the server.
     *
     * @throws IllegalStateException if the client is already connected
     * @throws IOException           if there is an error connecting
     */
    void connect() throws IllegalStateException, IOException;

    /**
     * Sends the given measurement to the server.
     * <p>
     * <b>NOTE:</b> The caller may modify the <code>measurement</code> buffer after this call.
     * The implementation of this method MUST NOT keep any reference to the buffer after this call.
     * </p>
     *
     * @param measurement a single measurement line,
     *                    according to the InfluxDb line protocol including a trailing newline.
     * @throws IOException if there was an error sending the metric
     */
    void send(StringBuilder measurement) throws IOException;

    /**
     * Flushes buffer, if applicable
     *
     * @throws IOException if there was an error during flushing metrics to the server
     */
    void flush() throws IOException;

    /**
     * Disconnects from the server.
     *
     * @throws IOException if there is an error disconnecting
     */
    void disconnect() throws IOException;

    /**
     * Returns true if ready to send data
     */
    boolean isConnected();
}
