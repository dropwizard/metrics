package com.yammer.metrics.core;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static java.lang.Double.doubleToLongBits;
import static java.lang.Double.longBitsToDouble;
import static java.lang.Math.floor;
import static java.lang.Math.sqrt;

/**
 * A timer metric which aggregates timing durations and provides latency
 * statistics, plus throughput statistics via {@link MeterMetric}.
 *
 * @author coda
 * @see <a href="http://www.johndcook.com/standard_deviation.html">Accurately
 * computing running variance</a>
 */
public class TimerMetric implements Metric {
	private final MeterMetric meter;
	// Using a sample size of 1028, which offers a 99.9% confidence level with a
	// 5% margin of error assuming a normal distribution. This might need to be
	// parameterized, but I'm only going to do that when someone complains.
	private final Sample sample = new Sample(1028);
	private final TimeUnit latencyUnit, rateUnit;
	private final AtomicLong _min = new AtomicLong();
	private final AtomicLong _max = new AtomicLong();
	private final AtomicLong _sum = new AtomicLong();
	// These are for the Welford algorithm for calculating running variance
	// without floating-point doom.
	private final AtomicLong varianceM = new AtomicLong();
	private final AtomicLong varianceS = new AtomicLong();

	/**
	 * Creates a new {@link TimerMetric}.
	 *
	 * @param latencyUnit the scale unit for this timer's latency metrics
	 * @param rateUnit the scale unit for this timer's rate metrics
	 */
	public TimerMetric(TimeUnit latencyUnit, TimeUnit rateUnit) {
		this.latencyUnit = latencyUnit;
		this.rateUnit = rateUnit;
		this.meter = MeterMetric.newMeter("calls", rateUnit);
		clear();
	}

	/**
	 * Returns the timer's latency scale unit.
	 *
	 * @return the timer's latency scale unit
	 */
	public TimeUnit getLatencyUnit() {
		return latencyUnit;
	}

	/**
	 * Returns the timer's rate scale unit.
	 *
	 * @return the timer's rate scale unit
	 */
	public TimeUnit getRateUnit() {
		return rateUnit;
	}

	/**
	 * Clears all recorded durations.
	 */
	public void clear() {
		sample.clear();
		_max.set(Long.MIN_VALUE);
		_min.set(Long.MAX_VALUE);
		_sum.set(0);
		varianceM.set(-1);
		varianceS.set(0);
	}

	/**
	 * Adds a recorded duration.
	 *
	 * @param duration the length of the duration
	 * @param unit the scale unit of {@code duration}
	 */
	public void update(long duration, TimeUnit unit) {
		update(unit.toNanos(duration));
	}

	/**
	 * Times and records the duration of event.
	 *
	 * @param event a {@link Callable} whose {@link Callable#call()} method
	 * implements a process whose duration should be timed
	 * @param <T> the type of the value returned by {@code event}
	 * @return the value returned by {@code event}
	 * @throws Exception if {@code event} throws an {@link Exception}
	 */
	public <T> T time(Callable<T> event) throws Exception {
		final long startTime = System.nanoTime();
		try {
			return event.call();
		} finally {
			update(System.nanoTime() - startTime);
		}
	}

	/**
	 * Returns the number of durations recorded.
	 *
	 * @return the number of durations recorded
	 */
	public long count() { return meter.count(); }

	/**
	 * Returns the fifteen-minute rate of timings.
	 *
	 * @return the fifteen-minute rate of timings
	 * @see MeterMetric#fifteenMinuteRate()
	 */
	public double fifteenMinuteRate() { return meter.fifteenMinuteRate(); }

	/**
	 * Returns the five-minute rate of timings.
	 *
	 * @return the five-minute rate of timings
	 * @see MeterMetric#fiveMinuteRate()
	 */
	public double fiveMinuteRate() { return meter.fiveMinuteRate(); }

	/**
	 * Returns the mean rate of timings.
	 *
	 * @return the mean rate of timings
	 * @see MeterMetric#meanRate()
	 */
	public double meanRate() { return meter.meanRate(); }

	/**
	 * Returns the one-minute rate of timings.
	 *
	 * @return the one-minute rate of timings
	 * @see MeterMetric#oneMinuteRate()
	 */
	public double oneMinuteRate() { return meter.oneMinuteRate(); }

	/**
	 * Returns the longest recorded duration.
	 *
	 * @return the longest recorded duration
	 */
	public double max() { return convertFromNS(_max.get()); }

	/**
	 * Returns the shortest recorded duration.
	 *
	 * @return the shortest recorded duration
	 */
	public double min() { return convertFromNS(_min.get()); }

	/**
	 * Returns the arithmetic mean of all recorded durations.
	 *
	 * @return the arithmetic mean of all recorded durations
	 */
	public double mean() { return convertFromNS(_sum.get() / (double) count()); }

	/**
	 * Returns the standard deviation of all recorded durations.
	 *
	 * @return the standard deviation of all recorded durations
	 */
	public double stdDev() { return convertFromNS(sqrt(variance())); }

	/**
	 * Returns an array of durations at the given percentiles.
	 *
	 * @param percentiles one or more percentiles ({@code 0..1})
	 * @return an array of durations at the given percentiles
	 */
	public double[] percentiles(double... percentiles) {
		final double[] scores = new double[percentiles.length];
		for (int i = 0; i < scores.length; i++) {
			scores[i] = 0.0;

		}

		if (count() > 0) {
			final List<Long> values = sample.values();
			Collections.sort(values);

			for (int i = 0; i < percentiles.length; i++) {
				final double p = percentiles[i];
				final double pos = p * (values.size() + 1);
				if (pos < 1) {
					scores[i] = convertFromNS(values.get(0));
				} else if (pos >= values.size()) {
					scores[i] = convertFromNS(values.get(values.size() - 1));
				} else {
					final double lower = values.get((int) pos - 1);
					final double upper = values.get((int) pos);
					scores[i] = convertFromNS(lower + (pos - floor(pos)) * (upper - lower));
				}
			}
		}

		return scores;
	}

	/**
	 * Returns the type of events the timer is measuring ({@code "calls"}).
	 *
	 * @return the timer's event type
	 */
	public String getEventType() {
		return meter.getEventType();
	}

	private void updateVariance(long ns) {
		// initialize varianceM to the first reading if it's still blank
		if (!varianceM.compareAndSet(-1, doubleToLongBits(ns))) {
			boolean done = false;
			while (!done) {
				final long oldMCas = varianceM.get();
				final double oldM = longBitsToDouble(oldMCas);
				final double newM = oldM + ((ns - oldM) / count());

				final long oldSCas = varianceS.get();
				final double oldS = longBitsToDouble(oldSCas);
				final double newS = oldS + ((ns - oldM) * (ns - newM));

				done = varianceM.compareAndSet(oldMCas, doubleToLongBits(newM)) &&
						varianceS.compareAndSet(oldSCas, doubleToLongBits(newS));
			}
		}
	}

	private void update(long duration) {
		if (duration >= 0) {
			meter.mark();
			sample.update(duration);
			setMax(duration);
			setMin(duration);
			_sum.getAndAdd(duration);
			updateVariance(duration);
		}
	}

	private double variance() {
		if (count() <= 1) {
			return 0.0;
		}
		return longBitsToDouble(varianceS.get()) / (count() - 1);
	}

	private double convertFromNS(double ns) {
		if (count() <= 0) {
			return 0.0;
		}
		return ns / TimeUnit.NANOSECONDS.convert(1, latencyUnit);
	}

	private void setMax(long ns) {
		boolean done = false;
		while (!done) {
			long value = _max.get();
			done = value >= ns || _max.compareAndSet(value, ns);
		}
	}

	private void setMin(long ns) {
		boolean done = false;
		while (!done) {
			long value = _min.get();
			done = value <= ns || _min.compareAndSet(value, ns);
		}
	}
}
