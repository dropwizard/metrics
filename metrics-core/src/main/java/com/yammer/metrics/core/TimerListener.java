package com.yammer.metrics.core;

import java.util.concurrent.TimeUnit;

/**
 * Listeners to {@link Timer} state change events.
 */
public interface TimerListener extends MetricListener {

	/**
	 * Called after any of timing events, {@link Timer#update(long, TimeUnit)}
	 * or {@link Timer#time(java.util.concurrent.Callable)}.
	 * 
	 * @param timer
	 *            the {@link Timer} whose state has changed
	 * @param duration
	 *            the length of the duration
	 * @param unit
	 *            the scale unit of {@code duration}
	 */
	void onUpdate(Timer timer, long duration, TimeUnit unit);
}
