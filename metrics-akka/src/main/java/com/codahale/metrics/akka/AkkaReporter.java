package com.codahale.metrics.akka;

import akka.actor.ActorRef;
import com.codahale.metrics.*;
import com.codahale.metrics.Timer;

import java.text.DateFormat;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A {@link ScheduledReporter} subclass that periodically pumps collected metrics to an Akka actor.
 */
public class AkkaReporter extends ScheduledReporter {
    private ActorRef metricReceiver;
    private final Clock clock;

    private AkkaReporter(MetricRegistry registry,
                          ActorRef metricReceiver,
                          Locale locale,
                          Clock clock,
                          TimeZone timeZone,
                          TimeUnit rateUnit,
                          TimeUnit durationUnit,
                          MetricFilter filter,
                          ScheduledExecutorService executor,
                          boolean shutdownExecutorOnStop,
                          Set<MetricAttribute> disabledMetricAttributes) {
        super(registry, "akka-reporter", filter, rateUnit, durationUnit, executor, shutdownExecutorOnStop,
            disabledMetricAttributes);

        this.metricReceiver = metricReceiver;
        this.clock = clock;
        DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM, locale);

        dateFormat.setTimeZone(timeZone);
    }

    @Override
    public void report(SortedMap<String, Gauge> gauges, SortedMap<String, Counter> counters,
            SortedMap<String, Histogram> histograms, SortedMap<String, Meter> meters, SortedMap<String, Timer> timers) {
        metricReceiver.tell(new MetricUpdate(gauges, counters, histograms, meters, timers, new Date(clock.getTime())),
            null);
    }

    /**
     * Returns a new {@link Builder} for {@link AkkaReporter}.
     *
     * @param metricRegistry the registry to report
     * @param metricReceiver the Akka actor that will be receiving reported metrics
     * @return a {@link Builder} instance for a {@link AkkaReporter}
     */
    public static Builder forRegistryAndReceiver(MetricRegistry metricRegistry, ActorRef metricReceiver) {
        return new Builder(metricRegistry, metricReceiver);
    }

    public static class Builder {
        private final MetricRegistry registry;
        private ActorRef metricReceiver;
        private Locale locale;
        private Clock clock;
        private TimeZone timeZone;
        private TimeUnit rateUnit;
        private TimeUnit durationUnit;
        private MetricFilter filter;
        private ScheduledExecutorService executor;
        private boolean shutdownExecutorOnStop;
        private Set<MetricAttribute> disabledMetricAttributes;

        private Builder(MetricRegistry registry, ActorRef metricReceiver) {
            this.registry = registry;
            this.metricReceiver = metricReceiver;
            this.locale = Locale.getDefault();
            this.clock = Clock.defaultClock();
            this.timeZone = TimeZone.getDefault();
            this.rateUnit = TimeUnit.SECONDS;
            this.durationUnit = TimeUnit.MILLISECONDS;
            this.filter = MetricFilter.ALL;
            this.executor = null;
            this.shutdownExecutorOnStop = true;
            this.disabledMetricAttributes = Collections.emptySet();
        }

        public AkkaReporter build() {
            return new AkkaReporter(
                registry,
                    metricReceiver,
                locale,
                clock,
                timeZone,
                rateUnit,
                durationUnit,
                filter,
                executor,
                shutdownExecutorOnStop,
                disabledMetricAttributes
            );
        }

        public Builder shutdownExecutorOnStop(boolean shutdownExecutorOnStop) {
            this.shutdownExecutorOnStop = shutdownExecutorOnStop;
            return this;
        }

        public Builder scheduleOn(ScheduledExecutorService executor) {
            this.executor = executor;
            return this;
        }

        public Builder formattedFor(Locale locale) {
            this.locale = locale;
            return this;
        }

        public Builder withClock(Clock clock) {
            this.clock = clock;
            return this;
        }

        public Builder formattedFor(TimeZone timeZone) {
            this.timeZone = timeZone;
            return this;
        }

        public Builder convertRatesTo(TimeUnit rateUnit) {
            this.rateUnit = rateUnit;
            return this;
        }

        public Builder convertDurationsTo(TimeUnit durationUnit) {
            this.durationUnit = durationUnit;
            return this;
        }

        public Builder filter(MetricFilter filter) {
            this.filter = filter;
            return this;
        }

        public Builder disabledMetricAttributes(Set<MetricAttribute> disabledMetricAttributes) {
            this.disabledMetricAttributes = disabledMetricAttributes;
            return this;
        }
    }
}
