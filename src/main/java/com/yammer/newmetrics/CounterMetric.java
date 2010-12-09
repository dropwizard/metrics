package com.yammer.newmetrics;

import java.util.concurrent.atomic.AtomicLong;

/**
 * An incrementing and decrementing counter metric.
 *
 * @author coda
 */
public class CounterMetric implements Metric {
	private final AtomicLong count = new AtomicLong();

	/**
	 * Increment the counter by one.
	 */
	public void inc() {
		inc(1);
	}

	/**
	 * Increment the counter by {@code n}.
	 *
	 * @param n the amount by which the counter will be increased
	 */
	public void inc(long n) {
		count.addAndGet(n);
	}

	/**
	 * Decrement the counter by one.
	 */
	public void dec() {
		dec(1);
	}

	/**
	 * Decrement the counter by {@code n}
	 *
	 * @param n the amount by which the counter will be increased
	 */
	public void dec(long n) {
		count.addAndGet(0 - n);
	}

	/**
	 * Returns the counter's current value.
	 *
	 * @return the counter's current value
	 */
	public long count() {
		return count.get();
	}
}
