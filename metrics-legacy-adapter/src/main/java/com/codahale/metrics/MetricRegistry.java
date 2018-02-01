package com.codahale.metrics;

import io.dropwizard.metrics5.MetricName;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static java.util.Collections.unmodifiableSortedSet;
import static java.util.Objects.requireNonNull;

@Deprecated
public class MetricRegistry implements MetricSet {

    private final io.dropwizard.metrics5.MetricRegistry delegate;

    public static String name(Class<?> klass, String... names) {
        return io.dropwizard.metrics5.MetricRegistry.name(klass, names).getKey();
    }

    public static String name(String name, String... names) {
        return io.dropwizard.metrics5.MetricRegistry.name(name, names).getKey();
    }

    public MetricRegistry() {
        this(new io.dropwizard.metrics5.MetricRegistry());
    }

    public MetricRegistry(io.dropwizard.metrics5.MetricRegistry delegate) {
        this.delegate = requireNonNull(delegate);
    }

    public <T extends Metric> T register(String name, T metric) throws IllegalArgumentException {
        delegate.register(MetricName.build(name), metric.getDelegate());
        return metric;
    }

    public void registerAll(MetricSet metrics) throws IllegalArgumentException {
        delegate.registerAll(metrics.getDelegate());
    }

    @SuppressWarnings("unchecked")
    public Gauge gauge(String name, MetricSupplier<Gauge> supplier) {
        return Gauge.of(delegate.gauge(MetricName.build(name), supplier.transform()));
    }

    public Counter counter(String name) {
        return new Counter(delegate.counter(name));
    }

    public Counter counter(String name, MetricSupplier<Counter> supplier) {
        return new Counter(delegate.counter(MetricName.build(name), supplier.transform()));
    }

    public Histogram histogram(String name) {
        return new Histogram(delegate.histogram(MetricName.build(name)));
    }

    public Histogram histogram(String name, MetricSupplier<Histogram> supplier) {
        return new Histogram(delegate.histogram(MetricName.build(name), supplier.transform()));
    }

    public Meter meter(String name) {
        return new Meter(delegate.meter(MetricName.build(name)));
    }

    public Meter meter(String name, MetricSupplier<Meter> supplier) {
        return new Meter(delegate.meter(MetricName.build(name), supplier.transform()));
    }

    public Timer timer(String name) {
        return new Timer(delegate.timer(MetricName.build(name)));
    }

    public Timer timer(String name, MetricSupplier<Timer> supplier) {
        return new Timer(delegate.timer(MetricName.build(name), supplier.transform()));
    }

    public boolean remove(String name) {
        return delegate.remove(MetricName.build(name));
    }

    public void removeMatching(MetricFilter filter) {
        delegate.removeMatching(filter.transform());
    }

    public void addListener(MetricRegistryListener listener) {
        delegate.addListener(new MetricRegistryListener.Adapter(listener));
    }

    public void removeListener(MetricRegistryListener listener) {
        delegate.removeListener(new MetricRegistryListener.Adapter(listener));
    }

    public SortedSet<String> getNames() {
        return unmodifiableSortedSet(delegate.getNames()
                .stream()
                .map(MetricName::getKey)
                .collect(Collectors.toCollection(TreeSet::new)));
    }

    public SortedMap<String, Gauge> getGauges() {
        return adaptMetrics(delegate.getGauges());
    }

    public SortedMap<String, Gauge> getGauges(MetricFilter filter) {
        return adaptMetrics(delegate.getGauges(filter.transform()));
    }

    public SortedMap<String, Counter> getCounters() {
        return adaptMetrics(delegate.getCounters());
    }

    public SortedMap<String, Counter> getCounters(MetricFilter filter) {
        return adaptMetrics(delegate.getCounters(filter.transform()));
    }

    public SortedMap<String, Histogram> getHistograms() {
        return adaptMetrics(delegate.getHistograms());
    }

    public SortedMap<String, Histogram> getHistograms(MetricFilter filter) {
        return adaptMetrics(delegate.getHistograms(filter.transform()));
    }

    public SortedMap<String, Meter> getMeters() {
        return adaptMetrics(delegate.getMeters());
    }

    public SortedMap<String, Meter> getMeters(MetricFilter filter) {
        return adaptMetrics(delegate.getMeters(filter.transform()));
    }

    public SortedMap<String, Timer> getTimers() {
        return adaptMetrics(delegate.getTimers());
    }

    public SortedMap<String, Timer> getTimers(MetricFilter filter) {
        return adaptMetrics(delegate.getTimers(filter.transform()));
    }

    @Override
    public Map<String, Metric> getMetrics() {
        return adaptMetrics(delegate.getMetrics());
    }

    @SuppressWarnings("unchecked")
    static <T extends Metric> SortedMap<String, T> adaptMetrics(
            Map<MetricName, ? extends io.dropwizard.metrics5.Metric> metrics) {
        final SortedMap<String, T> items = new TreeMap<>();
        for (Map.Entry<MetricName, ? extends io.dropwizard.metrics5.Metric> entry : metrics.entrySet()) {
            items.put(entry.getKey().getKey(), (T) Metric.of(entry.getValue()));
        }
        return Collections.unmodifiableSortedMap(items);
    }

    @Override
    public io.dropwizard.metrics5.MetricRegistry getDelegate() {
        return delegate;
    }

    @FunctionalInterface
    public interface MetricSupplier<T extends Metric> {

        T newMetric();

        @SuppressWarnings("unchecked")
        default <M extends io.dropwizard.metrics5.Metric>
        io.dropwizard.metrics5.MetricRegistry.MetricSupplier<M> transform() {
            MetricSupplier<T> original = this;
            return () -> (M) original.newMetric().getDelegate();
        }
    }
}
