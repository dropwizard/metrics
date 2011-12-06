package com.yammer.metrics.reporting;

import static com.yammer.metrics.core.VirtualMachineMetrics.daemonThreadCount;
import static com.yammer.metrics.core.VirtualMachineMetrics.fileDescriptorUsage;
import static com.yammer.metrics.core.VirtualMachineMetrics.garbageCollectors;
import static com.yammer.metrics.core.VirtualMachineMetrics.heapUsage;
import static com.yammer.metrics.core.VirtualMachineMetrics.memoryPoolUsage;
import static com.yammer.metrics.core.VirtualMachineMetrics.nonHeapUsage;
import static com.yammer.metrics.core.VirtualMachineMetrics.threadCount;
import static com.yammer.metrics.core.VirtualMachineMetrics.threadStatePercentages;
import static com.yammer.metrics.core.VirtualMachineMetrics.uptime;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.Thread.State;
import java.net.Socket;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Clock;
import com.yammer.metrics.core.CounterMetric;
import com.yammer.metrics.core.GaugeMetric;
import com.yammer.metrics.core.HistogramMetric;
import com.yammer.metrics.core.MeterMetric;
import com.yammer.metrics.core.Metered;
import com.yammer.metrics.core.Metric;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricsProcessor;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.core.TimerMetric;
import com.yammer.metrics.core.VirtualMachineMetrics;
import com.yammer.metrics.core.VirtualMachineMetrics.GarbageCollector;
import com.yammer.metrics.util.MetricPredicate;
import com.yammer.metrics.util.Utils;

/**
 * A simple reporter which sends out application metrics to a <a href="http://graphite.wikidot.com/faq">Graphite</a>
 * server periodically.
 */
public class GraphiteReporter extends AbstractPollingReporter implements MetricsProcessor<GraphiteRendererContext> {
    private static final Logger LOG = LoggerFactory.getLogger(GraphiteReporter.class);
    private final String prefix;
    private final MetricPredicate predicate;
    private final Locale locale = Locale.US;
    private Clock clock;
    private final SocketProvider socketProvider;
    private final Map<Class<?>, GraphiteMetricRenderer<? extends Metric>> renderers = new HashMap<Class<?>, GraphiteMetricRenderer<? extends Metric>>();
    public boolean printVMMetrics = true;

    /**
     * Enables the graphite reporter to send data for the default metrics registry to graphite server with the specified
     * period.
     * 
     * @param period the period between successive outputs
     * @param unit the time unit of {@code period}
     * @param host the host name of graphite server (carbon-cache agent)
     * @param port the port number on which the graphite server is listening
     */
    public static void enable(long period, TimeUnit unit, String host, int port) {
        enable(Metrics.defaultRegistry(), period, unit, host, port);
    }

    /**
     * Enables the graphite reporter to send data for the given metrics registry to graphite server with the specified
     * period.
     * 
     * @param metricsRegistry the metrics registry
     * @param period the period between successive outputs
     * @param unit the time unit of {@code period}
     * @param host the host name of graphite server (carbon-cache agent)
     * @param port the port number on which the graphite server is listening
     */
    public static void enable(MetricsRegistry metricsRegistry, long period, TimeUnit unit, String host, int port) {
        enable(metricsRegistry, period, unit, host, port, null);
    }

    /**
     * Enables the graphite reporter to send data to graphite server with the specified period.
     * 
     * @param period the period between successive outputs
     * @param unit the time unit of {@code period}
     * @param host the host name of graphite server (carbon-cache agent)
     * @param port the port number on which the graphite server is listening
     * @param prefix the string which is prepended to all metric names
     */
    public static void enable(long period, TimeUnit unit, String host, int port, String prefix) {
        enable(Metrics.defaultRegistry(), period, unit, host, port, prefix);
    }

    /**
     * Enables the graphite reporter to send data to graphite server with the specified period.
     * 
     * @param metricsRegistry the metrics registry
     * @param period the period between successive outputs
     * @param unit the time unit of {@code period}
     * @param host the host name of graphite server (carbon-cache agent)
     * @param port the port number on which the graphite server is listening
     * @param prefix the string which is prepended to all metric names
     */
    public static void enable(MetricsRegistry metricsRegistry, long period, TimeUnit unit, String host, int port, String prefix) {
        enable(metricsRegistry, period, unit, host, port, prefix, MetricPredicate.ALL);
    }

    /**
     * Enables the graphite reporter to send data to graphite server with the specified period.
     * 
     * @param metricsRegistry the metrics registry
     * @param period the period between successive outputs
     * @param unit the time unit of {@code period}
     * @param host the host name of graphite server (carbon-cache agent)
     * @param port the port number on which the graphite server is listening
     * @param prefix the string which is prepended to all metric names
     * @param predicate filters metrics to be reported
     */
    public static void enable(MetricsRegistry metricsRegistry, long period, TimeUnit unit, String host, int port, String prefix, MetricPredicate predicate) {
        try {
            final GraphiteReporter reporter = new GraphiteReporter(metricsRegistry, prefix, predicate, new DefaultSocketProvider(host, port), Clock.DEFAULT);
            reporter.start(period, unit);
        } catch (Exception e) {
            LOG.error("Error creating/starting Graphite reporter:", e);
        }
    }

    /**
     * Creates a new {@link GraphiteReporter}.
     * 
     * @param host is graphite server
     * @param port is port on which graphite server is running
     * @param prefix is prepended to all names reported to graphite
     * @throws IOException if there is an error connecting to the Graphite server
     */
    public GraphiteReporter(String host, int port, String prefix) throws IOException {
        this(Metrics.defaultRegistry(), host, port, prefix);
    }

    /**
     * Creates a new {@link GraphiteReporter}.
     * 
     * @param metricsRegistry the metrics registry
     * @param host is graphite server
     * @param port is port on which graphite server is running
     * @param prefix is prepended to all names reported to graphite
     * @throws IOException if there is an error connecting to the Graphite server
     */
    public GraphiteReporter(MetricsRegistry metricsRegistry, String host, int port, String prefix) throws IOException {
        this(metricsRegistry, prefix, MetricPredicate.ALL, new DefaultSocketProvider(host, port), Clock.DEFAULT);
    }

    /**
     * Creates a new {@link GraphiteReporter}.
     * 
     * @param metricsRegistry the metrics registry
     * @param prefix is prepended to all names reported to graphite
     * @param predicate filters metrics to be reported
     * @throws IOException if there is an error connecting to the Graphite server
     */
    public GraphiteReporter(MetricsRegistry metricsRegistry, String prefix, MetricPredicate predicate, SocketProvider socketProvider, Clock clock) throws IOException {
        super(metricsRegistry, "graphite-reporter");
        this.socketProvider = socketProvider;

        this.clock = clock;

        if (prefix != null) {
            // Pre-append the "." so that we don't need to make anything conditional later.
            this.prefix = prefix + ".";
        } else {
            this.prefix = "";
        }
        this.predicate = predicate;
        registerDefaultRenderers();
    }

    private void registerDefaultRenderers() {
        this.renderers.put(CounterMetric.class, new GraphiteMetricRenderer<CounterMetric>() {
            @Override
            public void renderMetric(MetricName metricName, CounterMetric metric, GraphiteRendererContext context) {
                sendToGraphite(String.format(context.locale, "%s%s.%s %d %d\n", context.prefix, sanitizeName(metricName), "count", metric.count(), context.epoch), context);
            }
        });

        this.renderers.put(GaugeMetric.class, new GraphiteMetricRenderer<GaugeMetric<?>>() {
            @Override
            public void renderMetric(MetricName metricName, GaugeMetric<?> metric, GraphiteRendererContext context) {
                sendToGraphite(String.format(context.locale, "%s%s.%s %s %d\n", context.prefix, sanitizeName(metricName), "value", metric.value(), context.epoch), context);
            }
        });

        this.renderers.put(MeterMetric.class, new GraphiteMetricRenderer<Metered>() {
            @Override
            public void renderMetric(MetricName metricName, Metered metric, GraphiteRendererContext context) {
                final String sanitizedName = sanitizeName(metricName);
                final StringBuilder lines = new StringBuilder();
                lines.append(String.format(context.locale, "%s%s.%s %d %d\n", context.prefix, sanitizedName, "count", metric.count(), context.epoch));
                lines.append(String.format(context.locale, "%s%s.%s %2.2f %d\n", context.prefix, sanitizedName, "meanRate", metric.meanRate(), context.epoch));
                lines.append(String.format(context.locale, "%s%s.%s %2.2f %d\n", context.prefix, sanitizedName, "1MinuteRate", metric.oneMinuteRate(), context.epoch));
                lines.append(String.format(context.locale, "%s%s.%s %2.2f %d\n", context.prefix, sanitizedName, "5MinuteRate", metric.fiveMinuteRate(), context.epoch));
                lines.append(String.format(context.locale, "%s%s.%s %2.2f %d\n", context.prefix, sanitizedName, "15MinuteRate", metric.fifteenMinuteRate(), context.epoch));

                sendToGraphite(lines.toString(), context);
            }
        });

        this.renderers.put(HistogramMetric.class, new GraphiteMetricRenderer<HistogramMetric>() {
            @Override
            public void renderMetric(MetricName metricName, HistogramMetric metric, GraphiteRendererContext context) {
                final String sanitizedName = sanitizeName(metricName);
                final Double[] percentiles = metric.percentiles(0.5, 0.75, 0.95, 0.98, 0.99, 0.999);
                final StringBuilder lines = new StringBuilder();
                lines.append(String.format(context.locale, "%s%s.%s %2.2f %d\n", context.prefix, sanitizedName, "min", metric.min(), context.epoch));
                lines.append(String.format(context.locale, "%s%s.%s %2.2f %d\n", context.prefix, sanitizedName, "max", metric.max(), context.epoch));
                lines.append(String.format(context.locale, "%s%s.%s %2.2f %d\n", context.prefix, sanitizedName, "mean", metric.mean(), context.epoch));
                lines.append(String.format(context.locale, "%s%s.%s %2.2f %d\n", context.prefix, sanitizedName, "stddev", metric.stdDev(), context.epoch));
                lines.append(String.format(context.locale, "%s%s.%s %2.2f %d\n", context.prefix, sanitizedName, "median", percentiles[0], context.epoch));
                lines.append(String.format(context.locale, "%s%s.%s %2.2f %d\n", context.prefix, sanitizedName, "75percentile", percentiles[1], context.epoch));
                lines.append(String.format(context.locale, "%s%s.%s %2.2f %d\n", context.prefix, sanitizedName, "95percentile", percentiles[2], context.epoch));
                lines.append(String.format(context.locale, "%s%s.%s %2.2f %d\n", context.prefix, sanitizedName, "98percentile", percentiles[3], context.epoch));
                lines.append(String.format(context.locale, "%s%s.%s %2.2f %d\n", context.prefix, sanitizedName, "99percentile", percentiles[4], context.epoch));
                lines.append(String.format(context.locale, "%s%s.%s %2.2f %d\n", context.prefix, sanitizedName, "999percentile", percentiles[5], context.epoch));

                sendToGraphite(lines.toString(), context);
            }
        });

        this.renderers.put(TimerMetric.class, new GraphiteMetricRenderer<TimerMetric>() {
            @Override
            public void renderMetric(MetricName metricName, TimerMetric metric, GraphiteRendererContext context) {
                final String sanitizedName = sanitizeName(metricName);
                final Double[] percentiles = metric.percentiles(0.5, 0.75, 0.95, 0.98, 0.99, 0.999);
                final StringBuilder lines = new StringBuilder();
                lines.append(String.format(context.locale, "%s%s.%s %2.2f %d\n", context.prefix, sanitizedName, "min", metric.min(), context.epoch));
                lines.append(String.format(context.locale, "%s%s.%s %2.2f %d\n", context.prefix, sanitizedName, "max", metric.max(), context.epoch));
                lines.append(String.format(context.locale, "%s%s.%s %2.2f %d\n", context.prefix, sanitizedName, "mean", metric.mean(), context.epoch));
                lines.append(String.format(context.locale, "%s%s.%s %2.2f %d\n", context.prefix, sanitizedName, "stddev", metric.stdDev(), context.epoch));
                lines.append(String.format(context.locale, "%s%s.%s %2.2f %d\n", context.prefix, sanitizedName, "median", percentiles[0], context.epoch));
                lines.append(String.format(context.locale, "%s%s.%s %2.2f %d\n", context.prefix, sanitizedName, "75percentile", percentiles[1], context.epoch));
                lines.append(String.format(context.locale, "%s%s.%s %2.2f %d\n", context.prefix, sanitizedName, "95percentile", percentiles[2], context.epoch));
                lines.append(String.format(context.locale, "%s%s.%s %2.2f %d\n", context.prefix, sanitizedName, "98percentile", percentiles[3], context.epoch));
                lines.append(String.format(context.locale, "%s%s.%s %2.2f %d\n", context.prefix, sanitizedName, "99percentile", percentiles[4], context.epoch));
                lines.append(String.format(context.locale, "%s%s.%s %2.2f %d\n", context.prefix, sanitizedName, "999percentile", percentiles[5], context.epoch));

                sendToGraphite(lines.toString(), context);
            }
        });

        this.renderers.put(VirtualMachineMetrics.class, new GraphiteMetricRenderer<VirtualMachineMetrics>() {
            @Override
            public void renderMetric(MetricName metricName, VirtualMachineMetrics metric, GraphiteRendererContext context) {
                printDoubleField("jvm.memory.heap_usage", heapUsage(), context);
                printDoubleField("jvm.memory.non_heap_usage", nonHeapUsage(), context);
                for (Entry<String, Double> pool : memoryPoolUsage().entrySet()) {
                    printDoubleField("jvm.memory.memory_pool_usages." + pool.getKey(), pool.getValue(), context);
                }

                printDoubleField("jvm.daemon_thread_count", daemonThreadCount(), context);
                printDoubleField("jvm.thread_count", threadCount(), context);
                printDoubleField("jvm.uptime", uptime(), context);
                printDoubleField("jvm.fd_usage", fileDescriptorUsage(), context);

                for (Entry<State, Double> entry : threadStatePercentages().entrySet()) {
                    printDoubleField("jvm.thread-states." + entry.getKey().toString().toLowerCase(), entry.getValue(), context);
                }

                for (Entry<String, GarbageCollector> entry : garbageCollectors().entrySet()) {
                    printLongField("jvm.gc." + entry.getKey() + ".time", entry.getValue().getTime(TimeUnit.MILLISECONDS), context);
                    printLongField("jvm.gc." + entry.getKey() + ".runs", entry.getValue().getRuns(), context);
                }
            }
        });
    }

    /**
     * Register a custom {@link GraphiteMetricRenderer} for the given metric type.
     * 
     * @param metricType the metric type to register a renderer for
     * @param renderer the {@link GraphiteMetricRenderer} to register for the given metric type
     */
    public <T extends Metric, Y extends T> void registerRenderer(Class<Y> metricType, GraphiteMetricRenderer<T> renderer) {
        this.renderers.put(metricType, renderer);
    }

    @Override
    public void run() {
        Socket socket = null;
        Writer writer = null;
        try {
            socket = this.socketProvider.get();
            writer = new OutputStreamWriter(socket.getOutputStream());

            long epoch = clock.time() / 1000;

            GraphiteRendererContext context = new GraphiteRendererContext(this.prefix, epoch, this.locale, writer);

            printVmMetrics(context);
            printRegularMetrics(context);

            writer.flush();
        } catch (Exception e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Error writing to Graphite", e);
            } else {
                LOG.warn("Error writing to Graphite: {}", e.getMessage());
            }
            if (writer != null) {
                try {
                    writer.flush();
                } catch (IOException e1) {
                    LOG.error("Error while flushing writer:", e1);
                }
            }
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    LOG.error("Error while closing socket:", e);
                }
            }
            writer = null;
        }
    }

    @SuppressWarnings("unchecked")
    private void printVmMetrics(GraphiteRendererContext context) {
        if (this.printVMMetrics) {
            GraphiteMetricRenderer<VirtualMachineMetrics> renderer = (GraphiteMetricRenderer<VirtualMachineMetrics>) this.renderers.get(VirtualMachineMetrics.class);
            renderer.renderMetric(null, null, context);
        }
    }

    private void printRegularMetrics(GraphiteRendererContext context) {
        for (Entry<String, Map<MetricName, Metric>> entry : Utils.sortAndFilterMetrics(metricsRegistry.allMetrics(), this.predicate).entrySet()) {
            for (Entry<MetricName, Metric> subEntry : entry.getValue().entrySet()) {
                final Metric metric = subEntry.getValue();

                if (metric != null) {
                    try {
                        metric.processWith(this, subEntry.getKey(), context);
                    } catch (Exception ignored) {
                        LOG.error("Error printing regular metrics:", ignored);
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void processMeter(MetricName name, Metered meter, GraphiteRendererContext context) throws Exception {
        GraphiteMetricRenderer<Metered> renderer = (GraphiteMetricRenderer<Metered>) this.renderers.get(MeterMetric.class);
        renderer.renderMetric(name, meter, context);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void processCounter(MetricName name, CounterMetric counter, GraphiteRendererContext context) throws Exception {
        GraphiteMetricRenderer<CounterMetric> renderer = (GraphiteMetricRenderer<CounterMetric>) this.renderers.get(CounterMetric.class);
        renderer.renderMetric(name, counter, context);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void processHistogram(MetricName name, HistogramMetric histogram, GraphiteRendererContext context) throws Exception {
        GraphiteMetricRenderer<HistogramMetric> renderer = (GraphiteMetricRenderer<HistogramMetric>) this.renderers.get(HistogramMetric.class);
        renderer.renderMetric(name, histogram, context);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void processTimer(MetricName name, TimerMetric timer, GraphiteRendererContext context) throws Exception {
        processMeter(name, timer, context);

        GraphiteMetricRenderer<TimerMetric> renderer = (GraphiteMetricRenderer<TimerMetric>) this.renderers.get(TimerMetric.class);
        renderer.renderMetric(name, timer, context);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void processGauge(MetricName name, GaugeMetric<?> gauge, GraphiteRendererContext context) throws Exception {
        GraphiteMetricRenderer<GaugeMetric<?>> renderer = (GraphiteMetricRenderer<GaugeMetric<?>>) this.renderers.get(GaugeMetric.class);

        renderer.renderMetric(name, gauge, context);
    }

    private static class DefaultSocketProvider implements SocketProvider {

        private final String host;
        private final int port;

        public DefaultSocketProvider(String host, int port) {
            this.host = host;
            this.port = port;

        }

        @Override
        public Socket get() throws Exception {
            return new Socket(this.host, this.port);
        }

    }
}
