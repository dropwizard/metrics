package io.dropwizard.metrics.collectd;

import java.io.Closeable;
import java.io.IOException;

public interface CollectdSender extends Closeable {

	/**
	 * Connects to the server.
	 *
	 * @throws IllegalStateException
	 *             if the client is already connected
	 * @throws IOException
	 *             if there is an error connecting
	 */
	public void connect() throws IllegalStateException, IOException;

	/**
	 * Sends the given packet to the server.
	 *
	 * @param packet
	 *            the packet
	 * @throws IOException
	 *             if there was an error sending the metric
	 */
	public void send(final Packet packet) throws IOException;

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
}