package com.codahale.metrics.stub;

import com.codahale.metrics.TimerContext;

/**
 * A timing context.
 * 
 * @see Timer#time()
 */
public class TimerContextStub extends TimerContext {

	/**
	 * Stops recording the elapsed time, updates the timer and returns the
	 * elapsed time in nanoseconds.
	 */
	public long stop() {
		return 0;
	}

	@Override
	public void close() {
	}
}
