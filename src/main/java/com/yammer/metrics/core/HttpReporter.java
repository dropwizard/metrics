package com.yammer.metrics.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.yammer.metrics.core.VirtualMachineMetrics.*;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;

/**
 * A very simple HTTP reporter which listens on the given port and returns all
 * JVM and application metrics as a JSON value.
 *
 * @author coda
 * @see VirtualMachineMetrics
 */
public class HttpReporter {
	private final JsonFactory factory = new JsonFactory();
	private final ExecutorService serverThread = Executors.newSingleThreadExecutor(new NamedThreadFactory("metrics-http-reporter"));
	private final Map<MetricName, Metric> metrics;
	private final int port;
	private ServerSocket serverSocket;
	private Future<?> future;

	private class ServerThread implements Runnable {
		@Override
		public void run() {
			try {
				while (serverSocket.isBound()) {
					final Socket client = serverSocket.accept();
					final BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
					while (!reader.readLine().equals("")) { /* I don't care */ }
					final OutputStreamWriter writer = new OutputStreamWriter(client.getOutputStream());
					writer.write("HTTP/1.1 200 OK\n");
					writer.write("Server: Metrics\n");
					writer.write("Content-Type: application/json\n");
					writer.write("Connection: close\n");
					writer.write("\n");

					final JsonGenerator json = factory.createJsonGenerator(writer).useDefaultPrettyPrinter();
					json.writeStartObject();
					{
						writeVmMetrics(json);
						writeRegularMetrics(json);
					}
					json.writeEndObject();
					json.close();
					client.close();
				}
			} catch (IOException ignored) {
//					ignored.printStackTrace();
			}
		}
	}

	/*package*/ HttpReporter(Map<MetricName, Metric> metrics, int port) {
		this.port = port;
		this.metrics = metrics;
	}

	/**
	 * Begins listening on the specified port.
	 *
	 * @throws IOException if there is an error listening on the port
	 */
	public void start() throws IOException {
		this.serverSocket = new ServerSocket(port);
		this.future = serverThread.submit(new ServerThread());
	}

	/**
	 * Stops listening if the server thread is running.
	 *
	 * @throws IOException if there is an error stopping the HTTP server
	 */
	public void stop() throws IOException {
		if (future != null) {
			serverSocket.close();
			future.cancel(false);
			future = null;
			serverSocket = null;
		}
	}

	private void writeRegularMetrics(JsonGenerator json) throws IOException {
		for (Entry<String, SortedMap<String, Metric>> entry : sortedMetrics().entrySet()) {
			json.writeFieldName(entry.getKey());
			json.writeStartObject();
			{
				for (Entry<String, Metric> subEntry : entry.getValue().entrySet()) {
					writeMetric(json, subEntry.getKey(), subEntry.getValue());
				}
			}
			json.writeEndObject();
		}
	}

	private SortedMap<String, SortedMap<String, Metric>> sortedMetrics() {
		final SortedMap<String, SortedMap<String, Metric>> sortedMetrics =
				new TreeMap<String, SortedMap<String, Metric>>();
		for (Entry<MetricName, Metric> entry : metrics.entrySet()) {
			final String packageName = entry.getKey().getKlass().getCanonicalName();
			SortedMap<String, Metric> submetrics = sortedMetrics.get(packageName);
			if (submetrics == null) {
				submetrics = new TreeMap<String, Metric>();
				sortedMetrics.put(packageName, submetrics);
			}
			submetrics.put(entry.getKey().getName(), entry.getValue());
		}
		return sortedMetrics;
	}

	private void writeMetric(JsonGenerator json, String key, Metric metric) throws IOException {
		if (metric instanceof GaugeMetric<?>) {
			json.writeFieldName(key);
			writeGauge(json, (GaugeMetric) metric);
		} else if (metric instanceof CounterMetric) {
			json.writeFieldName(key);
			writeCounter(json, (CounterMetric) metric);
		} else if (metric instanceof MeterMetric) {
			json.writeFieldName(key);
			writeMeter(json, (MeterMetric) metric);
		} else if (metric instanceof TimerMetric) {
			json.writeFieldName(key);
			writeTimer(json, (TimerMetric) metric);
		}
	}

	private void writeCounter(JsonGenerator json, CounterMetric counter) throws IOException {
		json.writeStartObject();
		{
			json.writeStringField("type", "counter");
			json.writeNumberField("count", counter.count());
		}
		json.writeEndObject();
	}

	private void writeGauge(JsonGenerator json, GaugeMetric gauge) throws IOException {
		json.writeStartObject();
		{
			json.writeStringField("type", "gauge");
			final Object value = gauge.value();
			if (value == null) {
				json.writeNullField("value");
			} else {
				json.writeStringField("value", gauge.value().toString());
			}
		}
		json.writeEndObject();
	}

	private void writeVmMetrics(JsonGenerator json) throws IOException {
		json.writeFieldName("jvm");
		json.writeStartObject();
		{

			json.writeFieldName("memory");
			json.writeStartObject();
			{
				json.writeNumberField("heap_usage", heapUsage());
				json.writeNumberField("non_heap_usage", nonHeapUsage());
				json.writeFieldName("memory_pool_usages");
				json.writeStartObject();
				{
					for (Entry<String, Double> pool : memoryPoolUsage().entrySet()) {
						json.writeNumberField(pool.getKey(), pool.getValue());
					}
				}
				json.writeEndObject();
			}
			json.writeEndObject();

			json.writeNumberField("daemon_thread_count", daemonThreadCount());
			json.writeNumberField("thread_count", threadCount());
			json.writeNumberField("uptime", uptime());
			json.writeNumberField("fd_usage", fileDescriptorUsage());

			json.writeFieldName("gc");
			json.writeStartObject();
			{
				json.writeFieldName("duration");
				json.writeStartObject();
				{
					for (Entry<String, TimerMetric> entry : gcDurations().entrySet()) {
						json.writeFieldName(entry.getKey());
						writeTimer(json, entry.getValue());
					}
				}
				json.writeEndObject();

				json.writeFieldName("throughput");
				json.writeStartObject();
				{
					for (Entry<String, MeterMetric> entry : gcThroughputs().entrySet()) {
						json.writeFieldName(entry.getKey());
						writeMeter(json, entry.getValue());
					}
				}
				json.writeEndObject();
			}
			json.writeEndObject();

		}
		json.writeEndObject();
	}

	private void writeMeter(JsonGenerator json, MeterMetric meter) throws IOException {
		json.writeStartObject();
		{
			json.writeStringField("type", "meter");
			json.writeStringField("event_type", meter.getEventType());
			json.writeStringField("unit", meter.getScaleUnit().toString().toLowerCase());
			json.writeNumberField("count", meter.count());
			json.writeNumberField("mean", meter.meanRate());
			json.writeNumberField("m1", meter.oneMinuteRate());
			json.writeNumberField("m5", meter.fiveMinuteRate());
			json.writeNumberField("m15", meter.fifteenMinuteRate());
		}
		json.writeEndObject();
	}

	private void writeTimer(JsonGenerator json, TimerMetric timer) throws IOException {
		json.writeStartObject();
		{
			json.writeStringField("type", "timer");
			json.writeFieldName("latency");
			json.writeStartObject();
			{
				json.writeStringField("unit", timer.getLatencyUnit().toString().toLowerCase());
				json.writeNumberField("min", timer.min());
				json.writeNumberField("max", timer.max());
				json.writeNumberField("mean", timer.mean());
				json.writeNumberField("std_dev", timer.stdDev());

				final double[] percentiles = timer.percentiles(0.5, 0.75, 0.95, 0.98, 0.99, 0.999);
				json.writeNumberField("median", percentiles[0]);
				json.writeNumberField("p75", percentiles[1]);
				json.writeNumberField("p95", percentiles[2]);
				json.writeNumberField("p98", percentiles[3]);
				json.writeNumberField("p99", percentiles[4]);
				json.writeNumberField("p999", percentiles[5]);
			}
			json.writeEndObject();

			json.writeFieldName("rate");
			json.writeStartObject();
			{
				json.writeStringField("unit", timer.getRateUnit().toString().toLowerCase());
				json.writeNumberField("count", timer.count());
				json.writeNumberField("mean", timer.meanRate());
				json.writeNumberField("m1", timer.oneMinuteRate());
				json.writeNumberField("m5", timer.fiveMinuteRate());
				json.writeNumberField("m15", timer.fifteenMinuteRate());
			}
			json.writeEndObject();
		}
		json.writeEndObject();
	}
}
