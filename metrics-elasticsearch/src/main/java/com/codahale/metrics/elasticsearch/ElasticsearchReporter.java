package com.codahale.metrics.elasticsearch;

import java.io.IOException;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

/**
 * Base class for Elasticsearch reporter implementations
 */
public abstract class ElasticsearchReporter extends ScheduledReporter {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(ElasticsearchReporter.class);
    private static final String DATE_DELIMETER = ".";
    private static final DecimalFormat TWO_DIGIT_FORMAT = new DecimalFormat(
            "00");

    private final Clock clock;
    private final String metricPrefix;
    private final String elasticsearchIndexPrefix;
    private final String timestampFieldName;

    private JsonFactory jsonFactory;

    protected ElasticsearchReporter(MetricRegistry registry, Clock clock,
            String elasticsearchIndexPrefix, String timestampFieldName,
            String metricPrefix, TimeUnit rateUnit, TimeUnit durationUnit,
            MetricFilter filter) {
        super(registry, "elasticsearch-reporter", filter, rateUnit,
                durationUnit);
        this.clock = clock;
        this.metricPrefix = metricPrefix;
        this.timestampFieldName = timestampFieldName;

        if (elasticsearchIndexPrefix.endsWith("-")) {
            this.elasticsearchIndexPrefix = elasticsearchIndexPrefix;
        } else {
            this.elasticsearchIndexPrefix = elasticsearchIndexPrefix + "-";
        }
        jsonFactory = new JsonFactory();
    }

    @Override
    public void report(SortedMap<String, Gauge> gauges,
            SortedMap<String, Counter> counters,
            SortedMap<String, Histogram> histograms,
            SortedMap<String, Meter> meters, SortedMap<String, Timer> timers) {
        long timestamp = clock.getTime();
        final DateTime dateTime = new DateTime(DateTimeZone.UTC);

        StringBuilder indexBuilder = new StringBuilder();
        indexBuilder.append(elasticsearchIndexPrefix);
        indexBuilder.append(dateTime.getYear());
        indexBuilder.append(DATE_DELIMETER);
        indexBuilder.append(TWO_DIGIT_FORMAT.format(dateTime.getMonthOfYear()));
        indexBuilder.append(DATE_DELIMETER);
        indexBuilder.append(TWO_DIGIT_FORMAT.format(dateTime.getDayOfMonth()));

        String index = indexBuilder.toString();

        try {
            for (Map.Entry<String, Gauge> entry : gauges.entrySet()) {
                reportGauge(index, timestamp, entry.getKey(), entry.getValue());
            }

            for (Map.Entry<String, Counter> entry : counters.entrySet()) {
                reportCounter(index, timestamp, entry.getKey(),
                        entry.getValue());
            }

            for (Map.Entry<String, Histogram> entry : histograms.entrySet()) {
                reportHistogram(index, timestamp, entry.getKey(),
                        entry.getValue());
            }

            for (Map.Entry<String, Meter> entry : meters.entrySet()) {
                reportMetered(index, timestamp, entry.getKey(),
                        entry.getValue());
            }

            for (Map.Entry<String, Timer> entry : timers.entrySet()) {
                reportTimer(index, timestamp, entry.getKey(), entry.getValue());
            }

            sendBulkRequest();
        } catch (IOException e) {
            LOGGER.warn("Unable to report to Elasticsearch", e);
        }
    }

    private void reportTimer(String index, long timestamp, String name,
            Timer timer) throws IOException {
        StringWriter writer = new StringWriter();
        JsonGenerator jsonGenerator = jsonFactory.createGenerator(writer);
        jsonGenerator.writeStartObject();
        jsonGenerator.writeNumberField(timestampFieldName, timestamp);
        jsonGenerator.writeStringField("@name", prefixMetricName(name));

        final Snapshot snapshot = timer.getSnapshot();

        jsonGenerator.writeNumberField("max",
                convertDuration(snapshot.getMax()));
        jsonGenerator.writeNumberField("mean",
                convertDuration(snapshot.getMean()));
        jsonGenerator.writeNumberField("min",
                convertDuration(snapshot.getMin()));
        jsonGenerator.writeNumberField("stddev",
                convertDuration(snapshot.getStdDev()));
        jsonGenerator.writeNumberField("p50",
                convertDuration(snapshot.getMedian()));
        jsonGenerator.writeNumberField("p75",
                convertDuration(snapshot.get75thPercentile()));
        jsonGenerator.writeNumberField("p95",
                convertDuration(snapshot.get95thPercentile()));
        jsonGenerator.writeNumberField("p98",
                convertDuration(snapshot.get98thPercentile()));
        jsonGenerator.writeNumberField("p99",
                convertDuration(snapshot.get99thPercentile()));
        jsonGenerator.writeNumberField("p999",
                convertDuration(snapshot.get999thPercentile()));

        jsonGenerator.writeNumberField("count", timer.getCount());
        jsonGenerator.writeNumberField("m1_rate",
                convertRate(timer.getOneMinuteRate()));
        jsonGenerator.writeNumberField("m5_rate",
                convertRate(timer.getFiveMinuteRate()));
        jsonGenerator.writeNumberField("m15_rate",
                convertRate(timer.getFifteenMinuteRate()));
        jsonGenerator.writeNumberField("mean_rate",
                convertRate(timer.getMeanRate()));

        jsonGenerator.writeEndObject();
        jsonGenerator.flush();
        addReportToBulkRequest(index, MetricElasticsearchTypes.TIMER,
                writer.toString());
    }

    private void reportMetered(String index, long timestamp, String name,
            Metered meter) throws IOException {
        StringWriter writer = new StringWriter();
        JsonGenerator jsonGenerator = jsonFactory.createGenerator(writer);
        jsonGenerator.writeStartObject();
        jsonGenerator.writeNumberField(timestampFieldName, timestamp);
        jsonGenerator.writeStringField("@name", prefixMetricName(name));

        jsonGenerator.writeNumberField("count", meter.getCount());
        jsonGenerator.writeNumberField("m1_rate",
                convertRate(meter.getOneMinuteRate()));
        jsonGenerator.writeNumberField("m5_rate",
                convertRate(meter.getFiveMinuteRate()));
        jsonGenerator.writeNumberField("m15_rate",
                convertRate(meter.getFifteenMinuteRate()));
        jsonGenerator.writeNumberField("mean_rate",
                convertRate(meter.getMeanRate()));

        jsonGenerator.writeEndObject();
        jsonGenerator.flush();
        addReportToBulkRequest(index, MetricElasticsearchTypes.METER,
                writer.toString());
    }

    private void reportHistogram(String index, long timestamp, String name,
            Histogram histogram) throws IOException {
        StringWriter writer = new StringWriter();
        JsonGenerator jsonGenerator = jsonFactory.createGenerator(writer);
        jsonGenerator.writeStartObject();
        jsonGenerator.writeNumberField(timestampFieldName, timestamp);
        jsonGenerator.writeStringField("@name", prefixMetricName(name));

        final Snapshot snapshot = histogram.getSnapshot();

        jsonGenerator.writeNumberField("count", histogram.getCount());
        jsonGenerator.writeNumberField("max",
                convertDuration(snapshot.getMax()));
        jsonGenerator.writeNumberField("mean",
                convertDuration(snapshot.getMean()));
        jsonGenerator.writeNumberField("min",
                convertDuration(snapshot.getMin()));
        jsonGenerator.writeNumberField("stddev",
                convertDuration(snapshot.getStdDev()));
        jsonGenerator.writeNumberField("p50",
                convertDuration(snapshot.getMedian()));
        jsonGenerator.writeNumberField("p75",
                convertDuration(snapshot.get75thPercentile()));
        jsonGenerator.writeNumberField("p95",
                convertDuration(snapshot.get95thPercentile()));
        jsonGenerator.writeNumberField("p98",
                convertDuration(snapshot.get98thPercentile()));
        jsonGenerator.writeNumberField("p99",
                convertDuration(snapshot.get99thPercentile()));
        jsonGenerator.writeNumberField("p999",
                convertDuration(snapshot.get999thPercentile()));

        jsonGenerator.writeEndObject();
        jsonGenerator.flush();
        addReportToBulkRequest(index, MetricElasticsearchTypes.HISTOGRAM,
                writer.toString());
    }

    private void reportCounter(String index, long timestamp, String name,
            Counter counter) throws IOException {
        StringWriter writer = new StringWriter();
        JsonGenerator jsonGenerator = jsonFactory.createGenerator(writer);
        jsonGenerator.writeStartObject();
        jsonGenerator.writeNumberField(timestampFieldName, timestamp);
        jsonGenerator.writeStringField("@name", prefixMetricName(name));
        jsonGenerator.writeNumberField("count", counter.getCount());
        jsonGenerator.writeEndObject();
        jsonGenerator.flush();
        addReportToBulkRequest(index, MetricElasticsearchTypes.COUNTER,
                writer.toString());
    }

    private void reportGauge(String index, long timestamp, String name,
            Gauge gauge) throws IOException {
        Object value = gauge.getValue();
        if (value == null) {
            return;
        }
        StringWriter writer = new StringWriter();
        JsonGenerator jsonGenerator = jsonFactory.createGenerator(writer);
        jsonGenerator.writeStartObject();
        jsonGenerator.writeNumberField(timestampFieldName, timestamp);
        jsonGenerator.writeStringField("@name", prefixMetricName(name));

        if (value instanceof Float) {
            jsonGenerator.writeNumberField("floatValue", (Float) value);
        } else if (value instanceof Double) {
            jsonGenerator.writeNumberField("doubleValue", (Double) value);
        } else if (value instanceof Byte) {
            jsonGenerator.writeNumberField("byteValue",
                    ((Byte) value).intValue());
        } else if (value instanceof Short) {
            jsonGenerator.writeNumberField("shortValue", (Short) value);
        } else if (value instanceof Integer) {
            jsonGenerator.writeNumberField("integerValue", (Integer) value);
        } else if (value instanceof Long) {
            jsonGenerator.writeNumberField("longValue", (Long) value);
        } else {
            jsonGenerator.writeStringField("stringValue", value.toString());
        }

        jsonGenerator.writeEndObject();
        jsonGenerator.flush();
        addReportToBulkRequest(index, MetricElasticsearchTypes.GAUGE,
                writer.toString());
    }

    protected abstract void addReportToBulkRequest(String index, String type,
            String json) throws IOException;

    protected abstract void sendBulkRequest() throws IOException;

    private String prefixMetricName(String... components) {
        return MetricRegistry.name(metricPrefix, components);
    }
}
