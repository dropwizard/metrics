package com.codahale.metrics;

import io.dropwizard.metrics5.MetricName;

import java.io.Closeable;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.requireNonNull;

@Deprecated
public abstract class ScheduledReporter implements Closeable, Reporter {

    private io.dropwizard.metrics5.ScheduledReporter delegate;

    protected ScheduledReporter(io.dropwizard.metrics5.ScheduledReporter delegate) {
        this.delegate = requireNonNull(delegate);
    }

    protected ScheduledReporter(MetricRegistry registry, String name, MetricFilter filter, TimeUnit rateUnit,
                                TimeUnit durationUnit) {
        delegate = new Adapter(registry, name, filter, rateUnit, durationUnit, this);
    }

    protected ScheduledReporter(MetricRegistry registry, String name, MetricFilter filter, TimeUnit rateUnit,
                                TimeUnit durationUnit, ScheduledExecutorService executor) {
        delegate = new Adapter(registry, name, filter, rateUnit, durationUnit, executor, this);
    }

    protected ScheduledReporter(MetricRegistry registry, String name, MetricFilter filter, TimeUnit rateUnit,
                                TimeUnit durationUnit, ScheduledExecutorService executor, boolean shutdownExecutorOnStop) {
        delegate = new Adapter(registry, name, filter, rateUnit, durationUnit, executor, shutdownExecutorOnStop, this);
    }

    protected ScheduledReporter(MetricRegistry registry, String name, MetricFilter filter, TimeUnit rateUnit,
                                TimeUnit durationUnit, ScheduledExecutorService executor, boolean shutdownExecutorOnStop,
                                Set<MetricAttribute> disabledMetricAttributes) {
        delegate = new Adapter(registry, name, filter, rateUnit, durationUnit, executor, shutdownExecutorOnStop,
                disabledMetricAttributes, this);
    }

    public void start(long period, TimeUnit unit) {
        delegate.start(period, unit);
    }

    synchronized public void start(long initialDelay, long period, TimeUnit unit) {
        delegate.start(initialDelay, period, unit);
    }

    public void stop() {
        delegate.stop();
    }

    @Override
    public void close() {
        delegate.close();
    }

    public void report() {
        delegate.report();
    }

    public io.dropwizard.metrics5.ScheduledReporter getDelegate() {
        return delegate;
    }

    @SuppressWarnings("rawtypes")
    public abstract void report(SortedMap<String, Gauge> gauges,
                                SortedMap<String, Counter> counters,
                                SortedMap<String, Histogram> histograms,
                                SortedMap<String, Meter> meters,
                                SortedMap<String, Timer> timers);

    @SuppressWarnings("unchecked")
    protected <T extends io.dropwizard.metrics5.Metric> SortedMap<MetricName, T> transform(
            SortedMap<String, ? extends Metric> metrics) {
        final SortedMap<MetricName, T> items = new TreeMap<>();
        for (Map.Entry<String, ? extends Metric> entry : metrics.entrySet()) {
            items.put(MetricName.build(entry.getKey()), (T) entry.getValue().getDelegate());
        }
        return Collections.unmodifiableSortedMap(items);
    }

    protected String getRateUnit() {
        return delegate.getRateUnit();
    }

    protected String getDurationUnit() {
        return delegate.getDurationUnit();
    }

    protected double convertDuration(double duration) {
        return delegate.convertDuration(duration);
    }

    protected double convertRate(double rate) {
        return delegate.convertRate(rate);
    }

    protected boolean isShutdownExecutorOnStop() {
        return delegate.isShutdownExecutorOnStop();
    }

    protected Set<io.dropwizard.metrics5.MetricAttribute> getDisabledMetricAttributes() {
        return delegate.getDisabledMetricAttributes();
    }

    public static class Adapter extends io.dropwizard.metrics5.ScheduledReporter {

        private final ScheduledReporter delegate;

        public Adapter(MetricRegistry registry, String name, MetricFilter filter, TimeUnit rateUnit, TimeUnit durationUnit,
                       ScheduledReporter delegate) {
            super(registry.getDelegate(), name, filter.transform(), rateUnit, durationUnit);
            this.delegate = delegate;
        }

        public Adapter(MetricRegistry registry, String name, MetricFilter filter, TimeUnit rateUnit, TimeUnit durationUnit,
                       ScheduledExecutorService executor, ScheduledReporter delegate) {
            super(registry.getDelegate(), name, filter.transform(), rateUnit, durationUnit, executor);
            this.delegate = delegate;
        }

        public Adapter(MetricRegistry registry, String name, MetricFilter filter, TimeUnit rateUnit, TimeUnit durationUnit,
                       ScheduledExecutorService executor, boolean shutdownExecutorOnStop, ScheduledReporter delegate) {
            super(registry.getDelegate(), name, filter.transform(), rateUnit, durationUnit, executor, shutdownExecutorOnStop);
            this.delegate = delegate;
        }

        public Adapter(MetricRegistry registry, String name, MetricFilter filter, TimeUnit rateUnit, TimeUnit durationUnit,
                       ScheduledExecutorService executor, boolean shutdownExecutorOnStop, Set<MetricAttribute> disabledMetricAttributes,
                       ScheduledReporter delegate) {
            super(registry.getDelegate(), name, filter.transform(), rateUnit, durationUnit, executor, shutdownExecutorOnStop,
                    MetricAttribute.transform(disabledMetricAttributes));
            this.delegate = delegate;
        }

        @Override
        public void report(SortedMap<MetricName, io.dropwizard.metrics5.Gauge> gauges, SortedMap<MetricName, io.dropwizard.metrics5.Counter> counters,
                           SortedMap<MetricName, io.dropwizard.metrics5.Histogram> histograms, SortedMap<MetricName, io.dropwizard.metrics5.Meter> meters,
                           SortedMap<MetricName, io.dropwizard.metrics5.Timer> timers) {
            delegate.report(MetricRegistry.adaptMetrics(gauges), MetricRegistry.adaptMetrics(counters),
                    MetricRegistry.adaptMetrics(histograms), MetricRegistry.adaptMetrics(meters),
                    MetricRegistry.adaptMetrics(timers));
        }
    }
}
