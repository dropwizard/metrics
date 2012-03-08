package com.yammer.metrics.reporting;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.*;
import com.yammer.metrics.stats.Snapshot;
import com.yammer.metrics.core.MetricPredicate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * A reporter which periodically appends data from each metric to a metric-specific CSV file in
 * an output directory.
 */
public class CsvReporter extends AbstractPollingReporter implements
                                                         MetricProcessor<CsvReporter.Context> {

    /**
     * Enables the CSV reporter for the default metrics registry, and causes it to write to files in
     * {@code outputDir} with the specified period.
     *
     * @param outputDir    the directory in which {@code .csv} files will be created
     * @param period       the period between successive outputs
     * @param unit         the time unit of {@code period}
     */
    public static void enable(File outputDir, long period, TimeUnit unit) {
        enable(Metrics.defaultRegistry(), outputDir, period, unit);
    }

    /**
     * Enables the CSV reporter for the given metrics registry, and causes it to write to files in
     * {@code outputDir} with the specified period.
     *
     * @param metricsRegistry the metrics registry
     * @param outputDir       the directory in which {@code .csv} files will be created
     * @param period          the period between successive outputs
     * @param unit            the time unit of {@code period}
     */
    public static void enable(MetricsRegistry metricsRegistry, File outputDir, long period, TimeUnit unit) {
        final CsvReporter reporter = new CsvReporter(metricsRegistry, outputDir);
        reporter.start(period, unit);
    }

    /**
     * The context used to output metrics.
     */
    public interface Context {
        /**
         * Returns an open {@link PrintStream} for the metric with {@code header} already written
         * to it.
         *
         * @param header    the CSV header
         * @return an open {@link PrintStream}
         * @throws IOException if there is an error opening the stream or writing to it
         */
        PrintStream getStream(String header) throws IOException;
    }

    private final MetricPredicate predicate;
    private final File outputDir;
    private final Map<MetricName, PrintStream> streamMap;
    private final Clock clock;
    private long startTime;

    /**
     * Creates a new {@link CsvReporter} which will write all metrics from the given
     * {@link MetricsRegistry} to CSV files in the given output directory.
     *
     * @param outputDir          the directory to which files will be written
     * @param metricsRegistry    the {@link MetricsRegistry} containing the metrics this reporter
     *                           will report
     */
    public CsvReporter(MetricsRegistry metricsRegistry, File outputDir) {
        this(metricsRegistry, MetricPredicate.ALL, outputDir);
    }

    /**
     * Creates a new {@link CsvReporter} which will write metrics from the given
     * {@link MetricsRegistry} which match the given {@link MetricPredicate} to CSV files in the
     * given output directory.
     *
     * @param metricsRegistry    the {@link MetricsRegistry} containing the metrics this reporter
     *                           will report
     * @param predicate          the {@link MetricPredicate} which metrics are required to match
     *                           before being written to files
     * @param outputDir          the directory to which files will be written
     */
    public CsvReporter(MetricsRegistry metricsRegistry,
                       MetricPredicate predicate,
                       File outputDir) {
        this(metricsRegistry, predicate, outputDir, Clock.defaultClock());
    }

    /**
     * Creates a new {@link CsvReporter} which will write metrics from the given
     * {@link MetricsRegistry} which match the given {@link MetricPredicate} to CSV files in the
     * given output directory.
     *
     * @param metricsRegistry    the {@link MetricsRegistry} containing the metrics this reporter
     *                           will report
     * @param predicate          the {@link MetricPredicate} which metrics are required to match
     *                           before being written to files
     * @param outputDir          the directory to which files will be written
     * @param clock              the clock used to measure time
     */
    public CsvReporter(MetricsRegistry metricsRegistry,
                       MetricPredicate predicate,
                       File outputDir,
                       Clock clock) {
        super(metricsRegistry, "csv-reporter");
        if (outputDir.exists() && !outputDir.isDirectory()) {
            throw new IllegalArgumentException(outputDir + " is not a directory");
        }
        this.outputDir = outputDir;
        this.predicate = predicate;
        this.streamMap = new HashMap<MetricName, PrintStream>();
        this.startTime = 0L;
        this.clock = clock;
    }

    /**
     * Returns an opened {@link PrintStream} for the given {@link MetricName} which outputs data
     * to a metric-specific {@code .csv} file in the output directory.
     *
     * @param metricName    the name of the metric
     * @return an opened {@link PrintStream} specific to {@code metricName}
     * @throws IOException if there is an error opening the stream
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
        final long time = TimeUnit.MILLISECONDS.toSeconds(clock.time() - startTime);
        final Set<Entry<MetricName, Metric>> metrics = getMetricsRegistry().allMetrics().entrySet();
        try {
            for (Entry<MetricName, Metric> entry : metrics) {
                final MetricName metricName = entry.getKey();
                final Metric metric = entry.getValue();
                if (predicate.matches(metricName, metric)) {
                    final Context context = new Context() {
                        @Override
                        public PrintStream getStream(String header) throws IOException {
                            final PrintStream stream = getPrintStream(metricName, header);
                            stream.print(time);
                            stream.print(',');
                            return stream;
                        }

                    };
                    metric.processWith(this, entry.getKey(), context);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void processMeter(MetricName name, Metered meter, Context context) throws IOException {
        final PrintStream stream = context.getStream(
                "# time,count,1 min rate,mean rate,5 min rate,15 min rate");
        stream.append(new StringBuilder()
                              .append(meter.count()).append(',')
                              .append(meter.oneMinuteRate()).append(',')
                              .append(meter.meanRate()).append(',')
                              .append(meter.fiveMinuteRate()).append(',')
                              .append(meter.fifteenMinuteRate()).toString())
              .println();
        stream.flush();
    }

    @Override
    public void processCounter(MetricName name, Counter counter, Context context) throws IOException {
        final PrintStream stream = context.getStream("# time,count");
        stream.println(counter.count());
        stream.flush();
    }

    @Override
    public void processHistogram(MetricName name, Histogram histogram, Context context) throws IOException {
        final PrintStream stream = context.getStream("# time,min,max,mean,median,stddev,95%,99%,99.9%");
        final Snapshot snapshot = histogram.getSnapshot();
        stream.append(new StringBuilder()
                              .append(histogram.min()).append(',')
                              .append(histogram.max()).append(',')
                              .append(histogram.mean()).append(',')
                              .append(snapshot.getMedian()).append(',')
                              .append(histogram.stdDev()).append(',')
                              .append(snapshot.get95thPercentile()).append(',')
                              .append(snapshot.get99thPercentile()).append(',')
                              .append(snapshot.get999thPercentile()).toString())
                .println();
        stream.println();
        stream.flush();
    }

    @Override
    public void processTimer(MetricName name, Timer timer, Context context) throws IOException {
        final PrintStream stream = context.getStream("# time,min,max,mean,median,stddev,95%,99%,99.9%");
        final Snapshot snapshot = timer.getSnapshot();
        stream.append(new StringBuilder()
                              .append(timer.min()).append(',')
                              .append(timer.max()).append(',')
                              .append(timer.mean()).append(',')
                              .append(snapshot.getMedian()).append(',')
                              .append(timer.stdDev()).append(',')
                              .append(snapshot.get95thPercentile()).append(',')
                              .append(snapshot.get99thPercentile()).append(',')
                              .append(snapshot.get999thPercentile()).toString())
                .println();
        stream.flush();
    }

    @Override
    public void processGauge(MetricName name, Gauge<?> gauge, Context context) throws IOException {
        final PrintStream stream = context.getStream("# time,value");
        stream.println(gauge.value());
        stream.flush();
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

    private PrintStream getPrintStream(MetricName metricName, String header)
            throws IOException {
        PrintStream stream;
        synchronized (streamMap) {
            stream = streamMap.get(metricName);
            if (stream == null) {
                stream = createStreamForMetric(metricName);
                streamMap.put(metricName, stream);
                stream.println(header);
            }
        }
        return stream;
    }
}
