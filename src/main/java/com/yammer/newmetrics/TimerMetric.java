package com.yammer.newmetrics;

import java.util.Collections;
import java.util.List;
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
	private final MeterMetric meter = MeterMetric.newMeter();
	// Using a sample size of 1028, which offers a 99.9% confidence level with a
	// 5% margin of error assuming a normal distribution. This might need to be
	// parameterized, but I'm only going to do that when someone complains.
	private final Sample sample = new Sample(1028);
	private final AtomicLong _min = new AtomicLong();
	private final AtomicLong _max = new AtomicLong();
	private final AtomicLong _sum = new AtomicLong();
	// These are for the Welford algorithm for calculating running variance
	// without floating-point doom.
	private final AtomicLong varianceM = new AtomicLong();
	private final AtomicLong varianceS = new AtomicLong();

	/**
	 * Creates a new {@link TimerMetric}.
	 */
	public TimerMetric() {
		clear();
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
	 * @param unit the unit of {@code duration}
	 */
	public void update(long duration, TimeUnit unit) {
		final long ns = unit.toNanos(duration);
		if (ns >= 0) {
			meter.mark();
			sample.update(ns);
			setMax(ns);
			setMin(ns);
			_sum.getAndAdd(ns);
			updateVariance(ns);
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
	 * @param unit the scale unit of the returned rate
	 * @return the fifteen-minute rate of timings
	 * @see MeterMetric#fifteenMinuteRate(java.util.concurrent.TimeUnit)
	 */
	public double fifteenMinuteRate(TimeUnit unit) { return meter.fifteenMinuteRate(unit); }

	/**
	 * Returns the five-minute rate of timings.
	 *
	 * @param unit the scale unit of the returned rate
	 * @return the five-minute rate of timings
	 * @see MeterMetric#fiveMinuteRate(java.util.concurrent.TimeUnit)
	 */
	public double fiveMinuteRate(TimeUnit unit) { return meter.fiveMinuteRate(unit); }

	/**
	 * Returns the mean rate of timings.
	 *
	 * @param unit the scale unit of the returned rate
	 * @return the mean rate of timings
	 * @see MeterMetric#meanRate(java.util.concurrent.TimeUnit)
	 */
	public double meanRate(TimeUnit unit) { return meter.meanRate(unit); }

	/**
	 * Returns the one-minute rate of timings.
	 *
	 * @param unit the scale unit of the returned rate
	 * @return the one-minute rate of timings
	 * @see MeterMetric#oneMinuteRate(java.util.concurrent.TimeUnit)
	 */
	public double oneMinuteRate(TimeUnit unit) { return meter.oneMinuteRate(unit); }

	/**
	 * Returns the longest recorded duration.
	 *
	 * @param unit the scale unit of the duration
	 * @return the longest recorded duration
	 */
	public double max(TimeUnit unit) { return convertFromNS(_max.get(), unit); }

	/**
	 * Returns the shortest recorded duration.
	 *
	 * @param unit the scale unit of the duration
	 * @return the shortest recorded duration
	 */
	public double min(TimeUnit unit) { return convertFromNS(_min.get(), unit); }

	/**
	 * Returns the arithmetic mean of all recorded durations.
	 *
	 * @param unit the scale unit of the duration
	 * @return the arithmetic mean of all recorded durations
	 */
	public double mean(TimeUnit unit) { return convertFromNS(_sum.get() / (double) count(), unit); }

	/**
	 * Returns the standard deviation of all recorded durations.
	 *
	 * @param unit the scale unit of the duration
	 * @return the standard deviation of all recorded durations
	 */
	public double stdDev(TimeUnit unit) { return convertFromNS(sqrt(variance()), unit); }

	/**
	 * Returns an array of durations at the given percentiles.
	 *
	 * @param unit the scale unit of the durations
	 * @param percentiles one or more percentiles ({@code 0..1})
	 * @return an array of durations at the given percentiles
	 */
	public double[] percentiles(TimeUnit unit, double... percentiles) {
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
					scores[i] = convertFromNS(values.get(0), unit);
				} else if (pos >= values.size()) {
					scores[i] = convertFromNS(values.get(values.size() - 1), unit);
				} else {
					final double lower = values.get((int) pos - 1);
					final double upper = values.get((int) pos);
					scores[i] = convertFromNS(lower + (pos - floor(pos)) * (upper - lower), unit);
				}
			}
		}

		return scores;
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

	private double variance() {
		if (count() <= 1) {
			return 0.0;
		}
		return longBitsToDouble(varianceS.get()) / (count() - 1);
	}

	private double convertFromNS(double ns, TimeUnit unit) {
		if (count() <= 0) {
			return 0.0;
		}
		return ns / TimeUnit.NANOSECONDS.convert(1, unit);
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
