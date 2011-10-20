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
    protected final Locale locale = Locale.US;
    private final String host;
    private final int port;
    private final MetricPredicate predicate;
    private Writer writer;

    protected AbstractTimeSeriesReporter(MetricsRegistry metricsRegistry, String name, String host, int port, MetricPredicate predicate) {
        super(metricsRegistry, name);
        this.host = host;
        this.port = port;
        this.predicate = predicate;
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

    private static String sanitizeName(String name) {
        return name.replace(' ', '-');
    }

    abstract protected String formatDoubleField(String sanitizedName, long epoch, double value);

    abstract protected String formatLongField(String sanitizedName, long epoch, long value);

    abstract protected String formatGaugeField(String sanitizedName, long epoch, GaugeMetric<?> gauge);

    private void printDoubleField(String unsanitaryName, long epoch, double value) {
        sendToWarehouse(formatDoubleField(sanitizeName(unsanitaryName), epoch, value));
    }

    private void printLongField(String unsanitaryName, long epoch, long value) {
        sendToWarehouse(formatLongField(sanitizeName(unsanitaryName), epoch, value));
    }

    private void printGauge(GaugeMetric<?> gauge, String sanitizedName, long epoch) {
        sendToWarehouse(formatGaugeField(sanitizedName + ".value", epoch, gauge));
    }

    private void printCounter(CounterMetric counter, String sanitizedName, long epoch) {
        sendToWarehouse(formatLongField(sanitizedName, epoch, counter.count()));
    }

    private void printHistogram(HistogramMetric histogram, String simpleName, long epoch) {
        final String sanitizedName = sanitizeName(simpleName);
        final double[] percentiles = histogram.percentiles(0.5, 0.75, 0.95, 0.98, 0.99, 0.999);
        final StringBuilder lines = new StringBuilder();
        lines.append(formatDoubleField(sanitizedName + ".min", epoch, histogram.min()));
        lines.append(formatDoubleField(sanitizedName + ".max", epoch, histogram.max()));
        lines.append(formatDoubleField(sanitizedName + ".mean", epoch, histogram.mean()));
        lines.append(formatDoubleField(sanitizedName + ".stddev", epoch, histogram.stdDev()));
        lines.append(formatDoubleField(sanitizedName + ".median", epoch, percentiles[0]));
        lines.append(formatDoubleField(sanitizedName + ".75percentile", epoch, percentiles[1]));
        lines.append(formatDoubleField(sanitizedName + ".95percentile", epoch, percentiles[2]));
        lines.append(formatDoubleField(sanitizedName + ".98percentile", epoch, percentiles[3]));
        lines.append(formatDoubleField(sanitizedName + ".99percentile", epoch, percentiles[4]));
        lines.append(formatDoubleField(sanitizedName + ".999percentile", epoch, percentiles[5]));

        sendToWarehouse(lines.toString());
    }

    private void printTimer(TimerMetric timer, String simpleName, long epoch) {
        printMetered(timer, simpleName, epoch);

        final String sanitizedName = sanitizeName(simpleName);
        final double[] percentiles = timer.percentiles(0.5, 0.75, 0.95, 0.98, 0.99, 0.999);

        final StringBuilder lines = new StringBuilder();
        lines.append(formatDoubleField(sanitizedName + ".min", epoch, timer.min()));
        lines.append(formatDoubleField(sanitizedName + ".max", epoch, timer.max()));
        lines.append(formatDoubleField(sanitizedName + ".mean", epoch, timer.mean()));
        lines.append(formatDoubleField(sanitizedName + ".stddev", epoch, timer.stdDev()));
        lines.append(formatDoubleField(sanitizedName + ".median", epoch, percentiles[0]));
        lines.append(formatDoubleField(sanitizedName + ".75percentile", epoch, percentiles[1]));
        lines.append(formatDoubleField(sanitizedName + ".95percentile", epoch, percentiles[2]));
        lines.append(formatDoubleField(sanitizedName + ".98percentile", epoch, percentiles[3]));
        lines.append(formatDoubleField(sanitizedName + ".99percentile", epoch, percentiles[4]));
        lines.append(formatDoubleField(sanitizedName + ".999percentile", epoch, percentiles[5]));

        sendToWarehouse(lines.toString());
    }


    private void printMetered(Metered meter, String simpleName, long epoch) {
        final String sanitizedName = sanitizeName(simpleName);
        final StringBuilder lines = new StringBuilder();

        lines.append(formatLongField(sanitizedName + ".count", epoch, meter.count()));
        lines.append(formatDoubleField(sanitizedName + ".meanRate", epoch, meter.meanRate()));
        lines.append(formatDoubleField(sanitizedName + ".oneMinuteRate", epoch, meter.oneMinuteRate()));
        lines.append(formatDoubleField(sanitizedName + ".fiveMinuteRate", epoch, meter.fiveMinuteRate()));
        lines.append(formatDoubleField(sanitizedName + ".fifteenMinuteRate", epoch, meter.fifteenMinuteRate()));

        sendToWarehouse(lines.toString());
    }

    private void printVmMetrics(long epoch) throws IOException {
        printDoubleField("jvm.memory.heap_usage", epoch, heapUsage());
        printDoubleField("jvm.memory.non_heap_usage", epoch, nonHeapUsage());
        for (Map.Entry<String, Double> pool : memoryPoolUsage().entrySet()) {
            printDoubleField("jvm.memory.memory_pool_usages." + pool.getKey(), epoch, pool.getValue());
        }

        printDoubleField("jvm.daemon_thread_count", epoch, daemonThreadCount());
        printDoubleField("jvm.thread_count", epoch, threadCount());
        printDoubleField("jvm.uptime", epoch, uptime());
        printDoubleField("jvm.fd_usage", epoch, fileDescriptorUsage());

        for (Map.Entry<Thread.State, Double> entry : threadStatePercentages().entrySet()) {
            printDoubleField("jvm.thread-states." + entry.getKey().toString().toLowerCase(), epoch, entry.getValue());
        }

        for (Map.Entry<String, GarbageCollector> entry : garbageCollectors().entrySet()) {
            printLongField("jvm.gc." + entry.getKey() + ".time", epoch, entry.getValue().getTime(TimeUnit.MILLISECONDS));
            printLongField("jvm.gc." + entry.getKey() + ".runs", epoch, entry.getValue().getRuns());
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
