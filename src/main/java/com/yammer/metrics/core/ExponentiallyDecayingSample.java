package com.yammer.metrics.core;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;
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
	static class Value implements Comparable<Value> {
		public final double priority;
		public final long value, id;

		Value(long id, long value, double priority) {
			this.id = id;
			this.value = value;
			this.priority = priority;
		}

		// the lowest value has the highest priority
		@Override
		public int compareTo(Value o) {
			if (o.priority > priority) {
				return -1;
			} else if (o.priority < priority) {
				return 1;
			}
			return 0;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) { return true; }
			if (o == null || getClass() != o.getClass()) { return false; }

			final Value value1 = (Value) o;

			return id == value1.id &&
					Double.compare(value1.priority, priority) == 0 &&
					value == value1.value;
		}

		@Override
		public int hashCode() {
			int result;
			long temp;
			temp = priority != +0.0d ? Double.doubleToLongBits(priority) : 0L;
			result = (int) (temp ^ (temp >>> 32));
			result = 31 * result + (int) (value ^ (value >>> 32));
			result = 31 * result + (int) (id ^ (id >>> 32));
			return result;
		}
	}

	private final PriorityBlockingQueue<Value> values;
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
		this.values = new PriorityBlockingQueue<Value>(reservoirSize);
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
		final double random = random();
		final double priority = weight(timestamp - startTime) / random;
		final long newCount = count.incrementAndGet();

		if (newCount <= reservoirSize) {
			values.put(new Value(newCount, value, priority));
		} else {
			if (values.peek().priority < priority) {
				values.add(new Value(newCount, value, priority));
				try {
					values.take();// this may remove the just-added value; that's OK
				} catch (InterruptedException ignored) {}
			}
		}
	}

	@Override
	public List<Long> values() {
		final List<Long> v = new ArrayList<Long>(size());
		for (Value value : values) {
			v.add(value.value);
		}
		return v;
	}

	private long tick() { return System.currentTimeMillis() / 1000; }

	private double weight(long t) {
		return exp(alpha * t);
	}
}
