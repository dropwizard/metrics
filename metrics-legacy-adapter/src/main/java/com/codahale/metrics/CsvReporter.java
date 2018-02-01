package com.codahale.metrics;

import java.io.File;
import java.util.Locale;
import java.util.SortedMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Deprecated
public class CsvReporter extends ScheduledReporter {

    public static Builder forRegistry(MetricRegistry registry) {
        return new Builder(registry);
    }

    public static class Builder {

        private io.dropwizard.metrics5.CsvReporter.Builder delegate;

        private Builder(MetricRegistry metricRegistry) {
            delegate = io.dropwizard.metrics5.CsvReporter.forRegistry(metricRegistry.getDelegate());
        }

        public CsvReporter.Builder shutdownExecutorOnStop(boolean shutdownExecutorOnStop) {
            delegate.shutdownExecutorOnStop(shutdownExecutorOnStop);
            return this;
        }


        public CsvReporter.Builder scheduleOn(ScheduledExecutorService executor) {
            delegate.scheduleOn(executor);
            return this;
        }

        public CsvReporter.Builder formatFor(Locale locale) {
            delegate.formatFor(locale);
            return this;
        }


        public CsvReporter.Builder convertRatesTo(TimeUnit rateUnit) {
            delegate.convertRatesTo(rateUnit);
            return this;
        }

        public CsvReporter.Builder convertDurationsTo(TimeUnit durationUnit) {
            delegate.convertDurationsTo(durationUnit);
            return this;
        }

        public CsvReporter.Builder withSeparator(String separator) {
            delegate.withSeparator(separator);
            return this;
        }

        public CsvReporter.Builder withClock(Clock clock) {
            delegate.withClock(clock.getDelegate());
            return this;
        }

        public CsvReporter.Builder filter(MetricFilter filter) {
            delegate.filter(filter.transform());
            return this;
        }

        public Builder withCsvFileProvider(CsvFileProvider csvFileProvider) {
            delegate.withCsvFileProvider(csvFileProvider::getFile);
            return this;
        }

        public CsvReporter build(File directory) {
            return new CsvReporter(delegate.build(directory));
        }
    }
    
    public CsvReporter(io.dropwizard.metrics5.CsvReporter delegate) {
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
