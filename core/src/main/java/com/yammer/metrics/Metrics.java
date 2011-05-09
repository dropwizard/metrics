package com.yammer.metrics;

import com.yammer.metrics.core.*;
import com.yammer.metrics.core.HistogramMetric.SampleType;
import com.yammer.metrics.reporting.ConsoleReporter;
import com.yammer.metrics.reporting.GraphiteReporter;
import com.yammer.metrics.reporting.JmxReporter;

import javax.management.MalformedObjectNameException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * A set of factory methods for creating centrally registered metric instances.
 *
 * @author coda
 */
public class Metrics {
	private static final ConcurrentMap<MetricName, Metric> METRICS =
            new ConcurrentHashMap<MetricName, Metric>();
	static {{
		JmxReporter.INSTANCE.start();
        // make sure we initialize this so it can monitor GC etc
		VirtualMachineMetrics.daemonThreadCount();
	}}

	private Metrics() { /* unused */ }

    /**
     * Given a new {@link com.yammer.metrics.core.GaugeMetric}, registers it
     * under the given class and name.
     *
     * @param klass  the class which owns the metric
     * @param name   the name of the metric
     * @param metric the metric
     * @param <T>    the type of the value returned by the metric
     * @return {@code metric}
     */
    public static <T> GaugeMetric<T> newGauge(Class<?> klass,
                                              String name,
                                              GaugeMetric<T> metric) {
        return newGauge(klass, name, null, metric);
    }

    /**
     * Given a new {@link com.yammer.metrics.core.GaugeMetric}, registers it
     * under the given class and name.
     *
     * @param klass  the class which owns the metric
     * @param name   the name of the metric
     * @param scope  the scope of the metric
     * @param metric the metric
     * @param <T>    the type of the value returned by the metric
     * @return {@code metric}
     */
    public static <T> GaugeMetric<T> newGauge(Class<?> klass,
                                              String name,
                                              String scope,
                                              GaugeMetric<T> metric) {
        return getOrAdd(new MetricName(klass, name, scope), metric);
    }

    /**
     * Given a JMX MBean's object name and an attribute name, registers a gauge
     * for that attribute under the given class and name.
     *
     * @param klass      the class which owns the metric
     * @param name       the name of the metric
     * @param objectName the object name of the MBean
     * @param attribute  the name of the bean's attribute
     * @return a new {@link JmxGauge}
     * @throws MalformedObjectNameException if the object name is malformed
     */
    public static JmxGauge newJmxGauge(Class<?> klass,
                                       String name,
                                       String objectName,
                                       String attribute) throws MalformedObjectNameException {
        return newJmxGauge(klass, name, null, objectName, attribute);
    }

    /**
     * Given a JMX MBean's object name and an attribute name, registers a gauge
     * for that attribute under the given class, name, and scope.
     *
     * @param klass      the class which owns the metric
     * @param name       the name of the metric
     * @param scope      the scope of the metric
     * @param objectName the object name of the MBean
     * @param attribute  the name of the bean's attribute
     * @return a new {@link JmxGauge}
     * @throws MalformedObjectNameException if the object name is malformed
     */
    public static JmxGauge newJmxGauge(Class<?> klass,
                                       String name,
                                       String scope,
                                       String objectName,
                                       String attribute) throws MalformedObjectNameException {
        return getOrAdd(new MetricName(klass, name, scope),
                new JmxGauge(objectName, attribute));
    }

    /**
     * Creates a new {@link com.yammer.metrics.core.CounterMetric} and registers
     * it under the given class and name.
     *
     * @param klass the class which owns the metric
     * @param name  the name of the metric
     * @return a new {@link com.yammer.metrics.core.CounterMetric}
     */
    public static CounterMetric newCounter(Class<?> klass, String name) {
        return newCounter(klass, name, null);
    }

    /**
     * Creates a new {@link com.yammer.metrics.core.CounterMetric} and registers
     * it under the given class and name.
     *
     * @param klass the class which owns the metric
     * @param name  the name of the metric
     * @param scope the scope of the metric
     * @return a new {@link com.yammer.metrics.core.CounterMetric}
     */
    public static CounterMetric newCounter(Class<?> klass,
                                           String name,
                                           String scope) {
        return getOrAdd(new MetricName(klass, name, scope), new CounterMetric());
    }

	/**
	 * Creates a new {@link HistogramMetric} and registers it under the given
	 * class and name.
	 *
	 * @param klass the class which owns the metric
	 * @param name the name of the metric
	 * @param biased whether or not the histogram should be biased
	 * @return a new {@link HistogramMetric}
	 */
	public static HistogramMetric newHistogram(Class<?> klass,
                                               String name,
											   boolean biased) {
        return newHistogram(klass, name, null, biased);
    }

    /**
     * Creates a new {@link HistogramMetric} and registers it under the given
     * class, name, and scope.
     *
     * @param klass  the class which owns the metric
     * @param name   the name of the metric
     * @param scope  the scope of the metric
     * @param biased whether or not the histogram should be biased
     * @return a new {@link HistogramMetric}
     */
    public static HistogramMetric newHistogram(Class<?> klass,
                                               String name,
                                               String scope,
                                               boolean biased) {
        return getOrAdd(new MetricName(klass, name, scope),
                new HistogramMetric(biased ? SampleType.BIASED : SampleType.UNIFORM));
    }

	/**
	 * Creates a new non-biased {@link HistogramMetric} and registers it under
	 * the given class and name.
	 *
	 * @param klass the class which owns the metric
	 * @param name the name of the metric
	 * @return a new {@link HistogramMetric}
	 */
	public static HistogramMetric newHistogram(Class<?> klass, String name) {
		return newHistogram(klass, name, false);
	}

    /**
     * Creates a new non-biased {@link HistogramMetric} and registers it under
     * the given class, name, and scope.
     *
     * @param klass the class which owns the metric
     * @param name  the name of the metric
     * @param scope the scope of the metric
     * @return a new {@link HistogramMetric}
     */
    public static HistogramMetric newHistogram(Class<?> klass,
                                               String name,
                                               String scope) {
        return newHistogram(klass, name, scope, false);
    }

	/**
	 * Creates a new {@link MeterMetric} and registers it under the given
	 * class and name.
	 *
	 * @param klass the class which owns the metric
	 * @param name the name of the metric
	 * @param eventType the plural name of the type of events the meter is
	 * 	                measuring (e.g., {@code "requests"})
	 * @param unit the rate unit of the new meter
	 * @return a new {@link MeterMetric}
	 */
	public static MeterMetric newMeter(Class<?> klass,
                                       String name,
                                       String eventType,
                                       TimeUnit unit) {
        return newMeter(klass, name, null, eventType, unit);
    }

    /**
     * Creates a new {@link MeterMetric} and registers it under the given
     * class, name, and scope.
     *
     * @param klass     the class which owns the metric
     * @param name      the name of the metric
     * @param scope     the scope of the metric
     * @param eventType the plural name of the type of events the meter is
     *                  measuring (e.g., {@code "requests"})
     * @param unit      the rate unit of the new meter
     * @return a new {@link MeterMetric}
     */
    public static MeterMetric newMeter(Class<?> klass,
                                       String name,
                                       String scope,
                                       String eventType,
                                       TimeUnit unit) {
        MetricName metricName = new MetricName(klass, name, scope);
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
	public static TimerMetric newTimer(Class<?> klass,
                                       String name,
                                       TimeUnit durationUnit,
                                       TimeUnit rateUnit) {
        return newTimer(klass, name, null, durationUnit, rateUnit);
    }

    /**
     * Creates a new {@link TimerMetric} and registers it under the given
     * class, name, and scope.
     *
     * @param klass        the class which owns the metric
     * @param name         the name of the metric
     * @param scope        the scope of the metric
     * @param durationUnit the duration scale unit of the new timer
     * @param rateUnit     the rate scale unit of the new timer
     * @return a new {@link TimerMetric}
     */
    public static TimerMetric newTimer(Class<?> klass,
                                       String name,
                                       String scope,
                                       TimeUnit durationUnit,
                                       TimeUnit rateUnit) {
        MetricName metricName = new MetricName(klass, name, scope);
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
		final ConsoleReporter reporter = new ConsoleReporter(System.out);
		reporter.start(period, unit);
	}
	
	/**
     * Enables the graphite reporter sends data to graphite server with the
     * specified period.
     *
     * @param period the period between successive outputs
     * @param unit the time unit of {@code period}
     * @param host the host name of graphite server (carbon-cache agent)
     * @param port the port number on which the graphite server is listening
     */
    public static void enableGraphiteReporting(long period, TimeUnit unit, String host, int port) {
            try{   
                    final GraphiteReporter reporter = new GraphiteReporter(host, port);
                    reporter.start(period, unit);
            }catch(Exception e){
                    e.printStackTrace();
            }
    }


	/**
	 * Returns an unmodifiable map of all metrics and their names.
	 *
	 * @return an unmodifiable map of all metrics and their names
	 */
	public static Map<MetricName, Metric> allMetrics() {
		return Collections.unmodifiableMap(METRICS);
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
