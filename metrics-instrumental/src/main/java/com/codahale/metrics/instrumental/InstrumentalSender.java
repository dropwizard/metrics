package com.codahale.metrics.instrumental;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by bvarner on 1/6/15.
 */
public interface InstrumentalSender extends Closeable {
	public void connect() throws IllegalStateException, IOException;

	public void send(String name, String value, long timestamp) throws IOException;

	void flush() throws IOException;

	boolean isConnected();

	public int getFailures();
}
