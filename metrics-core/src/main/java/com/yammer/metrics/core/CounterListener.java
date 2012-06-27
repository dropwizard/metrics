package com.yammer.metrics.core;

/**
 * Listeners to {@link Counter} state change events.
 */
public interface CounterListener extends MetricListener {

	/**
	 * Called after the {@link Counter#clear()} event.
	 * 
	 * @param counter
	 *            the {@link Counter} whose count was cleared
	 */
	void onClear(Counter counter);

	/**
	 * Called after any of the increment or decrement events.
	 * 
	 * @param counter
	 *            the {@link Counter} whose state has changed
	 * @param delta
	 *            the amount that the {@link Counter} has changed, can be
	 *            negative
	 */
	void onUpdate(Counter counter, long delta);
}
