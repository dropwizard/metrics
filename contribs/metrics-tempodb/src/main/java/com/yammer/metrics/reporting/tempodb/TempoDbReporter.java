package com.yammer.metrics.reporting.tempodb;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Map.Entry;
import java.util.Set;

import sun.misc.BASE64Encoder;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.yammer.metrics.core.Clock;
import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.core.Histogram;
import com.yammer.metrics.core.Metered;
import com.yammer.metrics.core.Metric;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricProcessor;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.core.Sampling;
import com.yammer.metrics.core.Summarizable;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.reporting.AbstractPollingReporter;
import com.yammer.metrics.stats.Snapshot;

public class TempoDbReporter extends AbstractPollingReporter implements MetricProcessor<JsonGenerator> {

	public static final String DEFAULT_ENDPOINT = "https://api.tempo-db.com/v1/data";
	private static final BASE64Encoder encoder = new BASE64Encoder();
	private static final JsonFactory factory = new JsonFactory();
	private static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");
	private final String authorization;
	private final String endpoint;
	private final Clock clock;

	public TempoDbReporter(MetricsRegistry registry, String name, String apiKey, String apiSecret) {
		this(registry, name, apiKey, apiSecret, DEFAULT_ENDPOINT, Clock.defaultClock());
	}

	public TempoDbReporter(
			MetricsRegistry registry,
			String name, 
			String apiKey, 
			String apiSecret, 
			String endpoint,
			Clock clock) {
		super(registry, name);
		// Wat. I don't even.
		// http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4615330
		this.authorization = ("Basic " + encoder.encode((apiKey + ":" + apiSecret).getBytes())).replace("\n", "");
		this.endpoint = endpoint;
		this.clock = clock;
	}

	@Override
	public void run() {
		try {
			final HttpURLConnection connection = (HttpURLConnection)new URL(this.endpoint).openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Authorization", this.authorization);
			connection.setDoOutput(true);
			final OutputStream stream = connection.getOutputStream();
			report(stream);
			stream.close();
			final int status = connection.getResponseCode();
			final String message = connection.getResponseMessage();
			connection.disconnect();
			if(2 != (status / 100)) {
				System.err.println("Unexpected response " + status + ": " + message);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void report(final OutputStream output) throws Exception {
		final JsonGenerator json = factory.createJsonGenerator(output, JsonEncoding.UTF8);
		json.setPrettyPrinter(new DefaultPrettyPrinter());
		json.writeStartObject();
		{
			json.writeStringField("t", df.format(this.clock.getTime()));
			json.writeArrayFieldStart("data");
			{
				final Set<Entry<MetricName, Metric>> metrics = getMetricsRegistry().getAllMetrics().entrySet();
				for (Entry<MetricName, Metric> entry : metrics) {
					entry.getValue().processWith(this, entry.getKey(), json);
				}
			}
			json.writeEndArray();
		}
		json.writeEndObject();
		json.close();
	}

	@Override
	public void processMeter(MetricName name, Metered meter, JsonGenerator json) throws IOException {
		writeValue(name, "count", meter.getCount(), json);
		writeValue(name, "mean", meter.getMeanRate(), json);
		writeValue(name, "1m", meter.getOneMinuteRate(), json);
		writeValue(name, "5m", meter.getFiveMinuteRate(), json);
		writeValue(name, "15m", meter.getFifteenMinuteRate(), json);
	}

	@Override
	public void processCounter(MetricName name, Counter counter, JsonGenerator json) throws IOException {
		writeValue(name, "count", counter.getCount(), json);
	}

	@Override
	public void processHistogram(MetricName name, Histogram histogram, JsonGenerator json) throws IOException {
		writeValue(name, "count", histogram.getCount(), json);
		writeSummarizable(name, json, histogram);
		writeSampling(name, json, histogram);

	}

	@Override
	public void processTimer(MetricName name, Timer timer, JsonGenerator json) throws IOException {
		processMeter(name, timer, json);
		writeSampling(name, json, timer);
		writeSummarizable(name, json, timer);
	}

	@Override
	public void processGauge(MetricName name, Gauge<?> gauge, JsonGenerator json) throws IOException {
		final Object value = gauge.getValue();
		if (value instanceof Number) {
			writeValue(name, "value", ((Number)value).doubleValue(), json);
		}
	}

	private static void writeSampling(MetricName name, JsonGenerator json, Sampling metric) throws IOException {
		final Snapshot snapshot = metric.getSnapshot();
		writeValue(name, "75th", snapshot.get75thPercentile(), json);
		writeValue(name, "95th", snapshot.get95thPercentile(), json);
		writeValue(name, "98th", snapshot.get98thPercentile(), json);
		writeValue(name, "99th", snapshot.get99thPercentile(), json);
		writeValue(name, "999th", snapshot.get999thPercentile(), json);
	}

	private static void writeSummarizable(MetricName name, JsonGenerator json, Summarizable metric) throws IOException {
		writeValue(name, "min", metric.getMin(), json);
		writeValue(name, "max", metric.getMax(), json);
		writeValue(name, "mean", metric.getMean(), json);
		writeValue(name, "stddev", metric.getStdDev(), json);
		writeValue(name, "sum", metric.getSum(), json);
	}

	private static void writeValue(MetricName name, String suffix, double value, JsonGenerator json) throws IOException {
		json.writeStartObject();
		{
			json.writeStringField("key", name.getMBeanName() + "." + suffix);
			json.writeNumberField("v", value);
		}
		json.writeEndObject();
		json.flush();
	}
}