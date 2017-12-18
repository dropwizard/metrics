package com.codahale.metrics.collectd;

import com.codahale.metrics.Clock;
import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Metered;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

/**
 * A reporter which publishes metric values to a Collectd server.
 *
 * @see <a href="https://collectd.org">collectd – The system statistics
 *      collection daemon</a>
 */
public class CollectdReporter extends ScheduledReporter {

    /**
     * Returns a builder for the specified registry.
     *
     * The default settings are:
     * <ul>
     *     <li>hostName: InetAddress.getLocalHost().getHostName()</li>
     *     <li>clock: Clock.defaultClock()</li>
     *     <li>rateUnit: TimeUnit.SECONDS</li>
     *     <li>durationUnit: TimeUnit.MILLISECONDS</li>
     *     <li>filter: MetricFilter.ALL</li>
     * </ul>
     */
    public static Builder forRegistry(MetricRegistry registry) {
        return new Builder(registry);
    }

    public static class Builder {

        private final MetricRegistry registry;
        private String hostName;
        private Clock clock = Clock.defaultClock();
        private TimeUnit rateUnit = TimeUnit.SECONDS;
        private TimeUnit durationUnit = TimeUnit.MILLISECONDS;
        private MetricFilter filter = MetricFilter.ALL;

        private Builder(MetricRegistry registry) {
            this.registry = registry;
        }

        public Builder withHostName(String hostName) {
            this.hostName = hostName;
            return this;
        }

        public Builder withClock(Clock clock) {
            this.clock = clock;
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

        public CollectdReporter build(Collectd collectd) {
            return new CollectdReporter(
                    registry, hostName, collectd, clock, rateUnit, durationUnit, filter);
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(CollectdReporter.class);
    private static final String REPORTER_NAME = "collectd-reporter";
    private static final String FALLBACK_HOST_NAME = "localhost";

    private String hostName;
    private final Collectd collectd;
    private final Clock clock;
    private long period;

    private CollectdReporter(
            MetricRegistry registry,
            String hostname,
            Collectd collectd,
            Clock clock,
            TimeUnit rateUnit,
            TimeUnit durationUnit,
            MetricFilter filter) {
        super(registry, REPORTER_NAME, filter, rateUnit, durationUnit);
        this.hostName = (hostname != null) ? hostname : resolveHostName();
        this.collectd = collectd;
        this.clock = clock;
    }

    private String resolveHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            LOG.error("Failed to lookup local host name: {}", e.getMessage(), e);
            return FALLBACK_HOST_NAME;
        }
    }

    @Override
    public void start(long period, TimeUnit unit) {
        this.period = period;
        super.start(period, unit);
    }

    @Override
    public void report(
            SortedMap<String, Gauge> gauges,
            SortedMap<String, Counter> counters,
            SortedMap<String, Histogram> histograms,
            SortedMap<String, Meter> meters,
            SortedMap<String, Timer> timers) {
        final long timestamp = clock.getTime() / 1000;
        try {
            if (!collectd.isConnected()) {
                collectd.connect();
            }
            for (Map.Entry<String, Gauge> entry : gauges.entrySet()) {
                reportGauge(entry.getKey(), entry.getValue(), timestamp);
            }
            for (Map.Entry<String, Counter> entry : counters.entrySet()) {
                reportCounter(entry.getKey(), entry.getValue(), timestamp);
            }
            for (Map.Entry<String, Histogram> entry : histograms.entrySet()) {
                reportHistogram(entry.getKey(), entry.getValue(), timestamp);
            }
            for (Map.Entry<String, Meter> entry : meters.entrySet()) {
                reportMetered(entry.getKey(), entry.getValue(), timestamp);
            }
            for (Map.Entry<String, Timer> entry : timers.entrySet()) {
                reportTimer(entry.getKey(), entry.getValue(), timestamp);
            }
        } catch (IOException e) {
            LOG.warn("Unable to report to Collectd", e);
        } finally {
            disconnect(collectd);
        }
    }

    private void reportTimer(String name, Timer timer, long timestamp) {
        final Snapshot snapshot = timer.getSnapshot();
        Identifier.Builder id = MetricNameMapping.createIdentifier(hostName, name);
        collectd.send(id.typeInstance("max").get(), convertDuration(snapshot.getMax()), timestamp, DataType.GAUGE, period);
        collectd.send(id.typeInstance("mean").get(), convertDuration(snapshot.getMean()), timestamp, DataType.GAUGE, period);
        collectd.send(id.typeInstance("min").get(), convertDuration(snapshot.getMin()), timestamp, DataType.GAUGE, period);
        collectd.send(id.typeInstance("stddev").get(), convertDuration(snapshot.getStdDev()), timestamp, DataType.GAUGE, period);
        collectd.send(id.typeInstance("p50").get(), convertDuration(snapshot.getMedian()), timestamp, DataType.GAUGE, period);
        collectd.send(id.typeInstance("p75").get(), convertDuration(snapshot.get75thPercentile()), timestamp, DataType.GAUGE, period);
        collectd.send(id.typeInstance("p95").get(), convertDuration(snapshot.get95thPercentile()), timestamp, DataType.GAUGE, period);
        collectd.send(id.typeInstance("p98").get(), convertDuration(snapshot.get98thPercentile()), timestamp, DataType.GAUGE, period);
        collectd.send(id.typeInstance("p99").get(), convertDuration(snapshot.get99thPercentile()), timestamp, DataType.GAUGE, period);
        collectd.send(id.typeInstance("p999").get(), convertDuration(snapshot.get999thPercentile()), timestamp, DataType.GAUGE, period);
        reportMetered(name, timer, timestamp);
    }

    private void reportMetered(String name, Metered meter, long timestamp) {
        Identifier.Builder id = MetricNameMapping.createIdentifier(hostName, name);
        collectd.send(id.typeInstance("count").get(), meter.getCount(), timestamp, DataType.GAUGE, period);
        collectd.send(id.typeInstance("m1_rate").get(), convertRate(meter.getOneMinuteRate()), timestamp, DataType.GAUGE, period);
        collectd.send(id.typeInstance("m5_rate").get(), convertRate(meter.getFiveMinuteRate()), timestamp, DataType.GAUGE, period);
        collectd.send(id.typeInstance("m15_rate").get(), convertRate(meter.getFifteenMinuteRate()), timestamp, DataType.GAUGE, period);
        collectd.send(id.typeInstance("mean_rate").get(), convertRate(meter.getMeanRate()), timestamp, DataType.GAUGE, period);
    }

    private void reportHistogram(String name, Histogram histogram, long timestamp) {
        final Snapshot snapshot = histogram.getSnapshot();
        Identifier.Builder id = MetricNameMapping.createIdentifier(hostName, name);
        collectd.send(id.typeInstance("count").get(), histogram.getCount(), timestamp, DataType.GAUGE, period);
        collectd.send(id.typeInstance("max").get(), snapshot.getMax(), timestamp, DataType.GAUGE, period);
        collectd.send(id.typeInstance("mean").get(), snapshot.getMean(), timestamp, DataType.GAUGE, period);
        collectd.send(id.typeInstance("min").get(), snapshot.getMin(), timestamp, DataType.GAUGE, period);
        collectd.send(id.typeInstance("stddev").get(), snapshot.getStdDev(), timestamp, DataType.GAUGE, period);
        collectd.send(id.typeInstance("p50").get(), snapshot.getMedian(), timestamp, DataType.GAUGE, period);
        collectd.send(id.typeInstance("p75").get(), snapshot.get75thPercentile(), timestamp, DataType.GAUGE, period);
        collectd.send(id.typeInstance("p95").get(), snapshot.get95thPercentile(), timestamp, DataType.GAUGE, period);
        collectd.send(id.typeInstance("p98").get(), snapshot.get98thPercentile(), timestamp, DataType.GAUGE, period);
        collectd.send(id.typeInstance("p99").get(), snapshot.get99thPercentile(), timestamp, DataType.GAUGE, period);
        collectd.send(id.typeInstance("p999").get(), snapshot.get999thPercentile(), timestamp, DataType.GAUGE, period);
    }

    private void reportCounter(String name, Counter value, long timestamp) {
        Identifier.Builder id = MetricNameMapping.createIdentifier(hostName, name);
        collectd.send(id.get(), value.getCount(), timestamp, DataType.COUNTER, period);
    }

    private void reportGauge(String name, Gauge<?> value, long timestamp) {
        Object object = value.getValue();
        if (object instanceof Number) {
            LOG.debug("Dropping metric '{}', which provides a value of unsupported type {}.",
                    name, object.getClass());
        }
        Identifier.Builder id = MetricNameMapping.createIdentifier(hostName, name);
        collectd.send(id.get(), ((Number) object).doubleValue(), timestamp, DataType.GAUGE, period);
    }

    private void disconnect(Collectd collectd) {
        try {
            collectd.disconnect();
        } catch (Exception e) {
            LOG.debug("Error disconnecting from Collectd: " + e.getMessage(), e);
        }
    }

}
