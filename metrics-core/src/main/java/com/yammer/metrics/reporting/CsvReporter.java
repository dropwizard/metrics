package com.yammer.metrics.reporting;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.yammer.metrics.core.Clock;
import com.yammer.metrics.core.CounterMetric;
import com.yammer.metrics.core.GaugeMetric;
import com.yammer.metrics.core.HistogramMetric;
import com.yammer.metrics.core.MeterMetric;
import com.yammer.metrics.core.Metric;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.core.TimerMetric;
import com.yammer.metrics.util.MetricPredicate;

public class CsvReporter extends AbstractPollingReporter {
    private final MetricPredicate predicate;
    private final File outputDir;
    private final Map<MetricName, PrintStream> streamMap;
    private final Clock clock;
    private long startTime;

    public CsvReporter(File outputDir,
            MetricsRegistry metricsRegistry,
            MetricPredicate predicate) throws Exception {
        this(outputDir, metricsRegistry, predicate, Clock.DEFAULT);
    }
    
    public CsvReporter(File outputDir,
                       MetricsRegistry metricsRegistry,
                       MetricPredicate predicate,
                       Clock clock) throws Exception {
        super(metricsRegistry, "csv-reporter");
        this.outputDir = outputDir;
        this.predicate = predicate;
        this.streamMap = new HashMap<MetricName, PrintStream>();
        this.startTime = 0L;
        this.clock = clock;
    }

    public CsvReporter(File outputDir, MetricsRegistry metricsRegistry)
            throws Exception {
        this(outputDir, metricsRegistry, MetricPredicate.ALL);
    }

    private PrintStream getPrintStream(MetricName metricName, Metric metric)
            throws IOException {
        PrintStream stream;
        synchronized (streamMap) {
            stream = streamMap.get(metricName);
            if (stream == null) {
                stream = createStreamForMetric(metricName);
                streamMap.put(metricName, stream);
                if (metric instanceof GaugeMetric<?>) {
                    stream.println("# time,value");
                } else if (metric instanceof CounterMetric) {
                    stream.println("# time,count");
                } else if (metric instanceof HistogramMetric) {
                    stream.println("# time,min,max,mean,median,stddev,90%,95%,99%");
                } else if (metric instanceof MeterMetric) {
                    stream.println("# time,count,1 min rate,mean rate,5 min rate,15 min rate");
                } else if (metric instanceof TimerMetric) {
                    stream.println("# time,min,max,mean,median,stddev,90%,95%,99%");
                }
            }
        }
        return stream;
    }
    
    /**
     * Override to do tricks (such as testing).
     */
    protected PrintStream createStreamForMetric(MetricName metricName) throws IOException {
        final File newFile = new File(outputDir, metricName.getName() + ".csv");
        if (newFile.createNewFile()) {
            return new PrintStream(new FileOutputStream(newFile));
        }
        throw new IOException("Unable to create " + newFile);
    }

    @Override
    public void run() {
        final long time = (clock.time() - startTime) / 1000;
        final Set<Entry<MetricName, Metric>> metrics = metricsRegistry.allMetrics().entrySet();
        try {
            for (Entry<MetricName, Metric> entry : metrics) {
                final MetricName metricName = entry.getKey();
                final Metric metric = entry.getValue();
                if (predicate.matches(metricName, metric)) {
                    final StringBuilder buf = new StringBuilder();
                    buf.append(time).append(",");
                    if (metric instanceof GaugeMetric<?>) {
                        final Object objVal = ((GaugeMetric<?>) metric).value();
                        buf.append(objVal);
                    } else if (metric instanceof CounterMetric) {
                        buf.append(((CounterMetric) metric).count());
                    } else if (metric instanceof HistogramMetric) {
                        final HistogramMetric timer = (HistogramMetric) metric;

                        final double[] percentiles = timer.percentiles(0.5, 0.90, 0.95, 0.99);
                        buf.append(timer.min()).append(",");
                        buf.append(timer.max()).append(",");
                        buf.append(timer.mean()).append(",");
                        buf.append(percentiles[0]).append(","); // median
                        buf.append(timer.stdDev()).append(",");
                        buf.append(percentiles[1]).append(","); // 90%
                        buf.append(percentiles[2]).append(","); // 95%
                        buf.append(percentiles[3]); // 99 %
                    } else if (metric instanceof MeterMetric) {
                        buf.append(((MeterMetric) metric).count()).append(",");
                        buf.append(((MeterMetric) metric).oneMinuteRate())
                           .append(",");
                        buf.append(((MeterMetric) metric).meanRate()).append(
                                ",");
                        buf.append(((MeterMetric) metric).fiveMinuteRate())
                           .append(",");
                        buf.append(((MeterMetric) metric).fifteenMinuteRate());
                    } else if (metric instanceof TimerMetric) {
                        final TimerMetric timer = (TimerMetric) metric;

                        final double[] percentiles = timer.percentiles(0.5, 0.90, 0.95, 0.99);
                        buf.append(timer.min()).append(",");
                        buf.append(timer.max()).append(",");
                        buf.append(timer.mean()).append(",");
                        buf.append(percentiles[0]).append(","); // median
                        buf.append(timer.stdDev()).append(",");
                        buf.append(percentiles[1]).append(","); // 90%
                        buf.append(percentiles[2]).append(","); // 95%
                        buf.append(percentiles[3]); // 99 %
                    }

                    final PrintStream out = getPrintStream(metricName, metric);
                    out.println(buf.toString());
                    out.flush();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start(long period, TimeUnit unit) {
        this.startTime = clock.time();
        super.start(period, unit);
    }

    @Override
    public void shutdown() {
        try {
            super.shutdown();
        } finally {
            for (PrintStream out : streamMap.values()) {
                out.close();
            }
        }
    }
}