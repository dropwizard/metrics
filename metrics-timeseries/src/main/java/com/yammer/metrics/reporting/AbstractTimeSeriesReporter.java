package com.yammer.metrics.reporting;

import com.yammer.metrics.core.*;
import com.yammer.metrics.util.MetricPredicate;
import com.yammer.metrics.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.yammer.metrics.core.VirtualMachineMetrics.*;

/**
 * A simple reporter that can be extended to send data to a time series
 * data warehousing system
 */
abstract public class AbstractTimeSeriesReporter extends AbstractReporter {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractTimeSeriesReporter.class);
    private final Locale locale = Locale.US;
    protected final String host;
    protected final int port;
    protected final MetricPredicate predicate;
    protected final String prefix;
    protected Writer writer;

    protected AbstractTimeSeriesReporter(MetricsRegistry metricsRegistry, String name, String host, int port, MetricPredicate predicate, String prefix) {
        super(metricsRegistry, name);
        this.host = host;
        this.port = port;
        this.predicate = predicate;
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
    @Override
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
            if (LOG.isDebugEnabled()) {
                LOG.debug("Error writing to Socket", e);
            } else {
                LOG.warn("Error writing to Socket: {}", e.getMessage());
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

    private void sendToWarehouse(String data) {
        try {
            writer.write(data);
        } catch (IOException e) {
            LOG.error("Error sending to Socket:", e);
        }
    }

    protected static String sanitizeName(String name) {
      return name.replace(' ', '-');
    }

    protected void printGauge(GaugeMetric<?> gauge, String name, long epoch) {
        sendToWarehouse(String.format(locale, "%s%s.%s %s %d\n", prefix, sanitizeName(name), "value", gauge.value(), epoch));
    }

    protected void printCounter(CounterMetric counter, String name, long epoch) {
        sendToWarehouse(String.format(locale, "%s%s.%s %d %d\n", prefix, sanitizeName(name), "count", counter.count(), epoch));
    }

    protected void printMetered(Metered meter, String name, long epoch) {
        final String sanitizedName = sanitizeName(name);
        final StringBuilder lines = new StringBuilder();
        lines.append(String.format(locale, "%s%s.%s %d %d\n",    prefix, sanitizedName, "count",        meter.count(), epoch));
        lines.append(String.format(locale, "%s%s.%s %2.2f %d\n", prefix, sanitizedName, "meanRate",     meter.meanRate(), epoch));
        lines.append(String.format(locale, "%s%s.%s %2.2f %d\n", prefix, sanitizedName, "1MinuteRate",  meter.oneMinuteRate(), epoch));
        lines.append(String.format(locale, "%s%s.%s %2.2f %d\n", prefix, sanitizedName, "5MinuteRate",  meter.fiveMinuteRate(), epoch));
        lines.append(String.format(locale, "%s%s.%s %2.2f %d\n", prefix, sanitizedName, "15MinuteRate", meter.fifteenMinuteRate(), epoch));
        sendToWarehouse(lines.toString());
    }

    protected void printHistogram(HistogramMetric histogram, String name, long epoch) {
        final String sanitizedName = sanitizeName(name);
        final double[] percentiles = histogram.percentiles(0.5, 0.75, 0.95, 0.98, 0.99, 0.999);
        final StringBuilder lines = new StringBuilder();
        lines.append(String.format(locale, "%s%s.%s %2.2f %d\n", prefix, sanitizedName, "min",           histogram.min(), epoch));
        lines.append(String.format(locale, "%s%s.%s %2.2f %d\n", prefix, sanitizedName, "max",           histogram.max(), epoch));
        lines.append(String.format(locale, "%s%s.%s %2.2f %d\n", prefix, sanitizedName, "mean",          histogram.mean(), epoch));
        lines.append(String.format(locale, "%s%s.%s %2.2f %d\n", prefix, sanitizedName, "stddev",        histogram.stdDev(), epoch));
        lines.append(String.format(locale, "%s%s.%s %2.2f %d\n", prefix, sanitizedName, "median",        percentiles[0], epoch));
        lines.append(String.format(locale, "%s%s.%s %2.2f %d\n", prefix, sanitizedName, "75percentile",  percentiles[1], epoch));
        lines.append(String.format(locale, "%s%s.%s %2.2f %d\n", prefix, sanitizedName, "95percentile",  percentiles[2], epoch));
        lines.append(String.format(locale, "%s%s.%s %2.2f %d\n", prefix, sanitizedName, "98percentile",  percentiles[3], epoch));
        lines.append(String.format(locale, "%s%s.%s %2.2f %d\n", prefix, sanitizedName, "99percentile",  percentiles[4], epoch));
        lines.append(String.format(locale, "%s%s.%s %2.2f %d\n", prefix, sanitizedName, "999percentile", percentiles[5], epoch));

        sendToWarehouse(lines.toString());
    }

    protected void printTimer(TimerMetric timer, String name, long epoch) {
        printMetered(timer, name, epoch);

        final String sanitizedName = sanitizeName(name);
        final double[] percentiles = timer.percentiles(0.5, 0.75, 0.95, 0.98, 0.99, 0.999);

        final StringBuilder lines = new StringBuilder();
        lines.append(String.format(locale, "%s%s.%s %2.2f %d\n", prefix, sanitizedName, "min",           timer.min(), epoch));
        lines.append(String.format(locale, "%s%s.%s %2.2f %d\n", prefix, sanitizedName, "max",           timer.max(), epoch));
        lines.append(String.format(locale, "%s%s.%s %2.2f %d\n", prefix, sanitizedName, "mean",          timer.mean(), epoch));
        lines.append(String.format(locale, "%s%s.%s %2.2f %d\n", prefix, sanitizedName, "stddev",        timer.stdDev(), epoch));
        lines.append(String.format(locale, "%s%s.%s %2.2f %d\n", prefix, sanitizedName, "median",        percentiles[0], epoch));
        lines.append(String.format(locale, "%s%s.%s %2.2f %d\n", prefix, sanitizedName, "75percentile",  percentiles[1], epoch));
        lines.append(String.format(locale, "%s%s.%s %2.2f %d\n", prefix, sanitizedName, "95percentile",  percentiles[2], epoch));
        lines.append(String.format(locale, "%s%s.%s %2.2f %d\n", prefix, sanitizedName, "98percentile",  percentiles[3], epoch));
        lines.append(String.format(locale, "%s%s.%s %2.2f %d\n", prefix, sanitizedName, "99percentile",  percentiles[4], epoch));
        lines.append(String.format(locale, "%s%s.%s %2.2f %d\n", prefix, sanitizedName, "999percentile", percentiles[5], epoch));
        sendToWarehouse(lines.toString());
    }

    private void printDoubleField(String name, double value, long epoch) {
        sendToWarehouse(String.format(locale, "%s%s %2.2f %d\n", prefix, sanitizeName(name), value, epoch));
    }

    private void printLongField(String name, long value, long epoch) {
        sendToWarehouse(String.format(locale, "%s%s %d %d\n", prefix, sanitizeName(name), value, epoch));
    }

    protected void printVmMetrics(long epoch) throws IOException {
        printDoubleField("jvm.memory.heap_usage", heapUsage(), epoch);
        printDoubleField("jvm.memory.non_heap_usage", nonHeapUsage(), epoch);
        for (Map.Entry<String, Double> pool : memoryPoolUsage().entrySet()) {
            printDoubleField("jvm.memory.memory_pool_usages." + pool.getKey(), pool.getValue(), epoch);
        }

        printDoubleField("jvm.daemon_thread_count", daemonThreadCount(), epoch);
        printDoubleField("jvm.thread_count", threadCount(), epoch);
        printDoubleField("jvm.uptime", uptime(), epoch);
        printDoubleField("jvm.fd_usage", fileDescriptorUsage(), epoch);

        for (Map.Entry<Thread.State, Double> entry : threadStatePercentages().entrySet()) {
            printDoubleField("jvm.thread-states." + entry.getKey().toString().toLowerCase(), entry.getValue(), epoch);
        }

        for (Map.Entry<String, GarbageCollector> entry : garbageCollectors().entrySet()) {
            printLongField("jvm.gc." + entry.getKey() + ".time", entry.getValue().getTime(TimeUnit.MILLISECONDS), epoch);
            printLongField("jvm.gc." + entry.getKey() + ".runs", entry.getValue().getRuns(), epoch);
        }
    }

    private void printRegularMetrics(long epoch) {
        for (Map.Entry<String, Map<String, Metric>> entry : Utils.sortAndFilterMetrics(metricsRegistry.allMetrics(), this.predicate).entrySet()) {
            for (Map.Entry<String, Metric> subEntry : entry.getValue().entrySet()) {
                final String simpleName = sanitizeName(entry.getKey() + "." + subEntry.getKey());
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
                        LOG.error("Error printing regular metrics:", ignored);
                    }
                }
            }
        }
    }
}
