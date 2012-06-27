package com.yammer.metrics.core;

/**
 * Listeners to {@link Meter} state change events.
 */
public interface MeterListener extends MetricListener {

	/**
	 * Called after any of the mark events, {@link Meter#mark()} or
	 * {@link Meter#mark(long)}.
	 * 
	 * @param meter
	 *            the {@link Meter} whose state has changed
	 * @param n
	 *            the number of events that have been marked on the
	 *            {@link Meter}
	 */
	void onMark(Meter meter, long n);
}
