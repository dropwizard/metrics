package com.yammer.metrics;

import com.yammer.metrics.core.*;
import com.yammer.metrics.reporting.ConsoleReporter;
import com.yammer.metrics.reporting.JmxReporter;

import javax.management.MalformedObjectNameException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * A set of factory methods for creating centrally registered metric instances.
 */
public class Metrics {
    private static final MetricsRegistry DEFAULT_REGISTRY = new MetricsRegistry();
    static {{
        JmxReporter.startDefault(DEFAULT_REGISTRY);
        // make sure we initialize this so it can monitor GC etc
        VirtualMachineMetrics.daemonThreadCount();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                JmxReporter.shutdownDefault();
            }
        });
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
        return DEFAULT_REGISTRY.newGauge(klass, name, metric);
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
        return DEFAULT_REGISTRY.newGauge(klass, name, scope, metric);
    }

    /**
     * Given a new {@link com.yammer.metrics.core.GaugeMetric}, registers it
     * under the given metric name.
     *
     * @param metricName the name of the metric
     * @param metric     the metric
     * @param <T>        the type of the value returned by the metric
     * @return {@code metric}
     */
    public static <T> GaugeMetric<T> newGauge(MetricName metricName,
                                              GaugeMetric<T> metric) {
        return DEFAULT_REGISTRY.newGauge(metricName, metric);
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
        return DEFAULT_REGISTRY.newJmxGauge(klass, name, null, objectName, attribute);
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
        return DEFAULT_REGISTRY.newJmxGauge(klass, name, scope, objectName, attribute);
    }

    /**
     * Given a JMX MBean's object name and an attribute name, registers a gauge
     * for that attribute under the given metric name.
     *
     * @param metricName the name of the metric
     * @param objectName the object name of the MBean
     * @param attribute  the name of the bean's attribute
     * @return a new {@link JmxGauge}
     * @throws MalformedObjectNameException if the object name is malformed
     */
    public static JmxGauge newJmxGauge(MetricName metricName,
                                       String objectName,
                                       String attribute) throws MalformedObjectNameException {
        return DEFAULT_REGISTRY.newJmxGauge(metricName, objectName, attribute);
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
        return DEFAULT_REGISTRY.newCounter(klass, name);
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
        return DEFAULT_REGISTRY.newCounter(klass, name, scope);
    }

    /**
     * Creates a new {@link com.yammer.metrics.core.CounterMetric} and registers
     * it under the given metric name.
     *
     * @param metricName the name of the metric
     * @return a new {@link com.yammer.metrics.core.CounterMetric}
     */
    public static CounterMetric newCounter(MetricName metricName) {
        return DEFAULT_REGISTRY.newCounter(metricName);
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
        return DEFAULT_REGISTRY.newHistogram(klass, name, biased);
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
        return DEFAULT_REGISTRY.newHistogram(klass, name, scope, biased);
    }

    /**
     * Creates a new {@link HistogramMetric} and registers it under the given
     * metric name.
     *
     * @param metricName the name of the metric
     * @param biased whether or not the histogram should be biased
     * @return a new {@link HistogramMetric}
     */
    public static HistogramMetric newHistogram(MetricName metricName,
                                               boolean biased) {
        return DEFAULT_REGISTRY.newHistogram(metricName, biased);
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
        return DEFAULT_REGISTRY.newHistogram(klass, name);
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
        return DEFAULT_REGISTRY.newHistogram(klass, name, scope);
    }

    /**
     * Creates a new non-biased {@link HistogramMetric} and registers it under
     * the given metric name.
     *
     * @param metricName the name of the metric
     * @return a new {@link HistogramMetric}
     */
    public static HistogramMetric newHistogram(MetricName metricName) {
        return newHistogram(metricName, false);
    }

    /**
     * Creates a new {@link MeterMetric} and registers it under the given
     * class and name.
     *
     * @param klass the class which owns the metric
     * @param name the name of the metric
     * @param eventType the plural name of the type of events the meter is
     *                     measuring (e.g., {@code "requests"})
     * @param unit the rate unit of the new meter
     * @return a new {@link MeterMetric}
     */
    public static MeterMetric newMeter(Class<?> klass,
                                       String name,
                                       String eventType,
                                       TimeUnit unit) {
        return DEFAULT_REGISTRY.newMeter(klass, name, eventType, unit);
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
        return DEFAULT_REGISTRY.newMeter(klass, name, scope, eventType, unit);
    }

    /**
     * Creates a new {@link MeterMetric} and registers it under the given
     * metric name.
     *
     * @param metricName the name of the metric
     * @param eventType  the plural name of the type of events the meter is
     *                    measuring (e.g., {@code "requests"})
     * @param unit       the rate unit of the new meter
     * @return a new {@link MeterMetric}
     */
    public static MeterMetric newMeter(MetricName metricName,
                                       String eventType,
                                       TimeUnit unit) {
        return DEFAULT_REGISTRY.newMeter(metricName, eventType, unit);
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
        return DEFAULT_REGISTRY.newTimer(klass, name, durationUnit, rateUnit);
    }

    /**
     * Creates a new {@link TimerMetric} and registers it under the given class and name, measuring
     * elapsed time in milliseconds and invocations per second.
     *
     * @param klass        the class which owns the metric
     * @param name         the name of the metric
     * @return a new {@link TimerMetric}
     */
    public static TimerMetric newTimer(Class<?> klass,
                                       String name) {
        return DEFAULT_REGISTRY.newTimer(klass, name);
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
        return DEFAULT_REGISTRY.newTimer(klass, name, scope, durationUnit, rateUnit);
    }

    /**
     * Creates a new {@link TimerMetric} and registers it under the given class, name, and scope,
     * measuring elapsed time in milliseconds and invocations per second.
     *
     * @param klass        the class which owns the metric
     * @param name         the name of the metric
     * @param scope        the scope of the metric
     * @return a new {@link TimerMetric}
     */
    public static TimerMetric newTimer(Class<?> klass,
                                       String name,
                                       String scope) {
        return DEFAULT_REGISTRY.newTimer(klass, name, scope);
    }

    /**
     * Creates a new {@link TimerMetric} and registers it under the given
     * metric name.
     *
     * @param metricName   the name of the metric
     * @param durationUnit the duration scale unit of the new timer
     * @param rateUnit     the rate scale unit of the new timer
     * @return a new {@link TimerMetric}
     */
    public static TimerMetric newTimer(MetricName metricName,
                                       TimeUnit durationUnit,
                                       TimeUnit rateUnit) {
        return DEFAULT_REGISTRY.newTimer(metricName, durationUnit, rateUnit);
    }

    /**
     * Removes the metric with the given name.
     *
     * @param name the name of the metric
     */
    public static void removeMetric(MetricName name) {
        DEFAULT_REGISTRY.removeMetric(name);
    }

    /**
     * Removes the metric for the given class with the given name.
     *
     * @param klass the klass the metric is associated with
     * @param name the name of the metric
     */
    public static void removeMetric(Class<?> klass, String name) {
        DEFAULT_REGISTRY.removeMetric(klass, name);
    }

    /**
     * Removes the metric for the given class with the given name and scope.
     *
     * @param klass the klass the metric is associated with
     * @param name the name of the metric
     * @param scope the scope of the metric
     */
    public static void removeMetric(Class<?> klass, String name, String scope) {
        DEFAULT_REGISTRY.removeMetric(klass, name, scope);
    }

    /**
     * Enables the console reporter and causes it to print to STDOUT with the
     * specified period.
     *
     * @param period the period between successive outputs
     * @param unit the time unit of {@code period}
     * @deprecated use {@link ConsoleReporter#enable(long, java.util.concurrent.TimeUnit)} instead
     */
    @Deprecated
    public static void enableConsoleReporting(long period, TimeUnit unit) {
        ConsoleReporter.enable(DEFAULT_REGISTRY, period, unit);
    }

    /**
     * Returns an unmodifiable map of all metrics and their names.
     *
     * @return an unmodifiable map of all metrics and their names
     */
    public static Map<MetricName, Metric> allMetrics() {
        return DEFAULT_REGISTRY.allMetrics();
    }

    /**
     * Returns the (static) default registry.
     * @return the metrics registry
     */
    public static MetricsRegistry defaultRegistry() {
        return DEFAULT_REGISTRY;
    }
}
