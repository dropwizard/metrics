package com.yammer.metrics.core;

import com.yammer.metrics.core.Histogram.SampleType;

import java.util.*;
import java.util.concurrent.*;

/**
 * A registry of metric instances.
 */
public class MetricsRegistry {
    private static final int EXPECTED_METRIC_COUNT = 1024;
    private final Clock clock;
    private final ConcurrentMap<MetricName, Metric> metrics;
    private final ThreadPools threadPools;
    private final List<MetricsRegistryListener> listeners;

    /**
     * Creates a new {@link MetricsRegistry}.
     */
    public MetricsRegistry() {
        this(Clock.defaultClock());
    }

    /**
     * Creates a new {@link MetricsRegistry} with the given {@link Clock} instance.
     *
     * @param clock    a {@link Clock} instance
     */
    public MetricsRegistry(Clock clock) {
        this.clock = clock;
        this.metrics = newMetricsMap();
        this.threadPools = new ThreadPools();
        this.listeners = new CopyOnWriteArrayList<MetricsRegistryListener>();
    }

    /**
     * Given a new {@link Gauge}, registers it under the given class and name.
     *
     * @param klass  the class which owns the metric
     * @param name   the name of the metric
     * @param metric the metric
     * @param <T>    the type of the value returned by the metric
     * @return {@code metric}
     */
    public <T> Gauge<T> newGauge(Class<?> klass,
                                 String name,
                                 Gauge<T> metric) {
        return newGauge(klass, name, null, metric);
    }

    /**
     * Given a new {@link Gauge}, registers it under the given class and name.
     *
     * @param klass  the class which owns the metric
     * @param name   the name of the metric
     * @param scope  the scope of the metric
     * @param metric the metric
     * @param <T>    the type of the value returned by the metric
     * @return {@code metric}
     */
    public <T> Gauge<T> newGauge(Class<?> klass,
                                 String name,
                                 String scope,
                                 Gauge<T> metric) {
        return newGauge(createName(klass, name, scope), metric);
    }

    /**
     * Given a new {@link Gauge}, registers it under the given metric name.
     *
     * @param metricName the name of the metric
     * @param metric     the metric
     * @param <T>        the type of the value returned by the metric
     * @return {@code metric}
     */
    public <T> Gauge<T> newGauge(MetricName metricName,
                                 Gauge<T> metric) {
        return getOrAdd(metricName, metric);
    }

    /**
     * Creates a new {@link Counter} and registers it under the given class and name.
     *
     * @param klass the class which owns the metric
     * @param name  the name of the metric
     * @return a new {@link Counter}
     */
    public Counter newCounter(Class<?> klass,
                              String name) {
        return newCounter(klass, name, null);
    }

    /**
     * Creates a new {@link Counter} and registers it under the given class and name.
     *
     * @param klass the class which owns the metric
     * @param name  the name of the metric
     * @param scope the scope of the metric
     * @return a new {@link Counter}
     */
    public Counter newCounter(Class<?> klass,
                              String name,
                              String scope) {
        return newCounter(createName(klass, name, scope));
    }

    /**
     * Creates a new {@link Counter} and registers it under the given metric name.
     *
     * @param metricName the name of the metric
     * @return a new {@link Counter}
     */
    public Counter newCounter(MetricName metricName) {
        return getOrAdd(metricName, new Counter());
    }

    /**
     * Creates a new {@link Histogram} and registers it under the given class and name.
     *
     * @param klass  the class which owns the metric
     * @param name   the name of the metric
     * @param biased whether or not the histogram should be biased
     * @return a new {@link Histogram}
     */
    public Histogram newHistogram(Class<?> klass,
                                  String name,
                                  boolean biased) {
        return newHistogram(klass, name, null, biased);
    }

    /**
     * Creates a new {@link Histogram} and registers it under the given class, name, and scope.
     *
     * @param klass  the class which owns the metric
     * @param name   the name of the metric
     * @param scope  the scope of the metric
     * @param biased whether or not the histogram should be biased
     * @return a new {@link Histogram}
     */
    public Histogram newHistogram(Class<?> klass,
                                  String name,
                                  String scope,
                                  boolean biased) {
        return newHistogram(createName(klass, name, scope), biased);
    }

    /**
     * Creates a new non-biased {@link Histogram} and registers it under the given class and name.
     *
     * @param klass the class which owns the metric
     * @param name  the name of the metric
     * @return a new {@link Histogram}
     */
    public Histogram newHistogram(Class<?> klass,
                                  String name) {
        return newHistogram(klass, name, false);
    }

    /**
     * Creates a new non-biased {@link Histogram} and registers it under the given class, name, and
     * scope.
     *
     * @param klass the class which owns the metric
     * @param name  the name of the metric
     * @param scope the scope of the metric
     * @return a new {@link Histogram}
     */
    public Histogram newHistogram(Class<?> klass,
                                  String name,
                                  String scope) {
        return newHistogram(klass, name, scope, false);
    }

    /**
     * Creates a new {@link Histogram} and registers it under the given metric name.
     *
     * @param metricName the name of the metric
     * @param biased     whether or not the histogram should be biased
     * @return a new {@link Histogram}
     */
    public Histogram newHistogram(MetricName metricName,
                                  boolean biased) {
        return getOrAdd(metricName,
                        new Histogram(biased ? SampleType.BIASED : SampleType.UNIFORM));
    }

    /**
     * Creates a new {@link Meter} and registers it under the given class and name.
     *
     * @param klass     the class which owns the metric
     * @param name      the name of the metric
     * @param eventType the plural name of the type of events the meter is measuring (e.g., {@code
     *                  "requests"})
     * @param unit      the rate unit of the new meter
     * @return a new {@link Meter}
     */
    public Meter newMeter(Class<?> klass,
                          String name,
                          String eventType,
                          TimeUnit unit) {
        return newMeter(klass, name, null, eventType, unit);
    }

    /**
     * Creates a new {@link Meter} and registers it under the given class, name, and scope.
     *
     * @param klass     the class which owns the metric
     * @param name      the name of the metric
     * @param scope     the scope of the metric
     * @param eventType the plural name of the type of events the meter is measuring (e.g., {@code
     *                  "requests"})
     * @param unit      the rate unit of the new meter
     * @return a new {@link Meter}
     */
    public Meter newMeter(Class<?> klass,
                          String name,
                          String scope,
                          String eventType,
                          TimeUnit unit) {
        return newMeter(createName(klass, name, scope), eventType, unit);
    }

    /**
     * Creates a new {@link Meter} and registers it under the given metric name.
     *
     * @param metricName the name of the metric
     * @param eventType  the plural name of the type of events the meter is measuring (e.g., {@code
     *                   "requests"})
     * @param unit       the rate unit of the new meter
     * @return a new {@link Meter}
     */
    public Meter newMeter(MetricName metricName,
                          String eventType,
                          TimeUnit unit) {
        final Metric existingMetric = metrics.get(metricName);
        if (existingMetric != null) {
            return (Meter) existingMetric;
        }
        return getOrAdd(metricName, new Meter(newMeterTickThreadPool(), eventType, unit, clock));
    }

    /**
     * Creates a new {@link Timer} and registers it under the given class and name, measuring
     * elapsed time in milliseconds and invocations per second.
     *
     * @param klass the class which owns the metric
     * @param name  the name of the metric
     * @return a new {@link Timer}
     */
    public Timer newTimer(Class<?> klass,
                          String name) {
        return newTimer(klass, name, null, TimeUnit.MILLISECONDS, TimeUnit.SECONDS);
    }

    /**
     * Creates a new {@link Timer} and registers it under the given class and name.
     *
     * @param klass        the class which owns the metric
     * @param name         the name of the metric
     * @param durationUnit the duration scale unit of the new timer
     * @param rateUnit     the rate scale unit of the new timer
     * @return a new {@link Timer}
     */
    public Timer newTimer(Class<?> klass,
                          String name,
                          TimeUnit durationUnit,
                          TimeUnit rateUnit) {
        return newTimer(klass, name, null, durationUnit, rateUnit);
    }

    /**
     * Creates a new {@link Timer} and registers it under the given class, name, and scope,
     * measuring elapsed time in milliseconds and invocations per second.
     *
     * @param klass the class which owns the metric
     * @param name  the name of the metric
     * @param scope the scope of the metric
     * @return a new {@link Timer}
     */
    public Timer newTimer(Class<?> klass,
                          String name,
                          String scope) {
        return newTimer(klass, name, scope, TimeUnit.MILLISECONDS, TimeUnit.SECONDS);
    }

    /**
     * Creates a new {@link Timer} and registers it under the given class, name, and scope.
     *
     * @param klass        the class which owns the metric
     * @param name         the name of the metric
     * @param scope        the scope of the metric
     * @param durationUnit the duration scale unit of the new timer
     * @param rateUnit     the rate scale unit of the new timer
     * @return a new {@link Timer}
     */
    public Timer newTimer(Class<?> klass,
                          String name,
                          String scope,
                          TimeUnit durationUnit,
                          TimeUnit rateUnit) {
        return newTimer(createName(klass, name, scope), durationUnit, rateUnit);
    }

    /**
     * Creates a new {@link Timer} and registers it under the given metric name.
     *
     * @param metricName   the name of the metric
     * @param durationUnit the duration scale unit of the new timer
     * @param rateUnit     the rate scale unit of the new timer
     * @return a new {@link Timer}
     */
    public Timer newTimer(MetricName metricName,
                          TimeUnit durationUnit,
                          TimeUnit rateUnit) {
        final Metric existingMetric = metrics.get(metricName);
        if (existingMetric != null) {
            return (Timer) existingMetric;
        }
        return getOrAdd(metricName,
                        new Timer(newMeterTickThreadPool(), durationUnit, rateUnit, clock));
    }

    /**
     * Returns an unmodifiable map of all metrics and their names.
     *
     * @return an unmodifiable map of all metrics and their names
     */
    public Map<MetricName, Metric> allMetrics() {
        return Collections.unmodifiableMap(metrics);
    }

    /**
     * Returns a grouped and sorted map of all registered metrics.
     *
     * @return all registered metrics, grouped by name and sorted
     */
    public SortedMap<String, SortedMap<MetricName, Metric>> groupedMetrics() {
        return groupedMetrics(MetricPredicate.ALL);
    }

    /**
     * Returns a grouped and sorted map of all registered metrics which match then given {@link
     * MetricPredicate}.
     *
     * @param predicate a predicate which metrics have to match to be in the results
     * @return all registered metrics which match {@code predicate}, sorted by name
     */
    public SortedMap<String, SortedMap<MetricName, Metric>> groupedMetrics(MetricPredicate predicate) {
        final SortedMap<String, SortedMap<MetricName, Metric>> groups =
                new TreeMap<String, SortedMap<MetricName, Metric>>();
        for (Map.Entry<MetricName, Metric> entry : metrics.entrySet()) {
            final String qualifiedTypeName = entry.getKey().getGroup() + "." + entry.getKey()
                                                                                    .getType();
            if (predicate.matches(entry.getKey(), entry.getValue())) {
                final String scopedName;
                if (entry.getKey().hasScope()) {
                    scopedName = qualifiedTypeName + "." + entry.getKey().getScope();
                } else {
                    scopedName = qualifiedTypeName;
                }
                SortedMap<MetricName, Metric> group = groups.get(scopedName);
                if (group == null) {
                    group = new TreeMap<MetricName, Metric>();
                    groups.put(scopedName, group);
                }
                group.put(entry.getKey(), entry.getValue());
            }
        }
        return Collections.unmodifiableSortedMap(groups);
    }

    /**
     * Shut down this registry's thread pools.
     */
    public void shutdown() {
        threadPools.shutdown();
    }

    /**
     * Creates a new scheduled thread pool of a given size with the given name, or returns an
     * existing thread pool if one was already created with the same name.
     *
     * @param poolSize the number of threads to create
     * @param name     the name of the pool
     * @return a new {@link ScheduledExecutorService}
     */
    public ScheduledExecutorService newScheduledThreadPool(int poolSize, String name) {
        return threadPools.newScheduledThreadPool(poolSize, name);
    }

    /**
     * Removes the metric for the given class with the given name.
     *
     * @param klass the klass the metric is associated with
     * @param name  the name of the metric
     */
    public void removeMetric(Class<?> klass,
                             String name) {
        removeMetric(klass, name, null);
    }

    /**
     * Removes the metric for the given class with the given name and scope.
     *
     * @param klass the klass the metric is associated with
     * @param name  the name of the metric
     * @param scope the scope of the metric
     */
    public void removeMetric(Class<?> klass,
                             String name,
                             String scope) {
        removeMetric(createName(klass, name, scope));
    }

    /**
     * Removes the metric with the given name.
     *
     * @param name the name of the metric
     */
    public void removeMetric(MetricName name) {
        final Metric metric = metrics.remove(name);
        if (metric != null) {
            if (metric instanceof Stoppable) {
                ((Stoppable) metric).stop();
            }
            notifyMetricRemoved(name);
        }
    }

    /**
     * Adds a {@link MetricsRegistryListener} to a collection of listeners that will be notified on
     * metric creation.  Listeners will be notified in the order in which they are added.
     * <p/>
     * <b>N.B.:</b> The listener will be notified of all existing metrics when it first registers.
     *
     * @param listener the listener that will be notified
     */
    public void addListener(MetricsRegistryListener listener) {
        listeners.add(listener);
        for (Map.Entry<MetricName, Metric> entry : metrics.entrySet()) {
            listener.onMetricAdded(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Removes a {@link MetricsRegistryListener} from this registry's collection of listeners.
     *
     * @param listener the listener that will be removed
     */
    public void removeListener(MetricsRegistryListener listener) {
        listeners.remove(listener);
    }

    /**
     * Override to customize how {@link MetricName}s are created.
     *
     * @param klass the class which owns the metric
     * @param name  the name of the metric
     * @param scope the metric's scope
     * @return the metric's full name
     */
    protected MetricName createName(Class<?> klass, String name, String scope) {
        return new MetricName(klass, name, scope);
    }

    /**
     * Returns a new {@link ConcurrentMap} implementation. Subclass this to do weird things with
     * your own {@link MetricsRegistry} implementation.
     *
     * @return a new {@link ConcurrentMap}
     */
    protected ConcurrentMap<MetricName, Metric> newMetricsMap() {
        return new ConcurrentHashMap<MetricName, Metric>(EXPECTED_METRIC_COUNT);
    }

    /**
     * Gets any existing metric with the given name or, if none exists, adds the given metric.
     *
     * @param name   the metric's name
     * @param metric the new metric
     * @param <T>    the type of the metric
     * @return either the existing metric or {@code metric}
     */
    @SuppressWarnings("unchecked")
    protected final <T extends Metric> T getOrAdd(MetricName name, T metric) {
        final Metric existingMetric = metrics.get(name);
        if (existingMetric == null) {
            final Metric justAddedMetric = metrics.putIfAbsent(name, metric);
            if (justAddedMetric == null) {
                notifyMetricAdded(name, metric);
                return metric;
            }

            if (metric instanceof Stoppable) {
                ((Stoppable) metric).stop();
            }

            return (T) justAddedMetric;
        }
        return (T) existingMetric;
    }

    private ScheduledExecutorService newMeterTickThreadPool() {
        return threadPools.newScheduledThreadPool(2, "meter-tick");
    }

    private void notifyMetricRemoved(MetricName name) {
        for (MetricsRegistryListener listener : listeners) {
            listener.onMetricRemoved(name);
        }
    }

    private void notifyMetricAdded(MetricName name, Metric metric) {
        for (MetricsRegistryListener listener : listeners) {
            listener.onMetricAdded(name, metric);
        }
    }
}
