package com.codahale.metrics;

import org.slf4j.Logger;
import org.slf4j.Marker;

import java.util.SortedMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.requireNonNull;

@Deprecated
public class Slf4jReporter extends ScheduledReporter {

    public static Slf4jReporter.Builder forRegistry(MetricRegistry registry) {
        return new Slf4jReporter.Builder(io.dropwizard.metrics5.Slf4jReporter.forRegistry(registry.getDelegate()));
    }

    public enum LoggingLevel {TRACE, DEBUG, INFO, WARN, ERROR}

    public static class Builder {

        private io.dropwizard.metrics5.Slf4jReporter.Builder delegate;

        private Builder(io.dropwizard.metrics5.Slf4jReporter.Builder delegate) {
            this.delegate = requireNonNull(delegate);
        }

        public Slf4jReporter.Builder shutdownExecutorOnStop(boolean shutdownExecutorOnStop) {
            delegate.shutdownExecutorOnStop(shutdownExecutorOnStop);
            return this;
        }

        public Slf4jReporter.Builder scheduleOn(ScheduledExecutorService executor) {
            delegate.scheduleOn(executor);
            return this;
        }

        public Slf4jReporter.Builder outputTo(Logger logger) {
            delegate.outputTo(logger);
            return this;
        }

        public Slf4jReporter.Builder markWith(Marker marker) {
            delegate.markWith(marker);
            return this;
        }

        public Slf4jReporter.Builder prefixedWith(String prefix) {
            delegate.prefixedWith(prefix);
            return this;
        }

        public Slf4jReporter.Builder convertRatesTo(TimeUnit rateUnit) {
            delegate.convertRatesTo(rateUnit);
            return this;
        }

        public Slf4jReporter.Builder convertDurationsTo(TimeUnit durationUnit) {
            delegate.convertDurationsTo(durationUnit);
            return this;
        }

        public Slf4jReporter.Builder filter(MetricFilter filter) {
            delegate.filter(filter.transform());
            return this;
        }

        public Slf4jReporter.Builder withLoggingLevel(LoggingLevel loggingLevel) {
            delegate.withLoggingLevel(io.dropwizard.metrics5.Slf4jReporter.LoggingLevel.valueOf(loggingLevel.name()));
            return this;
        }

        public Slf4jReporter build() {
            return new Slf4jReporter(delegate.build());
        }
    }

    private Slf4jReporter(io.dropwizard.metrics5.ScheduledReporter delegate) {
        super(delegate);
    }

    @Override
    public void report(SortedMap<String, Gauge> gauges, SortedMap<String, Counter> counters,
                       SortedMap<String, Histogram> histograms, SortedMap<String, Meter> meters,
                       SortedMap<String, Timer> timers) {
        getDelegate().report(transform(gauges), transform(counters), transform(histograms), transform(meters),
                transform(timers));
    }
}
