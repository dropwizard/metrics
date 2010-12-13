package com.yammer.metrics.core;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * A set of factory methods for creating centrally registered metric instances.
 *
 * @author coda
 */
public class MetricsFactory {
	private static final ConcurrentMap<MetricName, Metric> METRICS = new ConcurrentHashMap<MetricName, Metric>();

	private MetricsFactory() { /* unused */ }

	/**
	 * Given a new {@link ValueMetric}, registers it under the given class and
	 * name.
	 *
	 * @param klass the class which owns the metric
	 * @param name the name of the metric
	 * @param metric the metric
	 * @param <T> the type of the value returned by the metric
	 * @return {@code metric}
	 */
	public static <T> ValueMetric<T> newValue(Class<?> klass, String name, ValueMetric<T> metric) {
		return getOrAdd(new MetricName(klass, name), metric);
	}

	/**
	 * Creates a new {@link CounterMetric} and registers it under the given
	 * class and name.
	 *
	 * @param klass the class which owns the metric
	 * @param name the name of the metric
	 * @return a new {@link CounterMetric}
	 */
	public static CounterMetric newCounter(Class<?> klass, String name) {
		return getOrAdd(new MetricName(klass, name), new CounterMetric());
	}

	/**
	 * Creates a new {@link MeterMetric} and registers it under the given
	 * class and name.
	 *
	 * @param klass the class which owns the metric
	 * @param name the name of the metric
	 * @param unit the scale unit of the new meter
	 * @return a new {@link MeterMetric}
	 */
	public static MeterMetric newMeter(Class<?> klass, String name, TimeUnit unit) {
		return getOrAdd(new MetricName(klass, name), MeterMetric.newMeter(unit));
	}

	/**
	 * Creates a new {@link TimerMetric} and registers it under the given
	 * class and name.
	 *
	 * @param klass the class which owns the metric
	 * @param name the name of the metric
	 * @param latencyUnit the latency scale unit of the new meter
	 * @param rateUnit the rate scale unit of the new meter
	 * @return a new {@link TimerMetric}
	 */
	public static TimerMetric newTimer(Class<?> klass, String name, TimeUnit latencyUnit, TimeUnit rateUnit) {
		return getOrAdd(new MetricName(klass, name), new TimerMetric(latencyUnit, rateUnit));
	}

	/**
	 * Enables the HTTP/JSON reporter on the given port.
	 *
	 * @param port the port on which the HTTP server will listen
	 * @throws IOException
	 * @see HttpReporter
	 */
	public static void enableHttpReporting(int port) throws IOException {
		final HttpReporter reporter = new HttpReporter(METRICS, port);
		reporter.start();
	}

	@SuppressWarnings("unchecked")
	private static <T extends Metric> T getOrAdd(MetricName name, T metric) {
		final Metric existingMetric = METRICS.get(name);
		if (existingMetric == null) {
			final Metric justAddedMetric = METRICS.putIfAbsent(name, metric);
			if (justAddedMetric == null) {
				return metric;
			}
			return (T) justAddedMetric;
		}
		return (T) existingMetric;
	}
}
