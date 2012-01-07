package com.yammer.metrics;

import com.yammer.metrics.core.*;
import com.yammer.metrics.reporting.JmxReporter;

import java.util.concurrent.TimeUnit;

/**
 * A set of factory methods for creating centrally registered metric instances.
 */
public class Metrics {
    private static final MetricsRegistry DEFAULT_REGISTRY = new MetricsRegistry();
    private static final Thread SHUTDOWN_HOOK = new Thread() {
        public void run() {
            JmxReporter.shutdownDefault();
        }
    };

    static {
        JmxReporter.startDefault(DEFAULT_REGISTRY);
        Runtime.getRuntime().addShutdownHook(SHUTDOWN_HOOK);
    }

    private Metrics() { /* unused */ }

    /**
     * Given a new {@link com.yammer.metrics.core.Gauge}, registers it under the given class and
     * name.
     *
     * @param klass  the class which owns the metric
     * @param name   the name of the metric
     * @param metric the metric
     * @param <T>    the type of the value returned by the metric
     * @return {@code metric}
     */
    public static <T> Gauge<T> newGauge(Class<?> klass,
                                        String name,
                                        Gauge<T> metric) {
        return DEFAULT_REGISTRY.newGauge(klass, name, metric);
    }

    /**
     * Given a new {@link com.yammer.metrics.core.Gauge}, registers it under the given class and
     * name.
     *
     * @param klass  the class which owns the metric
     * @param name   the name of the metric
     * @param scope  the scope of the metric
     * @param metric the metric
     * @param <T>    the type of the value returned by the metric
     * @return {@code metric}
     */
    public static <T> Gauge<T> newGauge(Class<?> klass,
                                        String name,
                                        String scope,
                                        Gauge<T> metric) {
        return DEFAULT_REGISTRY.newGauge(klass, name, scope, metric);
    }

    /**
     * Given a new {@link com.yammer.metrics.core.Gauge}, registers it under the given metric name.
     *
     * @param metricName the name of the metric
     * @param metric     the metric
     * @param <T>        the type of the value returned by the metric
     * @return {@code metric}
     */
    public static <T> Gauge<T> newGauge(MetricName metricName,
                                        Gauge<T> metric) {
        return DEFAULT_REGISTRY.newGauge(metricName, metric);
    }

    /**
     * Creates a new {@link com.yammer.metrics.core.Counter} and registers it under the given class
     * and name.
     *
     * @param klass the class which owns the metric
     * @param name  the name of the metric
     * @return a new {@link com.yammer.metrics.core.Counter}
     */
    public static Counter newCounter(Class<?> klass, String name) {
        return DEFAULT_REGISTRY.newCounter(klass, name);
    }

    /**
     * Creates a new {@link com.yammer.metrics.core.Counter} and registers it under the given class
     * and name.
     *
     * @param klass the class which owns the metric
     * @param name  the name of the metric
     * @param scope the scope of the metric
     * @return a new {@link com.yammer.metrics.core.Counter}
     */
    public static Counter newCounter(Class<?> klass,
                                     String name,
                                     String scope) {
        return DEFAULT_REGISTRY.newCounter(klass, name, scope);
    }

    /**
     * Creates a new {@link com.yammer.metrics.core.Counter} and registers it under the given metric
     * name.
     *
     * @param metricName the name of the metric
     * @return a new {@link com.yammer.metrics.core.Counter}
     */
    public static Counter newCounter(MetricName metricName) {
        return DEFAULT_REGISTRY.newCounter(metricName);
    }

    /**
     * Creates a new {@link com.yammer.metrics.core.Histogram} and registers it under the given
     * class and name.
     *
     * @param klass  the class which owns the metric
     * @param name   the name of the metric
     * @param biased whether or not the histogram should be biased
     * @return a new {@link com.yammer.metrics.core.Histogram}
     */
    public static Histogram newHistogram(Class<?> klass,
                                         String name,
                                         boolean biased) {
        return DEFAULT_REGISTRY.newHistogram(klass, name, biased);
    }

    /**
     * Creates a new {@link com.yammer.metrics.core.Histogram} and registers it under the given
     * class, name, and scope.
     *
     * @param klass  the class which owns the metric
     * @param name   the name of the metric
     * @param scope  the scope of the metric
     * @param biased whether or not the histogram should be biased
     * @return a new {@link com.yammer.metrics.core.Histogram}
     */
    public static Histogram newHistogram(Class<?> klass,
                                         String name,
                                         String scope,
                                         boolean biased) {
        return DEFAULT_REGISTRY.newHistogram(klass, name, scope, biased);
    }

    /**
     * Creates a new {@link com.yammer.metrics.core.Histogram} and registers it under the given
     * metric name.
     *
     * @param metricName the name of the metric
     * @param biased     whether or not the histogram should be biased
     * @return a new {@link com.yammer.metrics.core.Histogram}
     */
    public static Histogram newHistogram(MetricName metricName,
                                         boolean biased) {
        return DEFAULT_REGISTRY.newHistogram(metricName, biased);
    }

    /**
     * Creates a new non-biased {@link com.yammer.metrics.core.Histogram} and registers it under the
     * given class and name.
     *
     * @param klass the class which owns the metric
     * @param name  the name of the metric
     * @return a new {@link com.yammer.metrics.core.Histogram}
     */
    public static Histogram newHistogram(Class<?> klass, String name) {
        return DEFAULT_REGISTRY.newHistogram(klass, name);
    }

    /**
     * Creates a new non-biased {@link com.yammer.metrics.core.Histogram} and registers it under the
     * given class, name, and scope.
     *
     * @param klass the class which owns the metric
     * @param name  the name of the metric
     * @param scope the scope of the metric
     * @return a new {@link com.yammer.metrics.core.Histogram}
     */
    public static Histogram newHistogram(Class<?> klass,
                                         String name,
                                         String scope) {
        return DEFAULT_REGISTRY.newHistogram(klass, name, scope);
    }

    /**
     * Creates a new non-biased {@link com.yammer.metrics.core.Histogram} and registers it under the
     * given metric name.
     *
     * @param metricName the name of the metric
     * @return a new {@link com.yammer.metrics.core.Histogram}
     */
    public static Histogram newHistogram(MetricName metricName) {
        return newHistogram(metricName, false);
    }

    /**
     * Creates a new {@link com.yammer.metrics.core.Meter} and registers it under the given class
     * and name.
     *
     * @param klass     the class which owns the metric
     * @param name      the name of the metric
     * @param eventType the plural name of the type of events the meter is measuring (e.g., {@code
     *                  "requests"})
     * @param unit      the rate unit of the new meter
     * @return a new {@link com.yammer.metrics.core.Meter}
     */
    public static Meter newMeter(Class<?> klass,
                                 String name,
                                 String eventType,
                                 TimeUnit unit) {
        return DEFAULT_REGISTRY.newMeter(klass, name, eventType, unit);
    }

    /**
     * Creates a new {@link com.yammer.metrics.core.Meter} and registers it under the given class,
     * name, and scope.
     *
     * @param klass     the class which owns the metric
     * @param name      the name of the metric
     * @param scope     the scope of the metric
     * @param eventType the plural name of the type of events the meter is measuring (e.g., {@code
     *                  "requests"})
     * @param unit      the rate unit of the new meter
     * @return a new {@link com.yammer.metrics.core.Meter}
     */
    public static Meter newMeter(Class<?> klass,
                                 String name,
                                 String scope,
                                 String eventType,
                                 TimeUnit unit) {
        return DEFAULT_REGISTRY.newMeter(klass, name, scope, eventType, unit);
    }

    /**
     * Creates a new {@link com.yammer.metrics.core.Meter} and registers it under the given metric
     * name.
     *
     * @param metricName the name of the metric
     * @param eventType  the plural name of the type of events the meter is measuring (e.g., {@code
     *                   "requests"})
     * @param unit       the rate unit of the new meter
     * @return a new {@link com.yammer.metrics.core.Meter}
     */
    public static Meter newMeter(MetricName metricName,
                                 String eventType,
                                 TimeUnit unit) {
        return DEFAULT_REGISTRY.newMeter(metricName, eventType, unit);
    }

    /**
     * Creates a new {@link com.yammer.metrics.core.Timer} and registers it under the given class
     * and name.
     *
     * @param klass        the class which owns the metric
     * @param name         the name of the metric
     * @param durationUnit the duration scale unit of the new timer
     * @param rateUnit     the rate scale unit of the new timer
     * @return a new {@link com.yammer.metrics.core.Timer}
     */
    public static Timer newTimer(Class<?> klass,
                                 String name,
                                 TimeUnit durationUnit,
                                 TimeUnit rateUnit) {
        return DEFAULT_REGISTRY.newTimer(klass, name, durationUnit, rateUnit);
    }

    /**
     * Creates a new {@link com.yammer.metrics.core.Timer} and registers it under the given class
     * and name, measuring elapsed time in milliseconds and invocations per second.
     *
     * @param klass the class which owns the metric
     * @param name  the name of the metric
     * @return a new {@link com.yammer.metrics.core.Timer}
     */
    public static Timer newTimer(Class<?> klass,
                                 String name) {
        return DEFAULT_REGISTRY.newTimer(klass, name);
    }

    /**
     * Creates a new {@link com.yammer.metrics.core.Timer} and registers it under the given class,
     * name, and scope.
     *
     * @param klass        the class which owns the metric
     * @param name         the name of the metric
     * @param scope        the scope of the metric
     * @param durationUnit the duration scale unit of the new timer
     * @param rateUnit     the rate scale unit of the new timer
     * @return a new {@link com.yammer.metrics.core.Timer}
     */
    public static Timer newTimer(Class<?> klass,
                                 String name,
                                 String scope,
                                 TimeUnit durationUnit,
                                 TimeUnit rateUnit) {
        return DEFAULT_REGISTRY.newTimer(klass, name, scope, durationUnit, rateUnit);
    }

    /**
     * Creates a new {@link com.yammer.metrics.core.Timer} and registers it under the given class,
     * name, and scope, measuring elapsed time in milliseconds and invocations per second.
     *
     * @param klass the class which owns the metric
     * @param name  the name of the metric
     * @param scope the scope of the metric
     * @return a new {@link com.yammer.metrics.core.Timer}
     */
    public static Timer newTimer(Class<?> klass,
                                 String name,
                                 String scope) {
        return DEFAULT_REGISTRY.newTimer(klass, name, scope);
    }

    /**
     * Creates a new {@link com.yammer.metrics.core.Timer} and registers it under the given metric
     * name.
     *
     * @param metricName   the name of the metric
     * @param durationUnit the duration scale unit of the new timer
     * @param rateUnit     the rate scale unit of the new timer
     * @return a new {@link com.yammer.metrics.core.Timer}
     */
    public static Timer newTimer(MetricName metricName,
                                 TimeUnit durationUnit,
                                 TimeUnit rateUnit) {
        return DEFAULT_REGISTRY.newTimer(metricName, durationUnit, rateUnit);
    }

    /**
     * Returns the (static) default registry.
     *
     * @return the metrics registry
     */
    public static MetricsRegistry defaultRegistry() {
        return DEFAULT_REGISTRY;
    }

    /**
     * Shuts down all thread pools for the default registry.
     */
    public static void shutdown() {
        DEFAULT_REGISTRY.shutdown();
        JmxReporter.shutdownDefault();
        Runtime.getRuntime().removeShutdownHook(SHUTDOWN_HOOK);
    }
}
