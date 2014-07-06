package com.codahale.metrics;

import java.io.Closeable;

/**
 * A timing context.
 * 
 * @see Timer#time()
 */
public abstract class TimerContext implements Closeable {

	/**
	 * Stops recording the elapsed time, updates the timer and returns the
	 * elapsed time in nanoseconds.
	 */
	public abstract long stop();

	@Override
	public abstract void close();
}
