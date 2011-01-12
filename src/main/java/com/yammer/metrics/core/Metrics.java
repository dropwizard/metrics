package com.yammer.metrics.core;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import javax.servlet.Servlet;

/**
 * A set of factory methods for creating centrally registered metric instances.
 *
 * @author coda
 */
public class Metrics {
	/*package*/ static final ConcurrentMap<MetricName, Metric> METRICS = new ConcurrentHashMap<MetricName, Metric>();
	/*package*/ static final ConcurrentMap<String, HealthCheck> HEALTH_CHECKS = new ConcurrentHashMap<String, HealthCheck>();
	private static final JmxReporter JMX_REPORTER = new JmxReporter(METRICS);
	{{
		JMX_REPORTER.start();
	}}

	private Metrics() { /* unused */ }

	/**
	 * Given a new {@link GaugeMetric}, registers it under the given class and
	 * name.
	 *
	 * @param klass the class which owns the metric
	 * @param name the name of the metric
	 * @param metric the metric
	 * @param <T> the type of the value returned by the metric
	 * @return {@code metric}
	 */
	public static <T> GaugeMetric<T> newGauge(Class<?> klass, String name, GaugeMetric<T> metric) {
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
	 * @param eventType the plural name of the type of events the meter is
	 * 	                measuring (e.g., {@code "requests"})
	 * @param unit the scale unit of the new meter
	 * @return a new {@link MeterMetric}
	 */
	public static MeterMetric newMeter(Class<?> klass, String name, String eventType, TimeUnit unit) {
		MetricName metricName = new MetricName(klass, name);
		final Metric existingMetric = METRICS.get(metricName);
		if (existingMetric == null) {
			final MeterMetric metric = MeterMetric.newMeter(eventType, unit);
			final Metric justAddedMetric = METRICS.putIfAbsent(metricName, metric);
			if (justAddedMetric == null) {
				return metric;
			}
			return (MeterMetric) justAddedMetric;
		}
		return (MeterMetric) existingMetric;
	}

	/**
	 * Creates a new {@link TimerMetric} and registers it under the given
	 * class and name.
	 *
	 * @param klass the class which owns the metric
	 * @param name the name of the metric
	 * @param durationUnit the duration scale unit of the new timer
	 * @param rateUnit the rate scale unit of the new timer
	 * @return a new {@link TimerMetric}
	 */
	public static TimerMetric newTimer(Class<?> klass, String name, TimeUnit durationUnit, TimeUnit rateUnit) {
		MetricName metricName = new MetricName(klass, name);
		final Metric existingMetric = METRICS.get(metricName);
		if (existingMetric == null) {
			final TimerMetric metric = new TimerMetric(durationUnit, rateUnit);
			final Metric justAddedMetric = METRICS.putIfAbsent(metricName, metric);
			if (justAddedMetric == null) {
				return metric;
			}
			return (TimerMetric) justAddedMetric;
		}
		return (TimerMetric) existingMetric;
	}

	/**
	 * Enables the console reporter and causes it to print to STDOUT with the
	 * specified period.
	 *
	 * @param period the period between successive outputs
	 * @param unit the time unit of {@code period}
	 */
	public static void enableConsoleReporting(long period, TimeUnit unit) {
		final ConsoleReporter reporter = new ConsoleReporter(METRICS, System.out);
		reporter.start(period, unit);
	}

	public static void registerHealthCheck(String name, HealthCheck healthCheck) {
		HEALTH_CHECKS.putIfAbsent(name, healthCheck);
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
