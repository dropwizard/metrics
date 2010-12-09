package com.yammer.newmetrics;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A meter metric which measures mean throughput and one-, five-, and
 * fifteen-minute exponentially-weighted moving average throughputs.
 *
 * @author coda
 * @see <a href="http://en.wikipedia.org/wiki/Moving_average#Exponential_moving_average">EMA</a>
 */
public class MeterMetric implements Metric {
	private static final ScheduledExecutorService TICK_THREAD =
			Executors.newScheduledThreadPool(2, new NamedThreadFactory("meter-tick"));
	private static final long INTERVAL = 5; // seconds
	private static final double INTERVAL_IN_NS = TimeUnit.SECONDS.toNanos(INTERVAL);
	private static final double ONE_MINUTE_FACTOR = 1 / Math.exp(TimeUnit.SECONDS.toMinutes(INTERVAL));
	private static final double FIVE_MINUTE_FACTOR = ONE_MINUTE_FACTOR / 5;
	private static final double FIFTEEN_MINUTE_FACTOR = ONE_MINUTE_FACTOR / 15;

	/**
	 * Creates a new {@link MeterMetric}.
	 *
	 * @return a new {@link MeterMetric}
	 */
	public static MeterMetric newMeter() {
		return newMeter(INTERVAL, TimeUnit.SECONDS);
	}

	/**
	 * Creates a new {@link MeterMetric} with a given tick interval.
	 *
	 * @param interval the duration of a meter tick
	 * @param unit the unit of {@code interval}
	 * @return a new {@link MeterMetric}
	 */
	public static MeterMetric newMeter(long interval, TimeUnit unit) {
		final MeterMetric meter = new MeterMetric();
		final Runnable ticker = new Runnable() {
			@Override
			public void run() {
				meter.tick();
			}
		};
		TICK_THREAD.scheduleAtFixedRate(ticker, interval, interval, unit);
		return meter;
	}

	private final AtomicLong uncounted = new AtomicLong();
	private final AtomicLong count = new AtomicLong();
	private final long startTime = System.nanoTime();
	private volatile boolean initialized;
	private volatile double _oneMinuteRate;
	private volatile double _fiveMinuteRate;
	private volatile double _fifteenMinuteRate;

	private MeterMetric() {
		initialized = false;
		_oneMinuteRate = _fiveMinuteRate = _fifteenMinuteRate = 0.0;
	}

	/**
	 * Updates the moving averages.
	 */
	void tick() {
		final long count = uncounted.getAndSet(0);
		if (initialized) {
			_oneMinuteRate += (ONE_MINUTE_FACTOR * ((count / INTERVAL_IN_NS) - _oneMinuteRate));
			_fiveMinuteRate += (FIVE_MINUTE_FACTOR * ((count / INTERVAL_IN_NS) - _fiveMinuteRate));
			_fifteenMinuteRate += (FIFTEEN_MINUTE_FACTOR * ((count / INTERVAL_IN_NS) - _fifteenMinuteRate));
		} else {
			_oneMinuteRate = _fiveMinuteRate = _fifteenMinuteRate = count / INTERVAL_IN_NS;
			initialized = true;
		}
	}

	/**
	 * Mark the occurence of an event.
	 */
	public void mark() {
		mark(1);
	}

	/**
	 * Mark the occurence of a given number of events.
	 *
	 * @param n the number of events
	 */
	public void mark(long n) {
		count.addAndGet(n);
		uncounted.addAndGet(n);
	}

	/**
	 * Returns the number of events which have been marked.
	 *
	 * @return the number of events which have been marked
	 */
	public long count() {
		return count.get();
	}

	/**
	 * Returns the fifteen-minute exponentially-weighted moving average rate at
	 * which events have occured since the meter was created.
	 * <p>
	 * This rate has the same exponential decay factor as the fifteen-minute load
	 * average in the {@code top} Unix command.
	 *
	 * @param unit the scale unit of the rate
	 * @return the fifteen-minute exponentially-weighted moving average rate at
	 *         which events have occured since the meter was created
	 */
	public double fifteenMinuteRate(TimeUnit unit) {
		return convertNsRate(_fifteenMinuteRate, unit);
	}

	/**
	 * Returns the five-minute exponentially-weighted moving average rate at
	 * which events have occured since the meter was created.
	 * <p>
	 * This rate has the same exponential decay factor as the five-minute load
	 * average in the {@code top} Unix command.
	 *
	 * @param unit the scale unit of the rate
	 * @return the five-minute exponentially-weighted moving average rate at
	 *         which events have occured since the meter was created
	 */
	public double fiveMinuteRate(TimeUnit unit) {
		return convertNsRate(_fiveMinuteRate, unit);
	}

	/**
	 * Returns the mean rate at which events have occured since the meter was
	 * created.
	 *
	 * @param unit the scale unit of the rate
	 * @return the mean rate at which events have occured since the meter was
	 *         created
	 */
	public double meanRate(TimeUnit unit) {
		if (count() == 0) {
			return 0.0;
		} else {
			final long elapsed = (System.nanoTime() - startTime);
			return convertNsRate(count() / (double) elapsed, unit);
		}
	}

	/**
	 * Returns the one-minute exponentially-weighted moving average rate at
	 * which events have occured since the meter was created.
	 * <p>
	 * This rate has the same exponential decay factor as the one-minute load
	 * average in the {@code top} Unix command.
	 *
	 * @param unit the scale unit of the rate
	 * @return the one-minute exponentially-weighted moving average rate at
	 *         which events have occured since the meter was created
	 */
	public double oneMinuteRate(TimeUnit unit) {
		return convertNsRate(_oneMinuteRate, unit);
	}

	private double convertNsRate(double ratePerNs, TimeUnit unit) {
		return ratePerNs * (double) unit.toNanos(1);
	}
}
