package io.dropwizard.metrics5;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A registry of metric instances.
 */
public class MetricRegistry implements MetricSet {

    /**
     * Concatenates elements to form a dotted name, eliding any null values or empty strings.
     *
     * @param name  the first element of the name
     * @param names the remaining elements of the name
     * @return {@code name} and {@code names} concatenated by periods
     */
    public static MetricName name(String name, String... names) {
        if (names == null) {
            return MetricName.build(name);
        }

        final String[] parts = new String[names.length + 1];
        parts[0] = name;
        System.arraycopy(names, 0, parts, 1, names.length);
        return MetricName.build(parts);
    }

    /**
     * Concatenates a class name and elements to form a dotted name, eliding any null values or
     * empty strings.
     *
     * @param klass the first element of the name
     * @param names the remaining elements of the name
     * @return {@code klass} and {@code names} concatenated by periods
     */
    public static MetricName name(Class<?> klass, String... names) {
        return name(klass.getName(), names);
    }

    private final ConcurrentMap<MetricName, Metric> metrics;
    private final List<MetricRegistryListener> listeners;

    /**
     * Creates a new {@link MetricRegistry}.
     */
    public MetricRegistry() {
        this.metrics = buildMap();
        this.listeners = new CopyOnWriteArrayList<>();
    }

    /**
     * Creates a new {@link ConcurrentMap} implementation for use inside the registry. Override this
     * to create a {@link MetricRegistry} with space- or time-bounded metric lifecycles, for
     * example.
     *
     * @return a new {@link ConcurrentMap}
     */
    protected ConcurrentMap<MetricName, Metric> buildMap() {
        return new ConcurrentHashMap<>();
    }

    /**
     * @see #register(MetricName, Metric)
     */
    @SuppressWarnings("unchecked")
    public <T extends Metric> T register(String name, T metric) throws IllegalArgumentException {
        return register(MetricName.build(name), metric);
    }


    /**
     * Given a {@link Metric}, registers it under the given name.
     *
     * @param name   the name of the metric
     * @param metric the metric
     * @param <T>    the type of the metric
     * @return {@code metric}
     * @throws IllegalArgumentException if the name is already registered
     */
    @SuppressWarnings("unchecked")
    public <T extends Metric> T register(MetricName name, T metric) throws IllegalArgumentException {
        if (metric instanceof MetricSet) {
            registerAll(name, (MetricSet) metric);
        } else {
            final Metric existing = metrics.putIfAbsent(name, metric);
            if (existing == null) {
                onMetricAdded(name, metric);
            } else {
                throw new IllegalArgumentException("A metric named " + name + " already exists");
            }
        }
        return metric;
    }

    /**
     * Given a metric set, registers them.
     *
     * @param metrics a set of metrics
     * @throws IllegalArgumentException if any of the names are already registered
     */
    public void registerAll(MetricSet metrics) throws IllegalArgumentException {
        registerAll(null, metrics);
    }

    /**
     * @see #counter(MetricName)
     */
    public Counter counter(String name) {
        return getOrAdd(MetricName.build(name), MetricBuilder.COUNTERS);
    }

    /**
     * Return the {@link Counter} registered under this name; or create and register
     * a new {@link Counter} if none is registered.
     *
     * @param name the name of the metric
     * @return a new or pre-existing {@link Counter}
     */
    public Counter counter(MetricName name) {
        return getOrAdd(name, MetricBuilder.COUNTERS);
    }

    /**
     * @see #histogram(MetricName)
     */
    public Histogram histogram(String name) {
        return getOrAdd(MetricName.build(name), MetricBuilder.HISTOGRAMS);
    }

    /**
     * Return the {@link Counter} registered under this name; or create and register
     * a new {@link Counter} using the provided MetricSupplier if none is registered.
     *
     * @param name     the name of the metric
     * @param supplier a MetricSupplier that can be used to manufacture a counter.
     * @return a new or pre-existing {@link Counter}
     */
    public <T extends Counter> T counter(MetricName name, final MetricSupplier<T> supplier) {
        return getOrAdd(name, new MetricBuilder<T>() {
            @Override
            public T newMetric() {
                return supplier.newMetric();
            }

            @Override
            public boolean isInstance(Metric metric) {
                return Counter.class.isInstance(metric);
            }
        });
    }

    /**
     * Return the {@link Histogram} registered under this name; or create and register
     * a new {@link Histogram} if none is registered.
     *
     * @param name the name of the metric
     * @return a new or pre-existing {@link Histogram}
     */
    public Histogram histogram(MetricName name) {
        return getOrAdd(name, MetricBuilder.HISTOGRAMS);
    }

    /**
     * @see #meter(MetricName)
     */
    public Meter meter(String name) {
        return getOrAdd(MetricName.build(name), MetricBuilder.METERS);
    }

    /**
     * Return the {@link Histogram} registered under this name; or create and register
     * a new {@link Histogram} using the provided MetricSupplier if none is registered.
     *
     * @param name     the name of the metric
     * @param supplier a MetricSupplier that can be used to manufacture a histogram
     * @return a new or pre-existing {@link Histogram}
     */
    public Histogram histogram(MetricName name, final MetricSupplier<Histogram> supplier) {
        return getOrAdd(name, new MetricBuilder<Histogram>() {
            @Override
            public Histogram newMetric() {
                return supplier.newMetric();
            }

            @Override
            public boolean isInstance(Metric metric) {
                return Histogram.class.isInstance(metric);
            }
        });
    }

    /**
     * Return the {@link Meter} registered under this name; or create and register
     * a new {@link Meter} if none is registered.
     *
     * @param name the name of the metric
     * @return a new or pre-existing {@link Meter}
     */
    public Meter meter(MetricName name) {
        return getOrAdd(name, MetricBuilder.METERS);
    }

    /**
     * @see #timer(MetricName)
     */
    public Timer timer(String name) {
        return getOrAdd(MetricName.build(name), MetricBuilder.TIMERS);
    }

    /**
     * Return the {@link Meter} registered under this name; or create and register
     * a new {@link Meter} using the provided MetricSupplier if none is registered.
     *
     * @param name     the name of the metric
     * @param supplier a MetricSupplier that can be used to manufacture a Meter
     * @return a new or pre-existing {@link Meter}
     */
    public Meter meter(MetricName name, final MetricSupplier<Meter> supplier) {
        return getOrAdd(name, new MetricBuilder<Meter>() {
            @Override
            public Meter newMetric() {
                return supplier.newMetric();
            }

            @Override
            public boolean isInstance(Metric metric) {
                return Meter.class.isInstance(metric);
            }
        });
    }

    /**
     * Return the {@link Timer} registered under this name; or create and register
     * a new {@link Timer} if none is registered.
     *
     * @param name the name of the metric
     * @return a new or pre-existing {@link Timer}
     */
    public Timer timer(MetricName name) {
        return getOrAdd(name, MetricBuilder.TIMERS);
    }

    /**
     * Return the {@link Timer} registered under this name; or create and register
     * a new {@link Timer} using the provided MetricSupplier if none is registered.
     *
     * @param name     the name of the metric
     * @param supplier a MetricSupplier that can be used to manufacture a Timer
     * @return a new or pre-existing {@link Timer}
     */
    public Timer timer(MetricName name, final MetricSupplier<Timer> supplier) {
        return getOrAdd(name, new MetricBuilder<Timer>() {
            @Override
            public Timer newMetric() {
                return supplier.newMetric();
            }

            @Override
            public boolean isInstance(Metric metric) {
                return Timer.class.isInstance(metric);
            }
        });
    }

    /**
     * Return the {@link Gauge} registered under this name; or create and register
     * a new {@link Gauge} using the provided MetricSupplier if none is registered.
     *
     * @param name     the name of the metric
     * @param supplier a MetricSupplier that can be used to manufacture a Gauge
     * @return a new or pre-existing {@link Gauge}
     */
    @SuppressWarnings("rawtypes")
    public Gauge gauge(MetricName name, final MetricSupplier<Gauge> supplier) {
        return getOrAdd(name, new MetricBuilder<Gauge>() {
            @Override
            public Gauge newMetric() {
                return supplier.newMetric();
            }

            @Override
            public boolean isInstance(Metric metric) {
                return Gauge.class.isInstance(metric);
            }
        });
    }


    /**
     * Removes the metric with the given name.
     *
     * @param name the name of the metric
     * @return whether or not the metric was removed
     */
    public boolean remove(MetricName name) {
        final Metric metric = metrics.remove(name);
        if (metric != null) {
            onMetricRemoved(name, metric);
            return true;
        }
        return false;
    }

    /**
     * Removes all metrics which match the given filter.
     *
     * @param filter a filter
     */
    public void removeMatching(MetricFilter filter) {
        for (Map.Entry<MetricName, Metric> entry : metrics.entrySet()) {
            if (filter.matches(entry.getKey(), entry.getValue())) {
                remove(entry.getKey());
            }
        }
    }

    /**
     * Adds a {@link MetricRegistryListener} to a collection of listeners that will be notified on
     * metric creation.  Listeners will be notified in the order in which they are added.
     * <p/>
     * <b>N.B.:</b> The listener will be notified of all existing metrics when it first registers.
     *
     * @param listener the listener that will be notified
     */
    public void addListener(MetricRegistryListener listener) {
        listeners.add(listener);

        for (Map.Entry<MetricName, Metric> entry : metrics.entrySet()) {
            notifyListenerOfAddedMetric(listener, entry.getValue(), entry.getKey());
        }
    }

    /**
     * Removes a {@link MetricRegistryListener} from this registry's collection of listeners.
     *
     * @param listener the listener that will be removed
     */
    public void removeListener(MetricRegistryListener listener) {
        listeners.remove(listener);
    }

    /**
     * Returns a set of the names of all the metrics in the registry.
     *
     * @return the names of all the metrics
     */
    public SortedSet<MetricName> getNames() {
        return Collections.unmodifiableSortedSet(new TreeSet<>(metrics.keySet()));
    }

    /**
     * Returns a map of all the gauges in the registry and their names.
     *
     * @return all the gauges in the registry
     */
    @SuppressWarnings("rawtypes")
    public SortedMap<MetricName, Gauge> getGauges() {
        return getGauges(MetricFilter.ALL);
    }

    /**
     * Returns a map of all the gauges in the registry and their names which match the given filter.
     *
     * @param filter the metric filter to match
     * @return all the gauges in the registry
     */
    @SuppressWarnings("rawtypes")
    public SortedMap<MetricName, Gauge> getGauges(MetricFilter filter) {
        return getMetrics(Gauge.class, filter);
    }

    /**
     * Returns a map of all the counters in the registry and their names.
     *
     * @return all the counters in the registry
     */
    public SortedMap<MetricName, Counter> getCounters() {
        return getCounters(MetricFilter.ALL);
    }

    /**
     * Returns a map of all the counters in the registry and their names which match the given
     * filter.
     *
     * @param filter the metric filter to match
     * @return all the counters in the registry
     */
    public SortedMap<MetricName, Counter> getCounters(MetricFilter filter) {
        return getMetrics(Counter.class, filter);
    }

    /**
     * Returns a map of all the histograms in the registry and their names.
     *
     * @return all the histograms in the registry
     */
    public SortedMap<MetricName, Histogram> getHistograms() {
        return getHistograms(MetricFilter.ALL);
    }

    /**
     * Returns a map of all the histograms in the registry and their names which match the given
     * filter.
     *
     * @param filter the metric filter to match
     * @return all the histograms in the registry
     */
    public SortedMap<MetricName, Histogram> getHistograms(MetricFilter filter) {
        return getMetrics(Histogram.class, filter);
    }

    /**
     * Returns a map of all the meters in the registry and their names.
     *
     * @return all the meters in the registry
     */
    public SortedMap<MetricName, Meter> getMeters() {
        return getMeters(MetricFilter.ALL);
    }

    /**
     * Returns a map of all the meters in the registry and their names which match the given filter.
     *
     * @param filter the metric filter to match
     * @return all the meters in the registry
     */
    public SortedMap<MetricName, Meter> getMeters(MetricFilter filter) {
        return getMetrics(Meter.class, filter);
    }

    /**
     * Returns a map of all the timers in the registry and their names.
     *
     * @return all the timers in the registry
     */
    public SortedMap<MetricName, Timer> getTimers() {
        return getTimers(MetricFilter.ALL);
    }

    /**
     * Returns a map of all the timers in the registry and their names which match the given filter.
     *
     * @param filter the metric filter to match
     * @return all the timers in the registry
     */
    public SortedMap<MetricName, Timer> getTimers(MetricFilter filter) {
        return getMetrics(Timer.class, filter);
    }

    @SuppressWarnings("unchecked")
    private <T extends Metric> T getOrAdd(MetricName name, MetricBuilder<T> builder) {
        final Metric metric = metrics.get(name);
        if (builder.isInstance(metric)) {
            return (T) metric;
        } else if (metric == null) {
            try {
                return register(name, builder.newMetric());
            } catch (IllegalArgumentException e) {
                final Metric added = metrics.get(name);
                if (builder.isInstance(added)) {
                    return (T) added;
                }
            }
        }
        throw new IllegalArgumentException(name + " is already used for a different type of metric");
    }

    @SuppressWarnings("unchecked")
    private <T extends Metric> SortedMap<MetricName, T> getMetrics(Class<T> klass, MetricFilter filter) {
        final TreeMap<MetricName, T> timers = new TreeMap<>();
        for (Map.Entry<MetricName, Metric> entry : metrics.entrySet()) {
            if (klass.isInstance(entry.getValue()) && filter.matches(entry.getKey(),
                    entry.getValue())) {
                timers.put(entry.getKey(), (T) entry.getValue());
            }
        }
        return Collections.unmodifiableSortedMap(timers);
    }

    private void onMetricAdded(MetricName name, Metric metric) {
        for (MetricRegistryListener listener : listeners) {
            notifyListenerOfAddedMetric(listener, metric, name);
        }
    }

    private void notifyListenerOfAddedMetric(MetricRegistryListener listener, Metric metric, MetricName name) {
        if (metric instanceof Gauge) {
            listener.onGaugeAdded(name, (Gauge<?>) metric);
        } else if (metric instanceof Counter) {
            listener.onCounterAdded(name, (Counter) metric);
        } else if (metric instanceof Histogram) {
            listener.onHistogramAdded(name, (Histogram) metric);
        } else if (metric instanceof Meter) {
            listener.onMeterAdded(name, (Meter) metric);
        } else if (metric instanceof Timer) {
            listener.onTimerAdded(name, (Timer) metric);
        } else {
            throw new IllegalArgumentException("Unknown metric type: " + metric.getClass());
        }
    }

    private void onMetricRemoved(MetricName name, Metric metric) {
        for (MetricRegistryListener listener : listeners) {
            notifyListenerOfRemovedMetric(name, metric, listener);
        }
    }

    private void notifyListenerOfRemovedMetric(MetricName name, Metric metric, MetricRegistryListener listener) {
        if (metric instanceof Gauge) {
            listener.onGaugeRemoved(name);
        } else if (metric instanceof Counter) {
            listener.onCounterRemoved(name);
        } else if (metric instanceof Histogram) {
            listener.onHistogramRemoved(name);
        } else if (metric instanceof Meter) {
            listener.onMeterRemoved(name);
        } else if (metric instanceof Timer) {
            listener.onTimerRemoved(name);
        } else {
            throw new IllegalArgumentException("Unknown metric type: " + metric.getClass());
        }
    }

    public void registerAll(MetricName prefix, MetricSet metrics) throws IllegalArgumentException {
        if (prefix == null) {
            prefix = MetricName.EMPTY;
        }

        for (Map.Entry<MetricName, Metric> entry : metrics.getMetrics().entrySet()) {
            if (entry.getValue() instanceof MetricSet) {
                registerAll(prefix.append(entry.getKey()), (MetricSet) entry.getValue());
            } else {
                register(prefix.append(entry.getKey()), entry.getValue());
            }
        }
    }

    @Override
    public Map<MetricName, Metric> getMetrics() {
        return Collections.unmodifiableMap(metrics);
    }

    @FunctionalInterface
    public interface MetricSupplier<T extends Metric> {
        T newMetric();
    }

    /**
     * A quick and easy way of capturing the notion of default metrics.
     */
    private interface MetricBuilder<T extends Metric> {
        MetricBuilder<Counter> COUNTERS = new MetricBuilder<Counter>() {
            @Override
            public Counter newMetric() {
                return new Counter();
            }

            @Override
            public boolean isInstance(Metric metric) {
                return Counter.class.isInstance(metric);
            }
        };

        MetricBuilder<Histogram> HISTOGRAMS = new MetricBuilder<Histogram>() {
            @Override
            public Histogram newMetric() {
                return new Histogram(new ExponentiallyDecayingReservoir());
            }

            @Override
            public boolean isInstance(Metric metric) {
                return Histogram.class.isInstance(metric);
            }
        };

        MetricBuilder<Meter> METERS = new MetricBuilder<Meter>() {
            @Override
            public Meter newMetric() {
                return new Meter();
            }

            @Override
            public boolean isInstance(Metric metric) {
                return Meter.class.isInstance(metric);
            }
        };

        MetricBuilder<Timer> TIMERS = new MetricBuilder<Timer>() {
            @Override
            public Timer newMetric() {
                return new Timer();
            }

            @Override
            public boolean isInstance(Metric metric) {
                return Timer.class.isInstance(metric);
            }
        };

        T newMetric();

        boolean isInstance(Metric metric);
    }
}
