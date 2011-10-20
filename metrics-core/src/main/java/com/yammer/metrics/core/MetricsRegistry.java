package com.yammer.metrics.core;

import com.yammer.metrics.core.HistogramMetric.SampleType;
import com.yammer.metrics.util.ThreadPools;

import javax.management.MalformedObjectNameException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A registry of metric instances.
 */
public class MetricsRegistry {

    private final ConcurrentMap<MetricName, Metric> metrics = newMetricsMap();
    private final ThreadPools threadPools = new ThreadPools();
    private final List<MetricsRegistryListener> listeners = Collections.synchronizedList(new ArrayList<MetricsRegistryListener>());


   /**
    * Adds a {@link MetricsRegistryListener} to a collection of listeners that will be notified on
    * metric creation.  Listeners will be notified in the order in which they are added.
    *
    * @param listener the listener that will be notified
    */
    public void addListener(MetricsRegistryListener listener) {
      listeners.add(listener);
    }

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
    public <T> GaugeMetric<T> newGauge(Class<?> klass,
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
    public <T> GaugeMetric<T> newGauge(Class<?> klass,
                                       String name,
                                       String scope,
                                       GaugeMetric<T> metric) {
        return newGauge(new MetricName(klass, name, scope), metric);
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
    public <T> GaugeMetric<T> newGauge(MetricName metricName,
                                       GaugeMetric<T> metric) {
        return getOrAdd(metricName, metric);
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
    public JmxGauge newJmxGauge(Class<?> klass,
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
    public JmxGauge newJmxGauge(Class<?> klass,
                                String name,
                                String scope,
                                String objectName,
                                String attribute) throws MalformedObjectNameException {
        return newJmxGauge(new MetricName(klass, name, scope), objectName, attribute);
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
    public JmxGauge newJmxGauge(MetricName metricName,
                                String objectName,
                                String attribute) throws MalformedObjectNameException {
        return getOrAdd(metricName, new JmxGauge(objectName, attribute));
    }

    /**
     * Creates a new {@link com.yammer.metrics.core.CounterMetric} and registers
     * it under the given class and name.
     *
     * @param klass the class which owns the metric
     * @param name  the name of the metric
     * @return a new {@link com.yammer.metrics.core.CounterMetric}
     */
    public CounterMetric newCounter(Class<?> klass,
                                    String name) {
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
    public CounterMetric newCounter(Class<?> klass,
                                    String name,
                                    String scope) {
        return newCounter(new MetricName(klass, name, scope));
    }

    /**
     * Creates a new {@link com.yammer.metrics.core.CounterMetric} and registers
     * it under the given metric name.
     *
     * @param metricName the name of the metric
     * @return a new {@link com.yammer.metrics.core.CounterMetric}
     */
    public CounterMetric newCounter(MetricName metricName) {
        return getOrAdd(metricName, new CounterMetric());
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
    public HistogramMetric newHistogram(Class<?> klass,
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
    public HistogramMetric newHistogram(Class<?> klass,
                                        String name,
                                        String scope,
                                        boolean biased) {
        return newHistogram(new MetricName(klass, name, scope), biased);
    }

    /**
     * Creates a new non-biased {@link HistogramMetric} and registers it under
     * the given class and name.
     *
     * @param klass the class which owns the metric
     * @param name the name of the metric
     * @return a new {@link HistogramMetric}
     */
    public HistogramMetric newHistogram(Class<?> klass,
                                        String name) {
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
    public HistogramMetric newHistogram(Class<?> klass,
                                        String name,
                                        String scope) {
        return newHistogram(klass, name, scope, false);
    }

    /**
     * Creates a new {@link HistogramMetric} and registers it under the given
     * metric name.
     *
     * @param metricName the name of the metric
     * @param biased whether or not the histogram should be biased
     * @return a new {@link HistogramMetric}
     */
    public HistogramMetric newHistogram(MetricName metricName,
                                        boolean biased) {
        return getOrAdd(metricName,
                new HistogramMetric(biased ? SampleType.BIASED : SampleType.UNIFORM));
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
    public MeterMetric newMeter(Class<?> klass,
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
    public MeterMetric newMeter(Class<?> klass,
                                String name,
                                String scope,
                                String eventType,
                                TimeUnit unit) {
        return newMeter(new MetricName(klass, name, scope), eventType, unit);
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
    public MeterMetric newMeter(MetricName metricName,
                                String eventType,
                                TimeUnit unit) {
        final Metric existingMetric = metrics.get(metricName);
        if (existingMetric == null) {
            final MeterMetric metric = MeterMetric.newMeter(newMeterTickThreadPool(), eventType, unit);
            final Metric justAddedMetric = metrics.putIfAbsent(metricName, metric);
            if (justAddedMetric == null) {
                notify(metricName, metric);
                return metric;
            }
            return (MeterMetric) justAddedMetric;
        }
        return (MeterMetric) existingMetric;
    }

    /**
     * Creates a new {@link TimerMetric} and registers it under the given class and name, measuring
     * elapsed time in milliseconds and invocations per second.
     *
     * @param klass        the class which owns the metric
     * @param name         the name of the metric
     * @return a new {@link TimerMetric}
     */
    public TimerMetric newTimer(Class<?> klass,
                                String name) {
        return newTimer(klass, name, null, TimeUnit.MILLISECONDS, TimeUnit.SECONDS);
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
    public TimerMetric newTimer(Class<?> klass,
                                String name,
                                TimeUnit durationUnit,
                                TimeUnit rateUnit) {
        return newTimer(klass, name, null, durationUnit, rateUnit);
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
    public TimerMetric newTimer(Class<?> klass,
                                String name,
                                String scope) {
        return newTimer(klass, name, scope, TimeUnit.MILLISECONDS, TimeUnit.SECONDS);
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
    public TimerMetric newTimer(Class<?> klass,
                                String name,
                                String scope,
                                TimeUnit durationUnit,
                                TimeUnit rateUnit) {
        return newTimer(new MetricName(klass, name, scope), durationUnit, rateUnit);
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
    public TimerMetric newTimer(MetricName metricName,
                                TimeUnit durationUnit,
                                TimeUnit rateUnit) {
        final Metric existingMetric = metrics.get(metricName);
        if (existingMetric == null) {
            final TimerMetric metric = new TimerMetric(newMeterTickThreadPool(), durationUnit, rateUnit);
            final Metric justAddedMetric = metrics.putIfAbsent(metricName, metric);
            if (justAddedMetric == null) {
                notify(metricName, metric);
                return metric;
            }
            return (TimerMetric) justAddedMetric;
        }
        return (TimerMetric) existingMetric;
    }
    /**
     * Returns an unmodifiable map of all metrics and their names.
     *
     * @return an unmodifiable map of all metrics and their names
     */
    public Map<MetricName, Metric> allMetrics() {
        return Collections.unmodifiableMap(metrics);
    }

    public ThreadPools threadPools() {
        return threadPools;
    }

    public ScheduledExecutorService newMeterTickThreadPool() {
        return threadPools.newScheduledThreadPool(2, "meter-tick");
    }

    /**
     * Removes the metric for the given class with the given name.
     *
     * @param klass the klass the metric is associated with
     * @param name the name of the metric
     */
    public void removeMetric(Class<?> klass,
                             String name) {
        removeMetric(klass, name, null);
    }

    /**
     * Removes the metric for the given class with the given name and scope.
     *
     * @param klass the klass the metric is associated with
     * @param name the name of the metric
     * @param scope the scope of the metric
     */
    public void removeMetric(Class<?> klass, String name,
                             String scope) {
        removeMetric(new MetricName(klass, name, scope));
    }

    /**
     * Removes the metric with the given name.
     *
     * @param name the name of the metric
     */
    public void removeMetric(MetricName name) {
        final Metric metric = metrics.remove(name);
        if (metric != null) {
            if (metric instanceof MeterMetric) {
                ((MeterMetric) metric).stop();
            } else if (metric instanceof TimerMetric) {
                ((TimerMetric) metric).stop();
            }
        }
    }

    /**
     * Returns a new {@link ConcurrentMap} implementation. Subclass this to do
     * weird things with your own {@link MetricsRegistry} implementation.
     *
     * @return a new {@link ConcurrentMap}
     */
    protected ConcurrentMap<MetricName, Metric> newMetricsMap() {
        return new ConcurrentHashMap<MetricName, Metric>();
    }

    @SuppressWarnings("unchecked")
    private <T extends Metric> T getOrAdd(MetricName name, T metric) {
        final Metric existingMetric = metrics.get(name);
        if (existingMetric == null) {
            final Metric justAddedMetric = metrics.putIfAbsent(name, metric);
            if (justAddedMetric == null) {
                notify(name, metric);
                return metric;
            }
            return (T) justAddedMetric;
        }
        return (T) existingMetric;
    }

    private void notify(MetricName name, Metric metric) {
      // clone the listeners for thread safety
      for (MetricsRegistryListener listener : listeners.toArray(new MetricsRegistryListener[] {})) {
        listener.newMetric(name, metric);
      }
    }

}
