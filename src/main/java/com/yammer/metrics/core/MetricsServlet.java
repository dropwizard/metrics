package com.yammer.metrics.core;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.yammer.metrics.core.VirtualMachineMetrics.*;

import com.yammer.metrics.core.HealthCheck.Result;
import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

public class MetricsServlet extends HttpServlet {
	private static final String TEMPLATE = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\"\n" +
										   "        \"http://www.w3.org/TR/html4/loose.dtd\">\n" +
										   "<html>\n" +
										   "<head>\n" +
										   "  <title>Metrics</title>\n" +
										   "</head>\n" +
										   "<body>\n" +
										   "  <h1>Operational Menu</h1>\n" +
										   "  <ul>\n" +
										   "    <li><a href=\"{0}\">Metrics</a></li>\n" +
										   "    <li><a href=\"{1}\">Ping</a></li>\n" +
										   "    <li><a href=\"{2}\">Threads</a></li>\n" +
										   "    <li><a href=\"{3}\">Healthcheck</a></li>\n" +
										   "  </ul>\n" +
										   "</body>\n" +
										   "</html>";
	private JsonFactory factory;
	private String metricsUri, pingUri, threadsUri, healthcheckUri;

	public MetricsServlet() {
		this(new JsonFactory(new ObjectMapper()), "/healthcheck", "/metrics", "/ping", "/threads");
	}

	public MetricsServlet(JsonFactory factory) {
		this(factory, "/healthcheck", "/metrics", "/ping", "/threads");
	}

	public MetricsServlet(String healthcheckUri, String metricsUri, String pingUri, String threadsUri) {
		this(new JsonFactory(new ObjectMapper()), healthcheckUri, metricsUri, pingUri, threadsUri);
	}

	public MetricsServlet(JsonFactory factory, String healthcheckUri, String metricsUri, String pingUri, String threadsUri) {
		this.factory = factory;
		this.healthcheckUri = healthcheckUri;
		this.metricsUri = metricsUri;
		this.pingUri = pingUri;
		this.threadsUri = threadsUri;
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		final String metricsUri = config.getInitParameter("metrics-uri");
		final String pingUri = config.getInitParameter("ping-uri");
		final String threadsUri = config.getInitParameter("threads-uri");
		final String healthcheckUri = config.getInitParameter("healthcheck-uri");

		if (metricsUri != null) {
			this.metricsUri = metricsUri;
		}

		if (pingUri != null) {
			this.pingUri = pingUri;
		}

		if (threadsUri != null) {
			this.threadsUri = threadsUri;
		}

		if (healthcheckUri != null) {
			this.healthcheckUri = healthcheckUri;
		}

		final Object factory = config.getServletContext().getAttribute(JsonFactory.class.getCanonicalName());
		if (factory != null && factory instanceof JsonFactory) {
			this.factory = (JsonFactory) factory;
		}
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		final String uri = req.getPathInfo();
		if (uri.equals("/")) {
			handleHome(resp);
		} else if (uri.startsWith(metricsUri)) {
			handleMetrics(req.getParameter("class"), Boolean.parseBoolean(req.getParameter("full-samples")), resp);
		} else if (uri.equals(pingUri)) {
			handlePing(resp);
		} else if (uri.equals(threadsUri)) {
			handleThreadDump(resp);
		} else if (uri.equals(healthcheckUri)) {
			handleHealthCheck(resp);
		} else {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}

	private void handleHome(HttpServletResponse resp) throws IOException {
		resp.setStatus(HttpServletResponse.SC_OK);
		resp.setContentType("text/html");

		final PrintWriter writer = resp.getWriter();
		writer.println(MessageFormat.format(TEMPLATE, metricsUri, pingUri, threadsUri, healthcheckUri));
		writer.close();
	}

	private void handleHealthCheck(HttpServletResponse resp) throws IOException {
		boolean allHealthy = true;
		final Map<String, Result> results = new TreeMap<String, Result>();
		for (Entry<String, HealthCheck> entry : Metrics.HEALTH_CHECKS.entrySet()) {
			final Result result = entry.getValue().execute();
			allHealthy &= result.isHealthy();
			results.put(entry.getKey(), result);
		}

		PrintWriter writer;
		if (allHealthy) {
			resp.setStatus(HttpServletResponse.SC_OK);
			resp.setContentType("text/plain");
			writer = resp.getWriter();


		} else {
			resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			resp.setContentType("text/plain");
			writer = resp.getWriter();
		}

		for (Entry<String, Result> entry : results.entrySet()) {
			final Result result = entry.getValue();
			if (result.isHealthy()) {
				writer.format("* %s: OK\n", entry.getKey());
			} else {
				if (result.getMessage() != null) {
					writer.format("! %s: ERROR\n!  %s\n", entry.getKey(), result.getMessage());
				}

				if (result.getError() != null) {
					writer.println();
					result.getError().printStackTrace(writer);
					writer.println();
				}
			}
		}

	}

	private void handleThreadDump(HttpServletResponse resp) throws IOException {
		resp.setStatus(HttpServletResponse.SC_OK);
		resp.setContentType("text/plain");
		final PrintWriter writer = resp.getWriter();

		Map<Thread, StackTraceElement[]> traces = Thread.getAllStackTraces();
		for (Entry<Thread, StackTraceElement[]> entry : traces.entrySet()) {
			Thread t = entry.getKey();
			writer.format("Thread: %s  %s\n", t.getName(), t.isDaemon() ? "daemon" : "non-daemon");
			if (!t.isDaemon()) {
				for (StackTraceElement frame : entry.getValue()) {
					writer.format("\t%s.%s[%s:%d]\n", frame.getClassName(), frame.getMethodName(),
							frame.getFileName(), frame.getLineNumber());
				}
			}
		}
		writer.close();
	}

	private void handlePing(HttpServletResponse resp) throws IOException {
		resp.setStatus(HttpServletResponse.SC_OK);
		resp.setContentType("text/plain");
		final PrintWriter writer = resp.getWriter();
		writer.println("pong");
		writer.close();
	}

	private void handleMetrics(String classPrefix, boolean showFullSamples, HttpServletResponse resp) throws IOException {
		resp.setStatus(HttpServletResponse.SC_OK);
		resp.setContentType("application/json");
		final OutputStream output = resp.getOutputStream();
		final JsonGenerator json = factory.createJsonGenerator(output, JsonEncoding.UTF8);
		json.writeStartObject();
		{
			if ("jvm".equals(classPrefix) || classPrefix == null) {
				writeVmMetrics(json, showFullSamples);
			}

			writeRegularMetrics(json, classPrefix, showFullSamples);
		}
		json.writeEndObject();
		json.close();
	}

	private void writeRegularMetrics(JsonGenerator json, String classPrefix, boolean showFullSamples) throws IOException {
		for (Entry<String, Map<String, Metric>> entry : Utils.sortMetrics(Metrics.METRICS).entrySet()) {
			if (classPrefix == null || entry.getKey().startsWith(classPrefix)) {
				json.writeFieldName(entry.getKey());
				json.writeStartObject();
				{
					for (Entry<String, Metric> subEntry : entry.getValue().entrySet()) {
						writeMetric(json, subEntry.getKey(), subEntry.getValue(), showFullSamples);
					}
				}
				json.writeEndObject();
			}
		}
	}

	private void writeMetric(JsonGenerator json, String key, Metric metric, boolean showFullSamples) throws IOException {
		if (metric instanceof GaugeMetric<?>) {
			json.writeFieldName(key);
			writeGauge(json, (GaugeMetric) metric);
		} else if (metric instanceof CounterMetric) {
			json.writeFieldName(key);
			writeCounter(json, (CounterMetric) metric);
		} else if (metric instanceof MeterMetric) {
			json.writeFieldName(key);
			writeMeter(json, (MeterMetric) metric);
		} else if (metric instanceof HistogramMetric) {
			json.writeFieldName(key);
			writeHistogram(json, (HistogramMetric) metric, showFullSamples);
		} else if (metric instanceof TimerMetric) {
			json.writeFieldName(key);
			writeTimer(json, (TimerMetric) metric, showFullSamples);
		}
	}

	private void writeHistogram(JsonGenerator json, HistogramMetric histogram, boolean showFullSamples) throws IOException {
		json.writeStartObject();
		{
			json.writeStringField("type", "histogram");
			json.writeNumberField("min", histogram.min());
			json.writeNumberField("max", histogram.max());
			json.writeNumberField("mean", histogram.mean());
			json.writeNumberField("std_dev", histogram.stdDev());

			final double[] percentiles = histogram.percentiles(0.5, 0.75, 0.95, 0.98, 0.99, 0.999);
			json.writeNumberField("median", percentiles[0]);
			json.writeNumberField("p75", percentiles[1]);
			json.writeNumberField("p95", percentiles[2]);
			json.writeNumberField("p98", percentiles[3]);
			json.writeNumberField("p99", percentiles[4]);
			json.writeNumberField("p999", percentiles[5]);

			if (showFullSamples) {
				json.writeObjectField("values", histogram.values());
			}
		}
		json.writeEndObject();
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
			json.writeFieldName("value");
			final Object value = gauge.value();
			try {
				json.writeObject(value);
			} catch (JsonMappingException e) {
				json.writeString("unknown value type: " + value.getClass());
			}
		}
		json.writeEndObject();
	}

	private void writeVmMetrics(JsonGenerator json, boolean showFullSamples) throws IOException {
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
						writeTimer(json, entry.getValue(), showFullSamples);
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
			json.writeStringField("event_type", meter.eventType());
			json.writeStringField("unit", meter.scaleUnit().toString().toLowerCase());
			json.writeNumberField("count", meter.count());
			json.writeNumberField("mean", meter.meanRate());
			json.writeNumberField("m1", meter.oneMinuteRate());
			json.writeNumberField("m5", meter.fiveMinuteRate());
			json.writeNumberField("m15", meter.fifteenMinuteRate());
		}
		json.writeEndObject();
	}

	private void writeTimer(JsonGenerator json, TimerMetric timer, boolean showFullSamples) throws IOException {
		json.writeStartObject();
		{
			json.writeStringField("type", "timer");
			json.writeFieldName("duration");
			json.writeStartObject();
			{
				json.writeStringField("unit", timer.durationUnit().toString().toLowerCase());
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

				if (showFullSamples) {
					json.writeObjectField("values", timer.values());
				}
			}
			json.writeEndObject();

			json.writeFieldName("rate");
			json.writeStartObject();
			{
				json.writeStringField("unit", timer.rateUnit().toString().toLowerCase());
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
