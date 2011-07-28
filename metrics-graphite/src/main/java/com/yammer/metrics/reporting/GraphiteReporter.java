package com.yammer.metrics.reporting;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.MetricsRegistry;
import com.yammer.metrics.core.*;
import com.yammer.metrics.util.Utils;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.Thread.State;
import java.net.Socket;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.yammer.metrics.core.VirtualMachineMetrics.*;

/**
 * A simple reporter which sends out application metrics to a
 * <a href="http://graphite.wikidot.com/faq">Graphite</a> server periodically.
 *
 * @author Mahesh Tiyyagura <tmahesh@gmail.com>
 */
public class GraphiteReporter implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(GraphiteReporter.class);
    private final ScheduledExecutorService tickThread;
    private final MetricsRegistry metricsRegistry;
    private final String host;
    private final int port;
    private Writer writer;
    private final String prefix;

    /**
     * Enables the graphite reporter to send data for the default metrics registry
     * to graphite server with the specified period.
     *
     * @param period the period between successive outputs
     * @param unit   the time unit of {@code period}
     * @param host   the host name of graphite server (carbon-cache agent)
     * @param port   the port number on which the graphite server is listening
     */
    public static void enable(long period, TimeUnit unit, String host, int port) {
        enable(Metrics.defaultRegistry(), period, unit, host, port);
    }

    /**
     * Enables the graphite reporter to send data for the given metrics registry
     * to graphite server with the specified period.
     *
     * @param metricsRegistry the metrics registry
     * @param period          the period between successive outputs
     * @param unit            the time unit of {@code period}
     * @param host            the host name of graphite server (carbon-cache agent)
     * @param port            the port number on which the graphite server is listening
     */
    public static void enable(MetricsRegistry metricsRegistry, long period, TimeUnit unit, String host, int port) {
        enable(metricsRegistry, period, unit, host, port, null);
    }

    /**
     * Enables the graphite reporter to send data to graphite server with the
     * specified period.
     *
     * @param period the period between successive outputs
     * @param unit   the time unit of {@code period}
     * @param host   the host name of graphite server (carbon-cache agent)
     * @param port   the port number on which the graphite server is listening
     * @param prefix the string which is prepended to all metric names
     */
    public static void enable(long period, TimeUnit unit, String host, int port, String prefix) {
        enable(Metrics.defaultRegistry(), period, unit, host, port, prefix);
    }

    /**
     * Enables the graphite reporter to send data to graphite server with the
     * specified period.
     *
     * @param metricsRegistry the metrics registry
     * @param period          the period between successive outputs
     * @param unit            the time unit of {@code period}
     * @param host            the host name of graphite server (carbon-cache agent)
     * @param port            the port number on which the graphite server is listening
     * @param prefix          the string which is prepended to all metric names
     */
    public static void enable(MetricsRegistry metricsRegistry, long period, TimeUnit unit, String host, int port, String prefix) {
        try {
            final GraphiteReporter reporter = new GraphiteReporter(metricsRegistry, host, port, prefix);
            reporter.start(period, unit);
        } catch (Exception e) {
            log.error("Error creating/starting Graphite reporter:", e);
        }
    }

    /**
     * Creates a new {@link GraphiteReporter}.
     *
     * @param host   is graphite server
     * @param port   is port on which graphite server is running
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
     * @param host            is graphite server
     * @param port            is port on which graphite server is running
     * @param prefix          is prepended to all names reported to graphite
     * @throws IOException if there is an error connecting to the Graphite server
     */
    public GraphiteReporter(MetricsRegistry metricsRegistry, String host, int port, String prefix) throws IOException {
        this.tickThread = Utils.newScheduledThreadPool(1, "graphite-reporter" + System.identityHashCode(this));
        this.metricsRegistry = metricsRegistry;
        this.host = host;
        this.port = port;
        if (prefix != null) {
            // Pre-append the "." so that we don't need to make anything conditional later.
            this.prefix = prefix + ".";
        } else {
            this.prefix = "";
        }
    }

    /**
     * Starts sending output to graphite server.
     *
     * @param period the period between successive displays
     * @param unit   the time unit of {@code period}
     */
    public void start(long period, TimeUnit unit) {
        tickThread.scheduleAtFixedRate(this, period, period, unit);
    }

    @Override
    public void run() {
        Socket socket = null;
        try {
            socket = new Socket(host, port);
            writer = new OutputStreamWriter(socket.getOutputStream());
            long epoch = System.currentTimeMillis() / 1000;
            printVmMetrics(epoch);
            printRegularMetrics(epoch);
            writer.flush();
        } catch (Exception e) {
            log.error("Error:", e);
            if (writer != null) {
                try {
                    writer.flush();
                } catch (IOException e1) {
                    log.error("Error while flushing writer:", e1);
                }
            }
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    log.error("Error while closing socket:", e);
                }
            }
            writer = null;
        }
    }

    private void printRegularMetrics(long epoch) {
        for (Entry<String, Map<String, Metric>> entry : Utils.sortMetrics(metricsRegistry.allMetrics()).entrySet()) {
            for (Entry<String, Metric> subEntry : entry.getValue().entrySet()) {
                final String simpleName = (entry.getKey() + "." + subEntry.getKey()).replaceAll(" ", "_");
                final Metric metric = subEntry.getValue();
                if (metric != null) {
                    try {
                        if (metric instanceof GaugeMetric<?>) {
                            printGauge((GaugeMetric<?>) metric, simpleName, epoch);
                        } else if (metric instanceof CounterMetric) {
                            printCounter((CounterMetric) metric, simpleName, epoch);
                        } else if (metric instanceof HistogramMetric) {
                            printHistogram((HistogramMetric) metric, simpleName, epoch);
                        } else if (metric instanceof MeterMetric) {
                            printMetered((MeterMetric) metric, simpleName, epoch);
                        } else if (metric instanceof TimerMetric) {
                            printTimer((TimerMetric) metric, simpleName, epoch);
                        }
                    } catch (Exception ignored) {
                        log.error("Error printing regular metrics:", ignored);
                    }
                }
            }
        }
    }

    private void sendToGraphite(String data) {
        try {
            writer.write(data);
        } catch (IOException e) {
            log.error("Error sending to Graphite:", e);
        }
    }

    private void printGauge(GaugeMetric<?> gauge, String name, long epoch) {
        sendToGraphite(String.format("%s%s.%s %s %d\n", prefix, name, "value", gauge.value(), epoch));
    }

    private void printCounter(CounterMetric counter, String name, long epoch) {
        sendToGraphite(String.format("%s%s.%s %d %d\n", prefix, name, "count", counter.count(), epoch));
    }

    private void printMetered(Metered meter, String name, long epoch) {
        StringBuffer lines = new StringBuffer();
        lines.append(String.format("%s%s.%s %d %d\n",    prefix, name, "count",        meter.count(), epoch));
        lines.append(String.format("%s%s.%s %2.2f %d\n", prefix, name, "meanRate",     meter.meanRate(), epoch));
        lines.append(String.format("%s%s.%s %2.2f %d\n", prefix, name, "1MinuteRate",  meter.oneMinuteRate(), epoch));
        lines.append(String.format("%s%s.%s %2.2f %d\n", prefix, name, "5MinuteRate",  meter.fiveMinuteRate(), epoch));
        lines.append(String.format("%s%s.%s %2.2f %d\n", prefix, name, "15MinuteRate", meter.fifteenMinuteRate(), epoch));
        sendToGraphite(lines.toString());
    }

    private void printHistogram(HistogramMetric histogram, String name, long epoch) {
        final double[] percentiles = histogram.percentiles(0.5, 0.75, 0.95, 0.98, 0.99, 0.999);
        StringBuffer lines = new StringBuffer();
        lines.append(String.format("%s%s.%s %2.2f %d\n", prefix, name, "min",           histogram.min(), epoch));
        lines.append(String.format("%s%s.%s %2.2f %d\n", prefix, name, "max",           histogram.max(), epoch));
        lines.append(String.format("%s%s.%s %2.2f %d\n", prefix, name, "mean",          histogram.mean(), epoch));
        lines.append(String.format("%s%s.%s %2.2f %d\n", prefix, name, "stddev",        histogram.stdDev(), epoch));
        lines.append(String.format("%s%s.%s %2.2f %d\n", prefix, name, "median",        percentiles[0], epoch));
        lines.append(String.format("%s%s.%s %2.2f %d\n", prefix, name, "75percentile",  percentiles[1], epoch));
        lines.append(String.format("%s%s.%s %2.2f %d\n", prefix, name, "95percentile",  percentiles[2], epoch));
        lines.append(String.format("%s%s.%s %2.2f %d\n", prefix, name, "98percentile",  percentiles[3], epoch));
        lines.append(String.format("%s%s.%s %2.2f %d\n", prefix, name, "99percentile",  percentiles[4], epoch));
        lines.append(String.format("%s%s.%s %2.2f %d\n", prefix, name, "999percentile", percentiles[5], epoch));

        sendToGraphite(lines.toString());
    }

    private void printTimer(TimerMetric timer, String name, long epoch) {
        printMetered(timer, name, epoch);

        final double[] percentiles = timer.percentiles(0.5, 0.75, 0.95, 0.98, 0.99, 0.999);

        StringBuffer lines = new StringBuffer();
        lines.append(String.format("%s%s.%s %2.2f %d\n", prefix, name, "min",           timer.min(), epoch));
        lines.append(String.format("%s%s.%s %2.2f %d\n", prefix, name, "max",           timer.max(), epoch));
        lines.append(String.format("%s%s.%s %2.2f %d\n", prefix, name, "mean",          timer.mean(), epoch));
        lines.append(String.format("%s%s.%s %2.2f %d\n", prefix, name, "stddev",        timer.stdDev(), epoch));
        lines.append(String.format("%s%s.%s %2.2f %d\n", prefix, name, "median",        percentiles[0], epoch));
        lines.append(String.format("%s%s.%s %2.2f %d\n", prefix, name, "75percentile",  percentiles[1], epoch));
        lines.append(String.format("%s%s.%s %2.2f %d\n", prefix, name, "95percentile",  percentiles[2], epoch));
        lines.append(String.format("%s%s.%s %2.2f %d\n", prefix, name, "98percentile",  percentiles[3], epoch));
        lines.append(String.format("%s%s.%s %2.2f %d\n", prefix, name, "99percentile",  percentiles[4], epoch));
        lines.append(String.format("%s%s.%s %2.2f %d\n", prefix, name, "999percentile", percentiles[5], epoch));
        sendToGraphite(lines.toString());
    }

    private void printDoubleField(String name, double value, long epoch) {
        sendToGraphite(String.format("%s%s %2.2f %d\n", prefix, name, value, epoch));
    }

    private void printLongField(String name, long value, long epoch) {
        sendToGraphite(String.format("%s%s %d %d\n", prefix, name, value, epoch));
    }

    private void printVmMetrics(long epoch) throws IOException {
        printDoubleField("jvm.memory.heap_usage", heapUsage(), epoch);
        printDoubleField("jvm.memory.non_heap_usage", nonHeapUsage(), epoch);
        for (Entry<String, Double> pool : memoryPoolUsage().entrySet()) {
            printDoubleField("jvm.memory.memory_pool_usages." + pool.getKey(), pool.getValue(), epoch);
        }

        printDoubleField("jvm.daemon_thread_count", daemonThreadCount(), epoch);
        printDoubleField("jvm.thread_count", threadCount(), epoch);
        printDoubleField("jvm.uptime", uptime(), epoch);
        printDoubleField("jvm.fd_usage", fileDescriptorUsage(), epoch);

        for (Entry<State, Double> entry : threadStatePercentages().entrySet()) {
            printDoubleField("jvm.thread-states." + entry.getKey().toString().toLowerCase(), entry.getValue(), epoch);
        }

        for (Entry<String, GarbageCollector> entry : garbageCollectors().entrySet()) {
            printLongField("jvm.gc." + entry.getKey() + ".time", entry.getValue().getTime(TimeUnit.MILLISECONDS), epoch);
            printLongField("jvm.gc." + entry.getKey() + ".runs", entry.getValue().getRuns(), epoch);
        }
    }
}
