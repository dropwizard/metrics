package io.dropwizard.metrics5.influxdb;

import io.dropwizard.metrics5.Counter;
import io.dropwizard.metrics5.Gauge;
import io.dropwizard.metrics5.Histogram;
import io.dropwizard.metrics5.Meter;
import io.dropwizard.metrics5.MetricAttribute;
import io.dropwizard.metrics5.MetricFilter;
import io.dropwizard.metrics5.MetricName;
import io.dropwizard.metrics5.MetricRegistry;
import io.dropwizard.metrics5.MetricRegistryListener;
import io.dropwizard.metrics5.ScheduledReporter;
import io.dropwizard.metrics5.Timer;

import java.util.Set;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

abstract class GarbageFreeScheduledReporter extends ScheduledReporter {

    private final MetricRegistry registry;
    private final RegistryMirror mirror;

    protected GarbageFreeScheduledReporter(MetricRegistry registry, String name, MetricFilter filter, TimeUnit rateUnit,
                                           TimeUnit durationUnit, ScheduledExecutorService executor,
                                           boolean shutdownExecutorOnStop,
                                           Set<MetricAttribute> disabledMetricAttributes) {
        super(registry, name, filter, rateUnit, durationUnit, executor, shutdownExecutorOnStop, disabledMetricAttributes);
        this.registry = registry;
        this.mirror = new RegistryMirror(filter);
        registry.addListener(mirror);
    }

    @Override
    public void stop() {
        try {
            super.stop();
        } finally {
            registry.removeListener(mirror);
        }
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void report() {
        synchronized (this) {
            report(mirror.gauges(),
                    mirror.counters(),
                    mirror.histograms(),
                    mirror.meters(),
                    mirror.timers());
        }
    }

    @SuppressWarnings("rawtypes") // because of signature (for Gauge) in ScheduledReporter#report(..)
    private static class RegistryMirror implements MetricRegistryListener {

        private final MetricFilter filter;
        private final ConcurrentSkipListMap<MetricName, Gauge> gauges = new ConcurrentSkipListMap<>();
        private final ConcurrentSkipListMap<MetricName, Counter> counters = new ConcurrentSkipListMap<>();
        private final ConcurrentSkipListMap<MetricName, Histogram> histograms = new ConcurrentSkipListMap<>();
        private final ConcurrentSkipListMap<MetricName, Meter> meters = new ConcurrentSkipListMap<>();
        private final ConcurrentSkipListMap<MetricName, Timer> timers = new ConcurrentSkipListMap<>();

        RegistryMirror(MetricFilter filter) {
            this.filter = filter;
        }

        SortedMap<MetricName, Gauge> gauges() {
            return gauges;
        }

        SortedMap<MetricName, Counter> counters() {
            return counters;
        }

        SortedMap<MetricName, Histogram> histograms() {
            return histograms;
        }

        SortedMap<MetricName, Meter> meters() {
            return meters;
        }

        SortedMap<MetricName, Timer> timers() {
            return timers;
        }


        @Override
        public void onGaugeAdded(MetricName name, Gauge<?> gauge) {
            if (filter.matches(name, gauge)) {
                gauges.put(name, gauge);
            }
        }

        @Override
        public void onGaugeRemoved(MetricName name) {
            gauges.remove(name);
        }

        @Override
        public void onCounterAdded(MetricName name, Counter counter) {
            if (filter.matches(name, counter)) {
                counters.put(name, counter);
            }
        }

        @Override
        public void onCounterRemoved(MetricName name) {
            counters.remove(name);
        }

        @Override
        public void onHistogramAdded(MetricName name, Histogram histogram) {
            if (filter.matches(name, histogram)) {
                histograms.put(name, histogram);
            }
        }

        @Override
        public void onHistogramRemoved(MetricName name) {
            histograms.remove(name);
        }

        @Override
        public void onMeterAdded(MetricName name, Meter meter) {
            if (filter.matches(name, meter)) {
                meters.put(name, meter);
            }
        }

        @Override
        public void onMeterRemoved(MetricName name) {
            meters.remove(name);
        }

        @Override
        public void onTimerAdded(MetricName name, Timer timer) {
            if (filter.matches(name, timer)) {
                timers.put(name, timer);
            }
        }

        @Override
        public void onTimerRemoved(MetricName name) {
            timers.remove(name);
        }

    }

}
