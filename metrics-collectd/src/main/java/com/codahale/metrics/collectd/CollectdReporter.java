package com.codahale.metrics.collectd;

import com.codahale.metrics.Clock;
import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricAttribute;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.codahale.metrics.MetricAttribute.COUNT;
import static com.codahale.metrics.MetricAttribute.M15_RATE;
import static com.codahale.metrics.MetricAttribute.M1_RATE;
import static com.codahale.metrics.MetricAttribute.M5_RATE;
import static com.codahale.metrics.MetricAttribute.MAX;
import static com.codahale.metrics.MetricAttribute.MEAN;
import static com.codahale.metrics.MetricAttribute.MEAN_RATE;
import static com.codahale.metrics.MetricAttribute.MIN;
import static com.codahale.metrics.MetricAttribute.P50;
import static com.codahale.metrics.MetricAttribute.P75;
import static com.codahale.metrics.MetricAttribute.P95;
import static com.codahale.metrics.MetricAttribute.P98;
import static com.codahale.metrics.MetricAttribute.P99;
import static com.codahale.metrics.MetricAttribute.P999;
import static com.codahale.metrics.MetricAttribute.STDDEV;

/**
 * A reporter which publishes metric values to a Collectd server.
 *
 * @see <a href="https://collectd.org">collectd – The system statistics
 * collection daemon</a>
 */
public class CollectdReporter extends ScheduledReporter {

    /**
     * Returns a builder for the specified registry.
     * <p>
     * The default settings are:
     * <ul>
     * <li>hostName: InetAddress.getLocalHost().getHostName()</li>
     * <li>executor: default executor created by {@code ScheduledReporter}</li>
     * <li>shutdownExecutorOnStop: true</li>
     * <li>clock: Clock.defaultClock()</li>
     * <li>rateUnit: TimeUnit.SECONDS</li>
     * <li>durationUnit: TimeUnit.MILLISECONDS</li>
     * <li>filter: MetricFilter.ALL</li>
     * <li>securityLevel: NONE</li>
     * <li>username: ""</li>
     * <li>password: ""</li>
     * </ul>
     */
    public static Builder forRegistry(MetricRegistry registry) {
        return new Builder(registry);
    }

    public static class Builder {

        private final MetricRegistry registry;
        private String hostName;
        private ScheduledExecutorService executor;
        private boolean shutdownExecutorOnStop = true;
        private Clock clock = Clock.defaultClock();
        private TimeUnit rateUnit = TimeUnit.SECONDS;
        private TimeUnit durationUnit = TimeUnit.MILLISECONDS;
        private MetricFilter filter = MetricFilter.ALL;
        private SecurityLevel securityLevel = SecurityLevel.NONE;
        private String username = "";
        private String password = "";
        private Set<MetricAttribute> disabledMetricAttributes = Collections.emptySet();
        private int maxLength = Sanitize.DEFAULT_MAX_LENGTH;

        private Builder(MetricRegistry registry) {
            this.registry = registry;
        }

        public Builder withHostName(String hostName) {
            this.hostName = hostName;
            return this;
        }

        public Builder shutdownExecutorOnStop(boolean shutdownExecutorOnStop) {
            this.shutdownExecutorOnStop = shutdownExecutorOnStop;
            return this;
        }

        public Builder scheduleOn(ScheduledExecutorService executor) {
            this.executor = executor;
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

        public Builder withUsername(String username) {
            this.username = username;
            return this;
        }

        public Builder withPassword(String password) {
            this.password = password;
            return this;
        }

        public Builder withSecurityLevel(SecurityLevel securityLevel) {
            this.securityLevel = securityLevel;
            return this;
        }

        public Builder disabledMetricAttributes(Set<MetricAttribute> attributes) {
            this.disabledMetricAttributes = attributes;
            return this;
        }

        public Builder withMaxLength(int maxLength) {
            this.maxLength = maxLength;
            return this;
        }

        public CollectdReporter build(Sender sender) {
            if (securityLevel != SecurityLevel.NONE) {
                if (username.isEmpty()) {
                    throw new IllegalArgumentException("username is required for securityLevel: " + securityLevel);
                }
                if (password.isEmpty()) {
                    throw new IllegalArgumentException("password is required for securityLevel: " + securityLevel);
                }
            }
            return new CollectdReporter(registry,
                    hostName, sender,
                    executor, shutdownExecutorOnStop,
                    clock, rateUnit, durationUnit,
                    filter, disabledMetricAttributes,
                    username, password, securityLevel, new Sanitize(maxLength));
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(CollectdReporter.class);
    private static final String REPORTER_NAME = "collectd-reporter";
    private static final String FALLBACK_HOST_NAME = "localhost";
    private static final String COLLECTD_TYPE_GAUGE = "gauge";

    private String hostName;
    private final Sender sender;
    private final Clock clock;
    private long period;
    private final PacketWriter writer;
    private final Sanitize sanitize;

    private CollectdReporter(MetricRegistry registry,
                             String hostname, Sender sender,
                             ScheduledExecutorService executor, boolean shutdownExecutorOnStop,
                             Clock clock, TimeUnit rateUnit, TimeUnit durationUnit,
                             MetricFilter filter, Set<MetricAttribute> disabledMetricAttributes,
                             String username, String password,
                             SecurityLevel securityLevel, Sanitize sanitize) {
        super(registry, REPORTER_NAME, filter, rateUnit, durationUnit, executor, shutdownExecutorOnStop,
                disabledMetricAttributes);
        this.hostName = (hostname != null) ? hostname : resolveHostName();
        this.sender = sender;
        this.clock = clock;
        this.sanitize = sanitize;
        writer = new PacketWriter(sender, username, password, securityLevel);
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
    @SuppressWarnings("rawtypes")
    public void report(SortedMap<String, Gauge> gauges, SortedMap<String, Counter> counters,
            SortedMap<String, Histogram> histograms, SortedMap<String, Meter> meters, SortedMap<String, Timer> timers) {
        MetaData.Builder metaData = new MetaData.Builder(sanitize, hostName, clock.getTime() / 1000, period)
                .type(COLLECTD_TYPE_GAUGE);
        try {
            connect(sender);
            for (Map.Entry<String, Gauge> entry : gauges.entrySet()) {
                serializeGauge(metaData.plugin(entry.getKey()), entry.getValue());
            }
            for (Map.Entry<String, Counter> entry : counters.entrySet()) {
                serializeCounter(metaData.plugin(entry.getKey()), entry.getValue());
            }
            for (Map.Entry<String, Histogram> entry : histograms.entrySet()) {
                serializeHistogram(metaData.plugin(entry.getKey()), entry.getValue());
            }
            for (Map.Entry<String, Meter> entry : meters.entrySet()) {
                serializeMeter(metaData.plugin(entry.getKey()), entry.getValue());
            }
            for (Map.Entry<String, Timer> entry : timers.entrySet()) {
                serializeTimer(metaData.plugin(entry.getKey()), entry.getValue());
            }
        } catch (IOException e) {
            LOG.warn("Unable to report to Collectd", e);
        } finally {
            disconnect(sender);
        }
    }

    private void connect(Sender sender) throws IOException {
        if (!sender.isConnected()) {
            sender.connect();
        }
    }

    private void disconnect(Sender sender) {
        try {
            sender.disconnect();
        } catch (Exception e) {
            LOG.warn("Error disconnecting from Collectd", e);
        }
    }

    private void writeValue(MetaData.Builder metaData, MetricAttribute attribute, Number value) {
        if (!getDisabledMetricAttributes().contains(attribute)) {
            write(metaData.typeInstance(attribute.getCode()).get(), value);
        }
    }

    private void writeRate(MetaData.Builder metaData, MetricAttribute attribute, double rate) {
        writeValue(metaData, attribute, convertRate(rate));
    }

    private void writeDuration(MetaData.Builder metaData, MetricAttribute attribute, double duration) {
        writeValue(metaData, attribute, convertDuration(duration));
    }

    private void write(MetaData metaData, Number value) {
        try {
            writer.write(metaData, value);
        } catch (RuntimeException e) {
            LOG.warn("Failed to process metric '" + metaData.getPlugin() + "': " + e.getMessage());
        } catch (IOException e) {
            LOG.error("Failed to send metric to collectd", e);
        }
    }

    @SuppressWarnings("rawtypes")
    private void serializeGauge(MetaData.Builder metaData, Gauge metric) {
        if (metric.getValue() instanceof Number) {
            write(metaData.typeInstance("value").get(), (Number) metric.getValue());
        } else if (metric.getValue() instanceof Boolean) {
            write(metaData.typeInstance("value").get(), ((Boolean) metric.getValue()) ? 1 : 0);
        } else {
            LOG.warn("Failed to process metric '{}'. Unsupported gauge of type: {} ", metaData.get().getPlugin(),
                    metric.getValue().getClass().getName());
        }
    }

    private void serializeMeter(MetaData.Builder metaData, Meter metric) {
        writeValue(metaData, COUNT, (double) metric.getCount());
        writeRate(metaData, M1_RATE, metric.getOneMinuteRate());
        writeRate(metaData, M5_RATE, metric.getFiveMinuteRate());
        writeRate(metaData, M15_RATE, metric.getFifteenMinuteRate());
        writeRate(metaData, MEAN_RATE, metric.getMeanRate());
    }

    private void serializeCounter(MetaData.Builder metaData, Counter metric) {
        writeValue(metaData, COUNT, (double) metric.getCount());
    }

    private void serializeHistogram(MetaData.Builder metaData, Histogram metric) {
        final Snapshot snapshot = metric.getSnapshot();
        writeValue(metaData, COUNT, (double) metric.getCount());
        writeValue(metaData, MAX, (double) snapshot.getMax());
        writeValue(metaData, MEAN, snapshot.getMean());
        writeValue(metaData, MIN, (double) snapshot.getMin());
        writeValue(metaData, STDDEV, snapshot.getStdDev());
        writeValue(metaData, P50, snapshot.getMedian());
        writeValue(metaData, P75, snapshot.get75thPercentile());
        writeValue(metaData, P95, snapshot.get95thPercentile());
        writeValue(metaData, P98, snapshot.get98thPercentile());
        writeValue(metaData, P99, snapshot.get99thPercentile());
        writeValue(metaData, P999, snapshot.get999thPercentile());
    }

    private void serializeTimer(MetaData.Builder metaData, Timer metric) {
        final Snapshot snapshot = metric.getSnapshot();
        writeValue(metaData, COUNT, (double) metric.getCount());
        writeDuration(metaData, MAX, (double) snapshot.getMax());
        writeDuration(metaData, MEAN, snapshot.getMean());
        writeDuration(metaData, MIN, (double) snapshot.getMin());
        writeDuration(metaData, STDDEV, snapshot.getStdDev());
        writeDuration(metaData, P50, snapshot.getMedian());
        writeDuration(metaData, P75, snapshot.get75thPercentile());
        writeDuration(metaData, P95, snapshot.get95thPercentile());
        writeDuration(metaData, P98, snapshot.get98thPercentile());
        writeDuration(metaData, P99, snapshot.get99thPercentile());
        writeDuration(metaData, P999, snapshot.get999thPercentile());
        writeRate(metaData, M1_RATE, metric.getOneMinuteRate());
        writeRate(metaData, M5_RATE, metric.getFiveMinuteRate());
        writeRate(metaData, M15_RATE, metric.getFifteenMinuteRate());
        writeRate(metaData, MEAN_RATE, metric.getMeanRate());
    }
}
