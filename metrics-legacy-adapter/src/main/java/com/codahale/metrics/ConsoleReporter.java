package com.codahale.metrics;

import java.io.PrintStream;
import java.util.Locale;
import java.util.Set;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Deprecated
public class ConsoleReporter extends ScheduledReporter {

    public static Builder forRegistry(MetricRegistry registry) {
        return new Builder(registry);
    }

    public static class Builder {

        private io.dropwizard.metrics5.ConsoleReporter.Builder delegate;

        private Builder(MetricRegistry metricRegistry) {
            delegate = io.dropwizard.metrics5.ConsoleReporter.forRegistry(metricRegistry.getDelegate());
        }

        public Builder shutdownExecutorOnStop(boolean shutdownExecutorOnStop) {
            delegate.shutdownExecutorOnStop(shutdownExecutorOnStop);
            return this;
        }

        public Builder scheduleOn(ScheduledExecutorService executor) {
            delegate.scheduleOn(executor);
            return this;
        }

        public Builder outputTo(PrintStream output) {
            delegate.outputTo(output);
            return this;
        }

        public Builder formattedFor(Locale locale) {
            delegate.formattedFor(locale);
            return this;
        }

        public Builder withClock(Clock clock) {
            delegate.withClock(clock.getDelegate());
            return this;
        }

        public Builder formattedFor(TimeZone timeZone) {
            delegate.formattedFor(timeZone);
            return this;
        }

        public Builder convertRatesTo(TimeUnit rateUnit) {
            delegate.convertRatesTo(rateUnit);
            return this;
        }

        public Builder convertDurationsTo(TimeUnit durationUnit) {
            delegate.convertDurationsTo(durationUnit);
            return this;
        }

        public Builder filter(MetricFilter filter) {
            delegate.filter(filter.transform());
            return this;
        }

        public Builder disabledMetricAttributes(Set<MetricAttribute> disabledMetricAttributes) {
            delegate.disabledMetricAttributes(MetricAttribute.transform(disabledMetricAttributes));
            return this;
        }

        public ConsoleReporter build() {
            return new ConsoleReporter(delegate.build());
        }
    }

    private ConsoleReporter(io.dropwizard.metrics5.ScheduledReporter delegate) {
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
