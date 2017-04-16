package com.codahale.metrics.cloudwatch;

import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.codahale.metrics.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

/**
 * A reporter which publishes metric values to CloudWatch.
 *
 */
public class CloudWatchReporter extends ScheduledReporter {
    /**
     * Returns a new {@link Builder} for {@link CloudWatchReporter}.
     *
     * @param registry the registry to report
     * @return a {@link Builder} instance for a {@link CloudWatchReporter}
     */
    public static Builder forRegistry(String nameSpace, MetricRegistry registry) {
        return new Builder(nameSpace, registry);
    }

    /**
     * A builder for {@link CloudWatchReporter} instances. Defaults to not using a prefix, using the
     * default clock, converting rates to events/second, converting durations to milliseconds, and
     * not filtering metrics.
     */
    public static class Builder {
        private final String nameSpace;
        private final MetricRegistry registry;
        private Clock clock;
        private String prefix;
        private TimeUnit rateUnit;
        private TimeUnit durationUnit;
        private MetricFilter filter;

        private Builder(String nameSpace, MetricRegistry registry) {
            this.nameSpace = nameSpace;
            this.registry = registry;
            this.clock = Clock.defaultClock();
            this.prefix = null;
            this.rateUnit = TimeUnit.SECONDS;
            this.durationUnit = TimeUnit.MILLISECONDS;
            this.filter = MetricFilter.ALL;
        }

        /**
         * Use the given {@link Clock} instance for the time.
         *
         * @param clock a {@link Clock} instance
         * @return {@code this}
         */
        public Builder withClock(Clock clock) {
            this.clock = clock;
            return this;
        }

        /**
         * Prefix all metric names with the given string.
         *
         * @param prefix the prefix for all metric names
         * @return {@code this}
         */
        public Builder prefixedWith(String prefix) {
            this.prefix = prefix;
            return this;
        }

        /**
         * Convert rates to the given time unit.
         *
         * @param rateUnit a unit of time
         * @return {@code this}
         */
        public Builder convertRatesTo(TimeUnit rateUnit) {
            this.rateUnit = rateUnit;
            return this;
        }

        /**
         * Convert durations to the given time unit.
         *
         * @param durationUnit a unit of time
         * @return {@code this}
         */
        public Builder convertDurationsTo(TimeUnit durationUnit) {
            this.durationUnit = durationUnit;
            return this;
        }

        /**
         * Only report metrics which match the given filter.
         *
         * @param filter a {@link MetricFilter}
         * @return {@code this}
         */
        public Builder filter(MetricFilter filter) {
            this.filter = filter;
            return this;
        }

        /**
         * Builds a {@link CloudWatchReporter} with the given properties, sending metrics using the
         * given {@link com.amazonaws.services.cloudwatch.AmazonCloudWatchClient} client.
         *
         * @param cloudwatch a {@link com.amazonaws.services.cloudwatch.AmazonCloudWatchClient} client
         * @return a {@link CloudWatchReporter}
         */
        public CloudWatchReporter build(AmazonCloudWatchClient cloudwatch) {
            return new CloudWatchReporter(
                    nameSpace,
                    registry,
                    cloudwatch,
                    clock,
                    prefix,
                    rateUnit,
                    durationUnit,
                    filter);
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(CloudWatchReporter.class);


    private final String nameSpace;
    private final AmazonCloudWatchClient cloudwatch;
    private final Clock clock;
    private final String prefix;

    private CloudWatchReporter(
                             String nameSpace,
                             MetricRegistry registry,
                             AmazonCloudWatchClient cloudwatch,
                             Clock clock,
                             String prefix,
                             TimeUnit rateUnit,
                             TimeUnit durationUnit,
                             MetricFilter filter) {
        super(registry, "cloudwatch-reporter", filter, rateUnit, durationUnit);
        this.nameSpace = nameSpace;
        this.cloudwatch = cloudwatch;
        this.clock = clock;
        this.prefix = prefix;
    }

    @Override
    public void report(SortedMap<String, Gauge> gauges,
                       SortedMap<String, Counter> counters,
                       SortedMap<String, Histogram> histograms,
                       SortedMap<String, Meter> meters,
                       SortedMap<String, Timer> timers) {

            CloudWatchSendTask packet = new CloudWatchSendTask(nameSpace, cloudwatch);

            for (Map.Entry<String, Gauge> entry : gauges.entrySet()) {
                reportGauge(packet, entry.getKey(), entry.getValue());
            }

            for (Map.Entry<String, Counter> entry : counters.entrySet()) {
                reportCounter(packet, entry.getKey(), entry.getValue());
            }

            for (Map.Entry<String, Histogram> entry : histograms.entrySet()) {
                reportHistogram(packet, entry.getKey(), entry.getValue());
            }

            for (Map.Entry<String, Meter> entry : meters.entrySet()) {
                reportMetered(packet, entry.getKey(), entry.getValue());
            }

            for (Map.Entry<String, Timer> entry : timers.entrySet()) {
                reportTimer(packet, entry.getKey(), entry.getValue());
            }

        try {
            packet.send();
        }catch (RuntimeException e){
            // Do not kill the reporting thread due to a RuntimeException
            LOGGER.warn("RuntimeException while reporting metrics", e);
        }
    }

    private void reportTimer(CloudWatchSendTask packet, String name, Timer timer) {
        final Snapshot snapshot = timer.getSnapshot();

        packet.add(prefix(name, "max"), format(convertDuration(snapshot.getMax())));
        packet.add(prefix(name, "mean"), format(convertDuration(snapshot.getMean())));
        packet.add(prefix(name, "min"), format(convertDuration(snapshot.getMin())));
        packet.add(prefix(name, "stddev"),
                format(convertDuration(snapshot.getStdDev())));
        packet.add(prefix(name, "p50"),
                format(convertDuration(snapshot.getMedian())));
        packet.add(prefix(name, "p75"),
                format(convertDuration(snapshot.get75thPercentile())));
        packet.add(prefix(name, "p95"),
                format(convertDuration(snapshot.get95thPercentile())));
        packet.add(prefix(name, "p98"),
                format(convertDuration(snapshot.get98thPercentile())));
        packet.add(prefix(name, "p99"),
                format(convertDuration(snapshot.get99thPercentile())));
        packet.add(prefix(name, "p999"),
                format(convertDuration(snapshot.get999thPercentile())));

        reportMetered(packet, name, timer);
    }

    private void reportMetered(CloudWatchSendTask packet, String name, Metered meter) {
        packet.add(prefix(name, "count"), format(meter.getCount()));
        packet.add(prefix(name, "m1_rate"),
                format(convertRate(meter.getOneMinuteRate())));
        packet.add(prefix(name, "m5_rate"),
                format(convertRate(meter.getFiveMinuteRate())));
        packet.add(prefix(name, "m15_rate"),
                format(convertRate(meter.getFifteenMinuteRate())));
        packet.add(prefix(name, "mean_rate"),
                format(convertRate(meter.getMeanRate())));
    }

    private void reportHistogram(CloudWatchSendTask packet, String name, Histogram histogram) {
        final Snapshot snapshot = histogram.getSnapshot();
        packet.add(prefix(name, "count"), format(histogram.getCount()));
        packet.add(prefix(name, "max"), format(snapshot.getMax()));
        packet.add(prefix(name, "mean"), format(snapshot.getMean()));
        packet.add(prefix(name, "min"), format(snapshot.getMin()));
        packet.add(prefix(name, "stddev"), format(snapshot.getStdDev()));
        packet.add(prefix(name, "p50"), format(snapshot.getMedian()));
        packet.add(prefix(name, "p75"), format(snapshot.get75thPercentile()));
        packet.add(prefix(name, "p95"), format(snapshot.get95thPercentile()));
        packet.add(prefix(name, "p98"), format(snapshot.get98thPercentile()));
        packet.add(prefix(name, "p99"), format(snapshot.get99thPercentile()));
        packet.add(prefix(name, "p999"), format(snapshot.get999thPercentile()));
    }

    private void reportCounter(CloudWatchSendTask packet, String name, Counter counter) {
        packet.add(prefix(name, "count"), format(counter.getCount()));
    }

    private void reportGauge(CloudWatchSendTask packet, String name, Gauge gauge) {
        final Double value = format(gauge.getValue());
        if (value != null) {
            packet.add(prefix(name), value);
        }
    }

    private Double format(Object o) {
        if(o instanceof Number){
            return ((Number)o).doubleValue();
        }else{
            return null;
        }
    }

    private String prefix(String... components) {
        return MetricRegistry.name(prefix, components);
    }

}
