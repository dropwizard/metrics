package io.dropwizard.metrics5.influxdb;

import io.dropwizard.metrics5.Clock;
import io.dropwizard.metrics5.Counter;
import io.dropwizard.metrics5.Gauge;
import io.dropwizard.metrics5.Histogram;
import io.dropwizard.metrics5.Meter;
import io.dropwizard.metrics5.Metered;
import io.dropwizard.metrics5.MetricAttribute;
import io.dropwizard.metrics5.MetricName;
import java.util.Map;

import static io.dropwizard.metrics5.MetricAttribute.*;
import io.dropwizard.metrics5.MetricFilter;
import io.dropwizard.metrics5.MetricRegistry;
import io.dropwizard.metrics5.Snapshot;
import io.dropwizard.metrics5.Timer;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.WeakHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A reporter which publishes metric values to InfluxDB.
 * 
 * <p>
 * Metrics are reported according to the 
 * <a href="https://docs.influxdata.com/influxdb/v1.4/write_protocols/line_protocol_reference/">InfluxDB Line Protocol</a>.
 * Brief line protocol syntax as follows:
 * <pre>
 * measurement(,tag_key=tag_val)* field_key=field_val(,field_key_n=field_value_n)* (nanoseconds-timestamp)?
 * </pre>
 * 
 * <p>
 * This InfluxDB reporter is "garbage free" in steady state.
 * This means objects and buffers are reused and no temporary objects are allocated as much as possible.
 */
public class InfluxDbReporter extends GarbageFreeScheduledReporter {

    /**
     * Returns a new Builder for {@link InfluxDbReporter}.
     *
     * @param registry the registry to report
     * @return a Builder instance for a {@link InfluxDbReporter}
     */
    public static Builder forRegistry(MetricRegistry registry) {
        return new Builder(registry);
    }

    /**
     * A builder for {@link InfluxDbReporter} instances. Defaults to not using a prefix, using the
     * default clock, converting rates to events/second, converting durations to milliseconds, and
     * not filtering metrics.
     */
    public static class Builder {
        private final MetricRegistry registry;
        private Clock clock;
        private MetricName prefix;
        private TimeUnit rateUnit;
        private TimeUnit durationUnit;
        private MetricFilter filter;
        private ScheduledExecutorService executor;
        private boolean shutdownExecutorOnStop;
        private Set<MetricAttribute> disabledMetricAttributes;

        private Builder(MetricRegistry registry) {
            this.registry = registry;
            this.clock = Clock.defaultClock();
            this.prefix = null;
            this.rateUnit = TimeUnit.SECONDS;
            this.durationUnit = TimeUnit.MILLISECONDS;
            this.filter = MetricFilter.ALL;
            this.executor = null;
            this.shutdownExecutorOnStop = true;
            this.disabledMetricAttributes = Collections.emptySet();
        }

        /**
         * Specifies whether or not, the executor (used for reporting) will be stopped with same time with reporter.
         * Default value is true.
         * Setting this parameter to false, has the sense in combining with providing external managed executor via 
         * {@link #scheduleOn(ScheduledExecutorService)}.
         *
         * @param shutdownExecutorOnStop if true, then executor will be stopped in same time with this reporter
         * @return {@code this}
         */
        public Builder shutdownExecutorOnStop(boolean shutdownExecutorOnStop) {
            this.shutdownExecutorOnStop = shutdownExecutorOnStop;
            return this;
        }

        /**
         * Specifies the executor to use while scheduling reporting of metrics.
         * Default value is null.
         * Null value leads to executor will be auto created on start.
         *
         * @param executor the executor to use while scheduling reporting of metrics.
         * @return {@code this}
         */
        public Builder scheduleOn(ScheduledExecutorService executor) {
            this.executor = executor;
            return this;
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
         * Prefix all metric names with the given name.
         *
         * @param prefix the prefix for all metric names
         * @return {@code this}
         */
        public Builder prefixedWith(MetricName prefix) {
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
         * Don't report the passed metric attributes for all metrics (e.g. "p999", "stddev" or "m15").
         *
         * @param disabledMetricAttributes the disabled metric attributes
         * @return {@code this}
         */
        public Builder disabledMetricAttributes(Set<MetricAttribute> disabledMetricAttributes) {
            this.disabledMetricAttributes = disabledMetricAttributes;
            return this;
        }

        /**
         * Builds a InfluxDbReporter with the given properties, sending metrics using the
         * given InfluxDbSender.
         *
         * @param sender the InfluxDbSender
         * @return the InfluxDbReporter
         */
        public InfluxDbReporter build(InfluxDbSender sender) {
            return new InfluxDbReporter(registry,
                    sender,
                    clock,
                    prefix,
                    rateUnit,
                    durationUnit,
                    filter,
                    executor,
                    shutdownExecutorOnStop,
                    disabledMetricAttributes);
        }
    }
    
    private static final Logger LOGGER = LoggerFactory.getLogger(InfluxDbReporter.class);
    private static final String VALUE = "value";
    
    private final Clock clock;
    private final MetricName prefix;
    private final InfluxDbSender sender;

    private final WeakHashMap<MetricName, char[]> encodedNameCache = new WeakHashMap<>();
    private final StringBuilder str = new StringBuilder();
    private boolean firstField;
    
    /**
     * Creates a new InfluxDbReporter instance.
     *
     * @param registry               the MetricRegistry containing the metrics this reporter will report
     * @param sender                 the InfluxDbSender which is responsible for sending metrics to a influxdb
     *                               server via a transport protocol
     * @param clock                  the instance of the time. Use {@link Clock#defaultClock()} for the default
     * @param prefix                 the prefix of all metric names (may be null)
     * @param rateUnit               the time unit of in which rates will be converted
     * @param durationUnit           the time unit of in which durations will be converted
     * @param filter                 the filter for which metrics to report
     * @param executor               the executor to use while scheduling reporting of metrics (may be null).
     * @param shutdownExecutorOnStop if true, then executor will be stopped in same time with this reporter
     * @param disabledMetricAttributes the disable metric attributes
     */
    protected InfluxDbReporter(MetricRegistry registry,
                               InfluxDbSender sender,
                               Clock clock,
                               MetricName prefix,
                               TimeUnit rateUnit,
                               TimeUnit durationUnit,
                               MetricFilter filter,
                               ScheduledExecutorService executor,
                               boolean shutdownExecutorOnStop,
                               Set<MetricAttribute> disabledMetricAttributes) {
        super(registry, "influxdb-reporter", filter, rateUnit, durationUnit, executor, shutdownExecutorOnStop,
                disabledMetricAttributes);
        this.sender = sender;
        this.clock = clock;
        this.prefix = prefix != null ? prefix : MetricName.build();
    }    
    
    @Override
    @SuppressWarnings("rawtypes")
    public void report(SortedMap<MetricName, Gauge> gauges, 
            SortedMap<MetricName, Counter> counters, 
            SortedMap<MetricName, Histogram> histograms, 
            SortedMap<MetricName, Meter> meters, 
            SortedMap<MetricName, Timer> timers) {

        final long timestamp = clock.getTime();

        try {
            sender.connect();

            for (Map.Entry<MetricName, Gauge> entry : gauges.entrySet()) {
                reportGauge(entry.getKey(), entry.getValue(), timestamp);
            }

            for (Map.Entry<MetricName, Counter> entry : counters.entrySet()) {
                reportCounter(entry.getKey(), entry.getValue(), timestamp);
            }

            for (Map.Entry<MetricName, Histogram> entry : histograms.entrySet()) {
                reportHistogram(entry.getKey(), entry.getValue(), timestamp);
            }

            for (Map.Entry<MetricName, Meter> entry : meters.entrySet()) {
                reportMetered(entry.getKey(), entry.getValue(), timestamp);
            }

            for (Map.Entry<MetricName, Timer> entry : timers.entrySet()) {
                reportTimer(entry.getKey(), entry.getValue(), timestamp);
            }
            sender.flush();
        } catch (IOException e) {
            LOGGER.warn("Unable to report to InfluxDb", sender, e);
        } finally {
            try {
                sender.disconnect();
            } catch (IOException e) {
                LOGGER.warn("Error disconnecting InfluxDb", sender, e);
            }
        }
    }
    
    @Override
    public void stop() {
        try {
            super.stop();
        } finally {
            try {
                sender.close();
            } catch (IOException e) {
                LOGGER.debug("Error disconnecting from InfluxDb", e);
            }
        }
    }
    
    private void reportTimer(MetricName name, Timer timer, long timestamp) throws IOException {
        final Snapshot snapshot = timer.getSnapshot();
        writeMeasurement(name);
        writeFieldIfEnabled(MAX,    convertDuration(snapshot.getMax()));
        writeFieldIfEnabled(MEAN,   convertDuration(snapshot.getMean()));
        writeFieldIfEnabled(MIN,    convertDuration(snapshot.getMin()));
        writeFieldIfEnabled(STDDEV, convertDuration(snapshot.getStdDev()));
        writeFieldIfEnabled(P50,    convertDuration(snapshot.getMedian()));
        writeFieldIfEnabled(P75,    convertDuration(snapshot.get75thPercentile()));
        writeFieldIfEnabled(P95,    convertDuration(snapshot.get95thPercentile()));
        writeFieldIfEnabled(P98,    convertDuration(snapshot.get98thPercentile()));
        writeFieldIfEnabled(P99,    convertDuration(snapshot.get99thPercentile()));
        writeFieldIfEnabled(P999,   convertDuration(snapshot.get999thPercentile()));
        writeMeteredFieldsIfEnabled(timer);
        if (hasValues()) {
            writeTimestampMillis(timestamp);
            reportLine();
        }
    }


    private void reportHistogram(MetricName name, Histogram histogram, long timestamp) throws IOException {
        writeMeasurement(name);
        final Snapshot snapshot = histogram.getSnapshot();
        writeFieldIfEnabled(COUNT, histogram.getCount());
        writeFieldIfEnabled(SUM, histogram.getSum());
        writeFieldIfEnabled(MAX,    snapshot.getMax());
        writeFieldIfEnabled(MEAN,   snapshot.getMean());
        writeFieldIfEnabled(MIN,    snapshot.getMin());
        writeFieldIfEnabled(STDDEV, snapshot.getStdDev());
        writeFieldIfEnabled(P50,    snapshot.getMedian());
        writeFieldIfEnabled(P75,    snapshot.get75thPercentile());
        writeFieldIfEnabled(P95,    snapshot.get95thPercentile());
        writeFieldIfEnabled(P98,    snapshot.get98thPercentile());
        writeFieldIfEnabled(P99,    snapshot.get99thPercentile());
        writeFieldIfEnabled(P999,   snapshot.get999thPercentile());
        if (hasValues()) {
            writeTimestampMillis(timestamp);
            reportLine();
        }
    }
    
    
    private void reportMetered(MetricName name, Metered meter, long timestamp) throws IOException {
        writeMeasurement(name);
        writeMeteredFieldsIfEnabled(meter);
        if (hasValues()) {
            writeTimestampMillis(timestamp);
            reportLine();
        }
    }

    private void writeMeteredFieldsIfEnabled(Metered meter) {
        writeFieldIfEnabled(COUNT,     meter.getCount());
        writeFieldIfEnabled(SUM,       meter.getSum());
        writeFieldIfEnabled(M1_RATE,   convertRate(meter.getOneMinuteRate()));
        writeFieldIfEnabled(M5_RATE,   convertRate(meter.getFiveMinuteRate()));
        writeFieldIfEnabled(M15_RATE,  convertRate(meter.getFifteenMinuteRate()));
        writeFieldIfEnabled(MEAN_RATE, convertRate(meter.getMeanRate()));
    }
    
    private void reportCounter(MetricName name, Counter counter, long timestamp) throws IOException {
        writeMeasurement(name);
        writeFieldIfEnabled(COUNT, counter.getCount());
        if (hasValues()) {
            writeTimestampMillis(timestamp);
            reportLine();
        }
    }

    private void reportGauge(MetricName name, Gauge<?> gauge, long timestamp) throws IOException {
        writeMeasurement(name);
        Object value = gauge.getValue();
        if (value != null) {
            writeField(VALUE);
            if (value instanceof Number) {
                Number number = (Number) value;
                if (number instanceof Long || 
                        number instanceof Integer || 
                        number instanceof Short || 
                        number instanceof Byte) {
                    writeFieldValue(number.longValue());
                } else {
                    writeFieldValue(number.doubleValue());
                }
            } else if (value instanceof Boolean) {
                writeFieldValue(((Boolean)value));
            } else {
                writeFieldValue(value.toString());
            }
        }
        writeTimestampMillis(timestamp);
        reportLine();
    }    
    
    private void reset() {
        str.delete(0, str.length());
    }
    
    private void writeMeasurement(MetricName name) {
        reset();
        char[] nameChars = encodedNameCache.get(name);
        if (nameChars != null) {
            str.append(nameChars);
        } else {
            writeMeasurementNoCache(name);
            nameChars = new char[str.length()];
            str.getChars(0, str.length(), nameChars, 0);
            encodedNameCache.put(name, nameChars);
        }
        
        str.append(' ');
        firstField = true;
    }
    
    private void writeMeasurementNoCache(MetricName name) {
        MetricName prefixedName = MetricName.join(prefix, name);
        
        appendName(prefixedName.getKey(), str);
        Map<String, String> tags = prefixedName.getTags();
        if (!tags.isEmpty()) {
            // InfluxDB Performance and Setup Tips:
            // Sort tags by key before sending them to the database. 
            // The sort should match the results from the Go bytes.Compare function.
            TreeMap<String, String> sortedTags = new TreeMap<>(tags);
            for (Map.Entry<String, String> tag : sortedTags.entrySet()) {
                str.append(',');
                appendName(tag.getKey(), str);
                str.append('=');
                appendName(tag.getValue(), str);
            }
        }
    }
    
    private void writeTimestampMillis(long utcMillis) {
        str.append(' ').append(utcMillis).append("000000\n");
    }
    
    private boolean hasValues() {
        return !firstField;
    }
    
    private void reportLine() throws IOException {
        sender.send(str);
    }

    private void writeFieldIfEnabled(MetricAttribute key, double value) {
        if (!getDisabledMetricAttributes().contains(key)) {
            writeField(key);
            writeFieldValue(value);
        }
    }
    
    private void writeFieldIfEnabled(MetricAttribute key, long value) {
        if (!getDisabledMetricAttributes().contains(key)) {
            writeField(key);
            writeFieldValue(value);
        }
    }
    
    private void writeField(MetricAttribute key) {
        if (!firstField) {
            str.append(',');
        }
        str.append(key.getCode()) // no need to escape, only safe chars
           .append('=');
        firstField = false;
    }
    
    private void writeField(String key) {
        if (!firstField) {
            str.append(',');
        }
        appendName(key, str);
        str.append('=');
        firstField = false;
    }
    
    private void writeFieldValue(double value) {
        str.append(value);
    }
    
    private void writeFieldValue(long value) {
        str.append(value).append('i');
    }
    
    private void writeFieldValue(String value) {
        str.append('"');
        appendString(value, str);
        str.append('"');
    }
    
    private void writeFieldValue(boolean value) {
        str.append(value ? 't' : 'f');
    }
    
    private static void appendName(CharSequence field, StringBuilder dst) {
        int len = field.length();
        for (int i=0; i<len; i++) {
            char ch = field.charAt(i);
            if (ch == ',' || ch == '=' || ch == ' ') {
                // escape
                dst.append('\\');
            }
            dst.append(ch);
        }
    }
    private static void appendString(CharSequence field, StringBuilder dst) {
        int len = field.length();
        for (int i=0; i<len; i++) {
            char ch = field.charAt(i);
            if (ch == '"') {
                // escape
                dst.append('\\');
            }
            dst.append(ch);
        }
    }
    
}
