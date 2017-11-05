package com.codahale.metrics.akka;

import akka.actor.ActorRef;
import com.codahale.metrics.*;
import com.codahale.metrics.Timer;

import java.text.DateFormat;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A {@link ScheduledReporter} subclass that periodically pumps collected metrics to an Akka actor.
 */
public class AkkaReporter extends ScheduledReporter {
    private ActorRef metricsReceiver;
    private final Clock clock;

    private AkkaReporter(MetricRegistry registry,
                          ActorRef metricsReceiver,
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

        this.metricsReceiver = metricsReceiver;
        this.clock = clock;
        DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM, locale);

        dateFormat.setTimeZone(timeZone);
    }

    @Override
    public void report(SortedMap<String, Gauge> gauges, SortedMap<String, Counter> counters,
            SortedMap<String, Histogram> histograms, SortedMap<String, Meter> meters, SortedMap<String, Timer> timers) {
        Map<String,Metric> allMetrics =
            Stream.of(gauges.entrySet(), counters.entrySet(), histograms.entrySet(), meters.entrySet(), timers.entrySet())
                .flatMap(Set::stream)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        MetricsUpdate update = new MetricsUpdate(allMetrics, new Date(clock.getTime()));

        metricsReceiver.tell(update, null);
    }

    public static Builder withBuilder(MetricRegistry registry, ActorRef metricsReceiver) {
        return new Builder(registry, metricsReceiver);
    }

    public static class Builder {
        private final MetricRegistry registry;
        private ActorRef metricsReceiver;
        private Locale locale;
        private Clock clock;
        private TimeZone timeZone;
        private TimeUnit rateUnit;
        private TimeUnit durationUnit;
        private MetricFilter filter;
        private ScheduledExecutorService executor;
        private boolean shutdownExecutorOnStop;
        private Set<MetricAttribute> disabledMetricAttributes;

        private Builder(MetricRegistry registry, ActorRef metricsReceiver) {
            this.registry = registry;
            this.metricsReceiver = metricsReceiver;
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
                metricsReceiver,
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
