package com.yammer.metrics.reporting;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.*;
import com.yammer.metrics.stats.Snapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

/**
 * An HTTP servlet which outputs the metrics in a {@link MetricsRegistry} (and optionally the data
 * provided by {@link VirtualMachineMetrics}) in a JSON object. Only responds to {@code GET}
 * requests.
 * <p/>
 * If the servlet context has an attribute named
 * {@code com.yammer.metrics.reporting.MetricsServlet.registry} which is a
 * {@link MetricsRegistry} instance, {@link MetricsServlet} will use it instead of {@link Metrics}.
 * <p/>
 * {@link MetricsServlet} also takes an initialization parameter, {@code show-jvm-metrics}, which
 * should be a boolean value (e.g., {@code "true"} or {@code "false"}). It determines whether or not
 * JVM-level metrics will be included in the JSON output.
 * <p/>
 * {@code GET} requests to {@link MetricsServlet} can make use of the following query-string
 * parameters:
 * <dl>
 *     <dt><code>/metrics?class=com.example.service</code></dt>
 *     <dd>
 *         <code>class</code> is a string used to filter the metrics in the JSON by metric name. In
 *         the given example, only metrics for classes whose canonical name starts with
 *         <code>com.example.service</code> would be shown. You can also use <code>jvm</code> for
 *         just the JVM-level metrics.
 *     </dd>
 *
 *     <dt><code>/metrics?pretty=true</code></dt>
 *     <dd>
 *         <code>pretty</code> determines whether or not the JSON which is returned is printed with
 *         indented whitespace or not. If you're looking at the JSON in the browser, use this.
 *     </dd>
 *
 *     <dt><code>/metrics?full-samples=true</code></dt>
 *     <dd>
 *         <code>full-samples</code> determines whether or not the JSON which is returned will
 *         include the full content of histograms' and timers' reservoir samples. If you're
 *         aggregating across hosts, you may want to do this to allow for more accurate quantile
 *         calculations.
 *     </dd>
 * </dl>
 */
public class MetricsServlet extends HttpServlet implements MetricProcessor<MetricsServlet.Context> {

    /**
     * The attribute name of the {@link MetricsRegistry} instance in the servlet context.
     */
    public static final String REGISTRY_ATTRIBUTE = MetricsServlet.class.getName() + ".registry";

    /**
     * The attribute name of the {@link JsonFactory} instance in the servlet context.
     */
    public static final String JSON_FACTORY_ATTRIBUTE = JsonFactory.class.getCanonicalName();

    /**
     * The initialization parameter name which determines whether or not JVM_level metrics will be
     * included in the JSON output.
     */
    public static final String SHOW_JVM_METRICS = "show-jvm-metrics";

    static final class Context {
        final boolean showFullSamples;
        final JsonGenerator json;

        Context(JsonGenerator json, boolean showFullSamples) {
            this.json = json;
            this.showFullSamples = showFullSamples;
        }
    }

    private static final JsonFactory DEFAULT_JSON_FACTORY = new JsonFactory(new ObjectMapper());
    private static final Logger LOGGER = LoggerFactory.getLogger(MetricsServlet.class);
    private static final String CONTENT_TYPE = "application/json";

    private final Clock clock;
    private final VirtualMachineMetrics vm;
    private MetricsRegistry registry;
    private JsonFactory factory;
    private boolean showJvmMetrics;

    /**
     * Creates a new {@link MetricsServlet}.
     */
    public MetricsServlet() {
        this(Clock.defaultClock(), VirtualMachineMetrics.getInstance(),
             Metrics.defaultRegistry(), DEFAULT_JSON_FACTORY, true);
    }

    /**
     * Creates a new {@link MetricsServlet}.
     *
     * @param showJvmMetrics    whether or not JVM-level metrics will be included in the output
     */
    public MetricsServlet(boolean showJvmMetrics) {
        this(Clock.defaultClock(), VirtualMachineMetrics.getInstance(),
             Metrics.defaultRegistry(), DEFAULT_JSON_FACTORY, showJvmMetrics);
    }

    /**
     * Creates a new {@link MetricsServlet}.
     *
     * @param clock             the clock used for the current time
     * @param vm                a {@link VirtualMachineMetrics} instance
     * @param registry          a {@link MetricsRegistry}
     * @param factory           a {@link JsonFactory}
     * @param showJvmMetrics    whether or not JVM-level metrics will be included in the output
     */
    public MetricsServlet(Clock clock,
                          VirtualMachineMetrics vm,
                          MetricsRegistry registry,
                          JsonFactory factory,
                          boolean showJvmMetrics) {
        this.clock = clock;
        this.vm = vm;
        this.registry = registry;
        this.factory = factory;
        this.showJvmMetrics = showJvmMetrics;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        final Object factory = config.getServletContext()
                                     .getAttribute(JSON_FACTORY_ATTRIBUTE);
        if (factory instanceof JsonFactory) {
            this.factory = (JsonFactory) factory;
        }

        final Object o = config.getServletContext().getAttribute(REGISTRY_ATTRIBUTE);
        if (o instanceof MetricsRegistry) {
            this.registry = (MetricsRegistry) o;
        }

        final String showJvmMetricsParam = config.getInitParameter(SHOW_JVM_METRICS);
        if (showJvmMetricsParam != null) {
            this.showJvmMetrics = Boolean.parseBoolean(showJvmMetricsParam);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final String classPrefix = req.getParameter("class");
        final boolean pretty = Boolean.parseBoolean(req.getParameter("pretty"));
        final boolean showFullSamples = Boolean.parseBoolean(req.getParameter("full-samples"));

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType(CONTENT_TYPE);
        final OutputStream output = resp.getOutputStream();
        final JsonGenerator json = factory.createJsonGenerator(output, JsonEncoding.UTF8);
        if (pretty) {
            json.useDefaultPrettyPrinter();
        }
        json.writeStartObject();
        {
            if (showJvmMetrics && ("jvm".equals(classPrefix) || classPrefix == null)) {
                writeVmMetrics(json);
            }

            writeRegularMetrics(json, classPrefix, showFullSamples);
        }
        json.writeEndObject();
        json.close();
    }

    private void writeVmMetrics(JsonGenerator json) throws IOException {
        json.writeFieldName("jvm");
        json.writeStartObject();
        {
            json.writeFieldName("vm");
            json.writeStartObject();
            {
                json.writeStringField("name", vm.name());
                json.writeStringField("version", vm.version());
            }
            json.writeEndObject();

            json.writeFieldName("memory");
            json.writeStartObject();
            {
                json.writeNumberField("totalInit", vm.totalInit());
                json.writeNumberField("totalUsed", vm.totalUsed());
                json.writeNumberField("totalMax", vm.totalMax());
                json.writeNumberField("totalCommitted", vm.totalCommitted());

                json.writeNumberField("heapInit", vm.heapInit());
                json.writeNumberField("heapUsed", vm.heapUsed());
                json.writeNumberField("heapMax", vm.heapMax());
                json.writeNumberField("heapCommitted", vm.heapCommitted());

                json.writeNumberField("heap_usage", vm.heapUsage());
                json.writeNumberField("non_heap_usage", vm.nonHeapUsage());
                json.writeFieldName("memory_pool_usages");
                json.writeStartObject();
                {
                    for (Map.Entry<String, Double> pool : vm.memoryPoolUsage().entrySet()) {
                        json.writeNumberField(pool.getKey(), pool.getValue());
                    }
                }
                json.writeEndObject();
            }
            json.writeEndObject();

            final Map<String, VirtualMachineMetrics.BufferPoolStats> bufferPoolStats = vm.getBufferPoolStats();
            if (!bufferPoolStats.isEmpty()) {
                json.writeFieldName("buffers");
                json.writeStartObject();
                {
                    json.writeFieldName("direct");
                    json.writeStartObject();
                    {
                        json.writeNumberField("count", bufferPoolStats.get("direct").getCount());
                        json.writeNumberField("memoryUsed", bufferPoolStats.get("direct").getMemoryUsed());
                        json.writeNumberField("totalCapacity", bufferPoolStats.get("direct").getTotalCapacity());
                    }
                    json.writeEndObject();

                    json.writeFieldName("mapped");
                    json.writeStartObject();
                    {
                        json.writeNumberField("count", bufferPoolStats.get("mapped").getCount());
                        json.writeNumberField("memoryUsed", bufferPoolStats.get("mapped").getMemoryUsed());
                        json.writeNumberField("totalCapacity", bufferPoolStats.get("mapped").getTotalCapacity());
                    }
                    json.writeEndObject();
                }
                json.writeEndObject();
            }


            json.writeNumberField("daemon_thread_count", vm.daemonThreadCount());
            json.writeNumberField("thread_count", vm.threadCount());
            json.writeNumberField("current_time", clock.time());
            json.writeNumberField("uptime", vm.uptime());
            json.writeNumberField("fd_usage", vm.fileDescriptorUsage());

            json.writeFieldName("thread-states");
            json.writeStartObject();
            {
                for (Map.Entry<Thread.State, Double> entry : vm.threadStatePercentages()
                                                               .entrySet()) {
                    json.writeNumberField(entry.getKey().toString().toLowerCase(),
                                          entry.getValue());
                }
            }
            json.writeEndObject();

            json.writeFieldName("garbage-collectors");
            json.writeStartObject();
            {
                for (Map.Entry<String, VirtualMachineMetrics.GarbageCollectorStats> entry : vm.garbageCollectors()
                                                                                              .entrySet()) {
                    json.writeFieldName(entry.getKey());
                    json.writeStartObject();
                    {
                        final VirtualMachineMetrics.GarbageCollectorStats gc = entry.getValue();
                        json.writeNumberField("runs", gc.getRuns());
                        json.writeNumberField("time", gc.getTime(TimeUnit.MILLISECONDS));
                    }
                    json.writeEndObject();
                }
            }
            json.writeEndObject();
        }
        json.writeEndObject();
    }

    public void writeRegularMetrics(JsonGenerator json, String classPrefix, boolean showFullSamples) throws IOException {
        for (Map.Entry<String, SortedMap<MetricName, Metric>> entry : registry.groupedMetrics().entrySet()) {
            if (classPrefix == null || entry.getKey().startsWith(classPrefix)) {
                json.writeFieldName(entry.getKey());
                json.writeStartObject();
                {
                    for (Map.Entry<MetricName, Metric> subEntry : entry.getValue().entrySet()) {
                        json.writeFieldName(subEntry.getKey().getName());
                        try {
                            subEntry.getValue()
                                    .processWith(this,
                                                 subEntry.getKey(),
                                                 new Context(json, showFullSamples));
                        } catch (Exception e) {
                            LOGGER.warn("Error writing out {}", subEntry.getKey(), e);
                        }
                    }
                }
                json.writeEndObject();
            }
        }
    }

    @Override
    public void processHistogram(MetricName name, Histogram histogram, Context context) throws Exception {
        final JsonGenerator json = context.json;
        json.writeStartObject();
        {
            json.writeStringField("type", "histogram");
            json.writeNumberField("count", histogram.count());
            writeSummarizable(histogram, json);
            writeSampling(histogram, json);

            if (context.showFullSamples) {
                json.writeObjectField("values", histogram.getSnapshot().getValues());
            }
        }
        json.writeEndObject();
    }

    @Override
    public void processCounter(MetricName name, Counter counter, Context context) throws Exception {
        final JsonGenerator json = context.json;
        json.writeStartObject();
        {
            json.writeStringField("type", "counter");
            json.writeNumberField("count", counter.count());
        }
        json.writeEndObject();
    }

    @Override
    public void processGauge(MetricName name, Gauge<?> gauge, Context context) throws Exception {
        final JsonGenerator json = context.json;
        json.writeStartObject();
        {
            json.writeStringField("type", "gauge");
            json.writeObjectField("value", evaluateGauge(gauge));
        }
        json.writeEndObject();
    }

    @Override
    public void processMeter(MetricName name, Metered meter, Context context) throws Exception {
        final JsonGenerator json = context.json;
        json.writeStartObject();
        {
            json.writeStringField("type", "meter");
            json.writeStringField("event_type", meter.eventType());
            writeMeteredFields(meter, json);
        }
        json.writeEndObject();
    }

    @Override
    public void processTimer(MetricName name, Timer timer, Context context) throws Exception {
        final JsonGenerator json = context.json;
        json.writeStartObject();
        {
            json.writeStringField("type", "timer");
            json.writeFieldName("duration");
            json.writeStartObject();
            {
                json.writeStringField("unit", timer.durationUnit().toString().toLowerCase());
                writeSummarizable(timer, json);
                writeSampling(timer, json);
                if (context.showFullSamples) {
                    json.writeObjectField("values", timer.getSnapshot().getValues());
                }
            }
            json.writeEndObject();

            json.writeFieldName("rate");
            json.writeStartObject();
            {
                writeMeteredFields(timer, json);
            }
            json.writeEndObject();
        }
        json.writeEndObject();
    }

    private static Object evaluateGauge(Gauge<?> gauge) {
        try {
            return gauge.value();
        } catch (RuntimeException e) {
            LOGGER.warn("Error evaluating gauge", e);
            return "error reading gauge: " + e.getMessage();
        }
    }

    private static void writeSummarizable(Summarizable metric, JsonGenerator json) throws IOException {
        json.writeNumberField("min", metric.min());
        json.writeNumberField("max", metric.max());
        json.writeNumberField("mean", metric.mean());
        json.writeNumberField("std_dev", metric.stdDev());
    }

    private static void writeSampling(Sampling metric, JsonGenerator json) throws IOException {
        final Snapshot snapshot = metric.getSnapshot();
        json.writeNumberField("median", snapshot.getMedian());
        json.writeNumberField("p75", snapshot.get75thPercentile());
        json.writeNumberField("p95", snapshot.get95thPercentile());
        json.writeNumberField("p98", snapshot.get98thPercentile());
        json.writeNumberField("p99", snapshot.get99thPercentile());
        json.writeNumberField("p999", snapshot.get999thPercentile());
    }

    private static void writeMeteredFields(Metered metered, JsonGenerator json) throws IOException {
        json.writeStringField("unit", metered.rateUnit().toString().toLowerCase());
        json.writeNumberField("count", metered.count());
        json.writeNumberField("mean", metered.meanRate());
        json.writeNumberField("m1", metered.oneMinuteRate());
        json.writeNumberField("m5", metered.fiveMinuteRate());
        json.writeNumberField("m15", metered.fifteenMinuteRate());
    }
}
