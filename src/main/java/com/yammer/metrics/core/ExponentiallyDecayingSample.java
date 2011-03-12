package com.yammer.metrics.core;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;

import static java.lang.Math.*;

/**
 * An exponentially-decaying random sample of {@code long}s. Uses Cormode et
 * al's forward-decaying priority reservoir sampling method to produce a
 * statistically representative sample, exponentially biased towards newer
 * entries.
 *
 * @see <a href="http://www.research.att.com/people/Cormode_Graham/library/publications/CormodeShkapenyukSrivastavaXu09.pdf">
 * Cormode et al. Forward Decay: A Practical Time Decay Model for Streaming
 * Systems. ICDE '09: Proceedings of the 2009 IEEE International Conference on
 * Data Engineering (2009)</a>
 */
public class ExponentiallyDecayingSample implements Sample {
	private final ConcurrentSkipListMap<Double, Long> values;
	private final double alpha;
	private final int reservoirSize;
	private final AtomicLong count = new AtomicLong();
	private volatile long startTime;

	/**
	 * Creates a new {@link ExponentiallyDecayingSample}.
	 *
	 * @param reservoirSize the number of samples to keep in the sampling
	 *                      reservoir
	 * @param alpha the exponential decay factor; the higher this is, the more
	 *              biased the sample will be towards newer values
	 */
	public ExponentiallyDecayingSample(int reservoirSize, double alpha) {
		this.values = new ConcurrentSkipListMap<Double, Long>();
		this.alpha = alpha;
		this.reservoirSize = reservoirSize;
		clear();
	}

	@Override
	public void clear() {
		values.clear();
		count.set(0);
		this.startTime = tick();
	}

	@Override
	public int size() {
		return (int) min(reservoirSize, count.get());
	}

	@Override
	public void update(long value) {
		update(value, tick());
	}

	/**
	 * Adds an old value with a fixed timestamp to the sample.
	 *
	 * @param value the value to be added
	 * @param timestamp the epoch timestamp of {@code value} in milliseconds
	 */
	public void update(long value, long timestamp) {
		final double priority = weight(timestamp - startTime) / random();
		final long newCount = count.incrementAndGet();

		if (newCount <= reservoirSize) {
			values.put(priority, value);
		} else {
			Double first = values.firstKey();
			if (first < priority) {
				values.put(priority, value);

				// ensure we always remove an item
				while (values.remove(first) == null) {
					first = values.firstKey();
				}
			}
		}
	}

	@Override
	public List<Long> values() {
		return new ArrayList<Long>(values.values());
	}

	private long tick() { return System.currentTimeMillis() / 1000; }

	private double weight(long t) {
		return exp(alpha * t);
	}
}
