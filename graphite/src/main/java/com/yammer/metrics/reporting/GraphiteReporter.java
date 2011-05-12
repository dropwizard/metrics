package com.yammer.metrics.reporting;

import com.yammer.metrics.Metrics;
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

import static com.yammer.metrics.core.VirtualMachineMetrics.*;

/**
 * A simple reporter which sends out application metrics to a
 * <a href="http://graphite.wikidot.com/faq">Graphite</a> server periodically.
 *
 * @author Mahesh Tiyyagura <tmahesh@gmail.com>
 */
public class GraphiteReporter implements Runnable {
    private static final ScheduledExecutorService TICK_THREAD = Utils.newScheduledThreadPool(1, "graphite-reporter");
    private final Writer writer;
    private final String prefix;

    /**
     * Enables the graphite reporter to send data to graphite server with the
     * specified period.
     *
     * @param period the period between successive outputs
     * @param unit   the time unit of {@code period}
     * @param host   the host name of graphite server (carbon-cache agent)
     * @param port   the port number on which the graphite server is listening
     */
    public static void enable(long period, TimeUnit unit, String host, int port) {
        enable(period, unit, host, port, null);
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
        try {
            final GraphiteReporter reporter = new GraphiteReporter(host, port, prefix);
            reporter.start(period, unit);
        } catch (Exception e) {
            e.printStackTrace();
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
        Socket socket = new Socket(host, port);
        this.writer = new OutputStreamWriter(socket.getOutputStream());
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
        TICK_THREAD.scheduleAtFixedRate(this, period, period, unit);
    }

    @Override
    public void run() {
        try {
            long epoch = System.currentTimeMillis() / 1000;
            printVmMetrics(epoch);
            printRegularMetrics(epoch);
            writer.flush();
        } catch (Exception e) {
            e.printStackTrace();
            try {
                writer.flush();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    private void printRegularMetrics(long epoch) {
        for (Entry<String, Map<String, Metric>> entry : Utils.sortMetrics(Metrics.allMetrics()).entrySet()) {
            for (Entry<String, Metric> subEntry : entry.getValue().entrySet()) {
                final String simpleName = entry.getKey() + "." + subEntry.getKey();
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
                        ignored.printStackTrace();
                    }
                }
            }
        }
    }

    private void sendToGraphite(String data) {
        try {
            writer.write(prefix);
            writer.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void printGauge(GaugeMetric<?> gauge, String name, long epoch) {
        sendToGraphite(String.format("%s.%s %s %d\n", name, "value", gauge.value(), epoch));
    }

    private void printCounter(CounterMetric counter, String name, long epoch) {
        sendToGraphite(String.format("%s.%s %d %d\n", name, "count", counter.count(), epoch));
    }

    private void printMetered(Metered meter, String name, long epoch) {
        StringBuffer lines = new StringBuffer();
        lines.append(String.format("%s.%s %d %d\n",    name, "count",        meter.count(), epoch));
        lines.append(String.format("%s.%s %2.2f %d\n", name, "meanRate",     meter.meanRate(), epoch));
        lines.append(String.format("%s.%s %2.2f %d\n", name, "1MinuteRate",  meter.oneMinuteRate(), epoch));
        lines.append(String.format("%s.%s %2.2f %d\n", name, "5MinuteRate",  meter.fiveMinuteRate(), epoch));
        lines.append(String.format("%s.%s %2.2f %d\n", name, "15MinuteRate", meter.fifteenMinuteRate(), epoch));
        sendToGraphite(lines.toString());
    }

    private void printHistogram(HistogramMetric histogram, String name, long epoch) {
        final double[] percentiles = histogram.percentiles(0.5, 0.75, 0.95, 0.98, 0.99, 0.999);
        StringBuffer lines = new StringBuffer();
        lines.append(String.format("%s.%s %2.2f %d\n", name, "min",           histogram.min(), epoch));
        lines.append(String.format("%s.%s %2.2f %d\n", name, "max",           histogram.max(), epoch));
        lines.append(String.format("%s.%s %2.2f %d\n", name, "mean",          histogram.mean(), epoch));
        lines.append(String.format("%s.%s %2.2f %d\n", name, "stddev",        histogram.stdDev(), epoch));
        lines.append(String.format("%s.%s %2.2f %d\n", name, "median",        percentiles[0], epoch));
        lines.append(String.format("%s.%s %2.2f %d\n", name, "75percentile",  percentiles[1], epoch));
        lines.append(String.format("%s.%s %2.2f %d\n", name, "95percentile",  percentiles[2], epoch));
        lines.append(String.format("%s.%s %2.2f %d\n", name, "98percentile",  percentiles[3], epoch));
        lines.append(String.format("%s.%s %2.2f %d\n", name, "99percentile",  percentiles[4], epoch));
        lines.append(String.format("%s.%s %2.2f %d\n", name, "999percentile", percentiles[5], epoch));

        sendToGraphite(lines.toString());
    }

    private void printTimer(TimerMetric timer, String name, long epoch) {
        printMetered(timer, name, epoch);

        final double[] percentiles = timer.percentiles(0.5, 0.75, 0.95, 0.98, 0.99, 0.999);

        StringBuffer lines = new StringBuffer();
        lines.append(String.format("%s.%s %2.2f %d\n", name, "min",           timer.min(), epoch));
        lines.append(String.format("%s.%s %2.2f %d\n", name, "max",           timer.max(), epoch));
        lines.append(String.format("%s.%s %2.2f %d\n", name, "mean",          timer.mean(), epoch));
        lines.append(String.format("%s.%s %2.2f %d\n", name, "stddev",        timer.stdDev(), epoch));
        lines.append(String.format("%s.%s %2.2f %d\n", name, "median",        percentiles[0], epoch));
        lines.append(String.format("%s.%s %2.2f %d\n", name, "75percentile",  percentiles[1], epoch));
        lines.append(String.format("%s.%s %2.2f %d\n", name, "95percentile",  percentiles[2], epoch));
        lines.append(String.format("%s.%s %2.2f %d\n", name, "98percentile",  percentiles[3], epoch));
        lines.append(String.format("%s.%s %2.2f %d\n", name, "99percentile",  percentiles[4], epoch));
        lines.append(String.format("%s.%s %2.2f %d\n", name, "999percentile", percentiles[5], epoch));
        sendToGraphite(lines.toString());
    }

    private void printDoubleField(String name, double value, long epoch) {
        sendToGraphite(String.format("%s.%s %2.2f %d\n", name, value, epoch));
    }

    private void printVmMetrics(long epoch) throws IOException {
        printDoubleField("jvm.memory.heap_usage", heapUsage(), epoch);
        printDoubleField("jvm.memory.heap_usage", nonHeapUsage(), epoch);
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

        for (Entry<String, TimerMetric> entry : gcDurations().entrySet()) {
            printTimer(entry.getValue(), "jvm.gc.duration." + entry.getKey(), epoch);
        }

        for (Entry<String, MeterMetric> entry : gcThroughputs().entrySet()) {
            printMetered(entry.getValue(), "jvm.gc.throughput." + entry.getKey(), epoch);
        }
    }
}
