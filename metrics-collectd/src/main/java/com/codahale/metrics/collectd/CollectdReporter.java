package com.codahale.metrics.collectd;

import com.codahale.metrics.Clock;
import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Metric;
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
 * collection daemon</a>
 */
public class CollectdReporter extends ScheduledReporter {

    /**
     * Returns a builder for the specified registry.
     * <p>
     * The default settings are:
     * <ul>
     * <li>hostName: InetAddress.getLocalHost().getHostName()</li>
     * <li>clock: Clock.defaultClock()</li>
     * <li>rateUnit: TimeUnit.SECONDS</li>
     * <li>durationUnit: TimeUnit.MILLISECONDS</li>
     * <li>filter: MetricFilter.ALL</li>
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

        public CollectdReporter build(Sender sender) {
            return new CollectdReporter(
                    registry, hostName, sender, clock, rateUnit, durationUnit, filter);
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

    private Serializer<Gauge> gaugeSerializer;
    private Serializer<Counter> counterSerializer;
    private Serializer<Histogram> histogramSerializer;
    private Serializer<Meter> meterSerializer;
    private Serializer<Timer> timerSerializer;

    private CollectdReporter(
            MetricRegistry registry,
            String hostname,
            Sender sender,
            Clock clock,
            TimeUnit rateUnit,
            TimeUnit durationUnit,
            MetricFilter filter) {
        super(registry, REPORTER_NAME, filter, rateUnit, durationUnit);
        this.hostName = (hostname != null) ? hostname : resolveHostName();
        this.sender = sender;
        this.clock = clock;
        createSerializers(new PacketWriter(sender));
    }

    private void createSerializers(PacketWriter writer) {
        gaugeSerializer = new GaugeSerializer(writer);
        counterSerializer = new CounterSerializer(writer);
        histogramSerializer = new HistogramSerializer(writer);
        meterSerializer = new MeterSerializer(writer);
        timerSerializer = new TimerSerializer(writer);
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
        final MetaData.Builder metaData = createMetaDataBuilder();
        try {
            connect(sender);
            report(gauges, metaData, gaugeSerializer);
            report(counters, metaData, counterSerializer);
            report(histograms, metaData, histogramSerializer);
            report(meters, metaData, meterSerializer);
            report(timers, metaData, timerSerializer);
        } catch (IOException e) {
            LOG.warn("Unable to report to Collectd", e);
        } finally {
            disconnect(sender);
        }
    }

    private MetaData.Builder createMetaDataBuilder() {
        final long timestamp = clock.getTime() / 1000;
        return new MetaData.Builder(hostName, timestamp, period).type(COLLECTD_TYPE_GAUGE);
    }

    private void connect(Sender sender) throws IOException {
        if (!sender.isConnected()) {
            sender.connect();
        }
    }

    private <M extends Metric> void report(Map<String, M> metrics, MetaData.Builder metaData,
                                           Serializer<M> serializer) {
        for (Map.Entry<String, M> entry : metrics.entrySet()) {
            serializer.serialize(metaData.plugin(entry.getKey()), entry.getValue());
        }
    }

    private void disconnect(Sender sender) {
        try {
            sender.disconnect();
        } catch (Exception e) {
            LOG.warn("Error disconnecting from Collectd: " + e.getMessage(), e);
        }
    }

    abstract class Serializer<M extends Metric> {

        final Logger log = LoggerFactory.getLogger(getClass());
        final PacketWriter writer;

        Serializer(PacketWriter writer) {
            this.writer = writer;
        }

        protected abstract void serialize(MetaData.Builder metaData, M metric);

        final void write(MetaData metaData, Number... values) {
            try {
                writer.write(metaData, values);
            } catch (RuntimeException e) {
                log.warn("Failed to process metric '" + metaData.getPlugin() + "': " + e.getMessage());
            } catch (IOException e) {
                log.error("Failed to send metric to collectd: " + e.getMessage(), e);
            }
        }
    }

    class GaugeSerializer extends Serializer<Gauge> {

        GaugeSerializer(PacketWriter writer) {
            super(writer);
        }

        @Override
        public void serialize(MetaData.Builder metaData, Gauge metric) {
            try {
                write(metaData.typeInstance("value").get(), getValue(metric));
            } catch (IllegalArgumentException e) {
                log.warn("Failed to process metric '" + metaData.get().getPlugin() + "': " + e.getMessage());
            }
        }

        private Number getValue(Gauge<?> gauge) throws IllegalArgumentException {
            Object value = gauge.getValue();
            if (value instanceof Number) {
                return (Number) value;
            } else if (value instanceof Boolean) {
                return (boolean) value ? 1 : 0;
            }
            throw new IllegalArgumentException(
                    "Unsupported gauge of type " + value.getClass().getName());
        }
    }

    class MeterSerializer extends Serializer<Meter> {

        MeterSerializer(PacketWriter writer) {
            super(writer);
        }

        @Override
        public void serialize(MetaData.Builder metaData, Meter metric) {
            write(metaData.typeInstance("count").get(), (double) metric.getCount());
            write(metaData.typeInstance("m1_rate").get(), convertRate(metric.getOneMinuteRate()));
            write(metaData.typeInstance("m5_rate").get(), convertRate(metric.getFiveMinuteRate()));
            write(metaData.typeInstance("m15_rate").get(), convertRate(metric.getFifteenMinuteRate()));
            write(metaData.typeInstance("mean_rate").get(), convertRate(metric.getMeanRate()));
        }
    }

    class CounterSerializer extends Serializer<Counter> {

        CounterSerializer(PacketWriter writer) {
            super(writer);
        }

        @Override
        public void serialize(MetaData.Builder metaData, Counter metric) {
            write(metaData.typeInstance("count").get(), (double) metric.getCount());
        }
    }

    class HistogramSerializer extends CollectdReporter.Serializer<Histogram> {

        HistogramSerializer(PacketWriter writer) {
            super(writer);
        }

        @Override
        public void serialize(MetaData.Builder metaData, Histogram metric) {
            final Snapshot snapshot = metric.getSnapshot();
            write(metaData.typeInstance("count").get(), (double) metric.getCount());
            write(metaData.typeInstance("max").get(), (double) snapshot.getMax());
            write(metaData.typeInstance("mean").get(), snapshot.getMean());
            write(metaData.typeInstance("min").get(), (double) snapshot.getMin());
            write(metaData.typeInstance("stddev").get(), snapshot.getStdDev());
            write(metaData.typeInstance("p50").get(), snapshot.getMedian());
            write(metaData.typeInstance("p75").get(), snapshot.get75thPercentile());
            write(metaData.typeInstance("p95").get(), snapshot.get95thPercentile());
            write(metaData.typeInstance("p98").get(), snapshot.get98thPercentile());
            write(metaData.typeInstance("p99").get(), snapshot.get99thPercentile());
            write(metaData.typeInstance("p999").get(), snapshot.get999thPercentile());
        }
    }

    class TimerSerializer extends Serializer<Timer> {

        TimerSerializer(PacketWriter writer) {
            super(writer);
        }

        @Override
        public void serialize(MetaData.Builder metaData, Timer metric) {
            final Snapshot snapshot = metric.getSnapshot();
            write(metaData.typeInstance("count").get(), (double) metric.getCount());
            write(metaData.typeInstance("max").get(), convertDuration(snapshot.getMax()));
            write(metaData.typeInstance("mean").get(), convertDuration(snapshot.getMean()));
            write(metaData.typeInstance("min").get(), convertDuration(snapshot.getMin()));
            write(metaData.typeInstance("stddev").get(), convertDuration(snapshot.getStdDev()));
            write(metaData.typeInstance("p50").get(), convertDuration(snapshot.getMedian()));
            write(metaData.typeInstance("p75").get(), convertDuration(snapshot.get75thPercentile()));
            write(metaData.typeInstance("p95").get(), convertDuration(snapshot.get95thPercentile()));
            write(metaData.typeInstance("p98").get(), convertDuration(snapshot.get98thPercentile()));
            write(metaData.typeInstance("p99").get(), convertDuration(snapshot.get99thPercentile()));
            write(metaData.typeInstance("p999").get(), convertDuration(snapshot.get999thPercentile()));
            write(metaData.typeInstance("m1_rate").get(), convertRate(metric.getOneMinuteRate()));
            write(metaData.typeInstance("m5_rate").get(), convertRate(metric.getFiveMinuteRate()));
            write(metaData.typeInstance("m15_rate").get(), convertRate(metric.getFifteenMinuteRate()));
            write(metaData.typeInstance("mean_rate").get(), convertRate(metric.getMeanRate()));
        }
    }
}
