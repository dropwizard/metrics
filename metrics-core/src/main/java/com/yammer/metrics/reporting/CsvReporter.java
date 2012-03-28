package com.yammer.metrics.reporting;

import com.yammer.metrics.core.*;
import com.yammer.metrics.stats.Snapshot;

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
public class CsvReporter extends AbstractPollingReporter implements MetricProcessor<CsvReporter.Context> {

    /**
     * The context used to output metrics.
     */
    public interface Context {
        /**
         * Returns an open {@link PrintStream} for the metric with {@code header} already written
         * to it.
         *
         * @param header the CSV header
         * @return an open {@link PrintStream}
         * @throws IOException if there is an error opening the stream or writing to it
         */
        PrintStream getStream(String header) throws IOException;
    }

    public static class Builder {
        private final Set<MetricsRegistry> registries;
        private final String name;
        private final long period;
        private final TimeUnit timeUnit;
        private MetricPredicate predicate;
        private File outputDir;
        private Clock clock;

        public Builder(Set<MetricsRegistry> registries, String name, long period, TimeUnit unit){
            this.registries = registries;
            this.name = name;
            this.period = period;
            this.timeUnit = unit;

            //Set mutable items to defaults
            this.predicate = MetricPredicate.ALL;
            this.clock = Clock.defaultClock();
            this.outputDir = new File(System.getProperty("java.io.tmpdir"));
        }

        public Builder withPredicate(MetricPredicate predicate) {
            this.predicate = predicate;
            return this;
        }

        public Builder withOutputDir(File outputDir) {
            this.outputDir = outputDir;
            return this;
        }

        public Builder withClock(Clock clock) {
            this.clock = clock;
            return this;
        }

        public CsvReporter build() {
            return new CsvReporter(this);
        }
    }

    private final MetricPredicate predicate;
    private final File outputDir;
    private final Map<MetricName, PrintStream> streamMap;
    private final Clock clock;
    private final long startTime;

    /**
     * Creates a new {@link CsvReporter} which will write all metrics from the given
     * {@link MetricsRegistry} to CSV files in the given output directory.
     *
     * @param builder
     */
    private CsvReporter(Builder builder) {
        super(builder.registries, builder.name, builder.period, builder.timeUnit);
        this.predicate = builder.predicate;
        this.outputDir = builder.outputDir;
        this.streamMap = new HashMap<MetricName, PrintStream>();
        this.clock = builder.clock;
        this.startTime = System.currentTimeMillis();
    }

    /**
     * Returns an opened {@link PrintStream} for the given {@link MetricName} which outputs data
     * to a metric-specific {@code .csv} file in the output directory.
     *
     * @param metricName the name of the metric
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

        try {
            for (MetricsRegistry metricsRegistry : super.metricsRegistries) {
                for (Entry<MetricName, Metric> entry : metricsRegistry.allMetrics().entrySet()) {
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
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void processMeter(MetricName name, Metered meter, Context context) throws IOException {
        final PrintStream stream = context.getStream(
                "# time,getCount,1 getMin rate,getMean rate,5 getMin rate,15 getMin rate");
        stream.append(new StringBuilder()
                .append(meter.getCount()).append(',')
                .append(meter.getOneMinuteRate()).append(',')
                .append(meter.getMeanRate()).append(',')
                .append(meter.getFiveMinuteRate()).append(',')
                .append(meter.getFifteenMinuteRate()).toString())
                .println();
        stream.flush();
    }

    @Override
    public void processCounter(MetricName name, Counter counter, Context context) throws IOException {
        final PrintStream stream = context.getStream("# time,getCount");
        stream.println(counter.count());
        stream.flush();
    }

    @Override
    public void processHistogram(MetricName name, Histogram histogram, Context context) throws IOException {
        final PrintStream stream = context.getStream("# time,getMin,getMax,getMean,median,stddev,95%,99%,99.9%");
        final Snapshot snapshot = histogram.getSnapshot();
        stream.append(new StringBuilder()
                .append(histogram.getMin()).append(',')
                .append(histogram.getMax()).append(',')
                .append(histogram.getMean()).append(',')
                .append(snapshot.getMedian()).append(',')
                .append(histogram.getStdDev()).append(',')
                .append(snapshot.get95thPercentile()).append(',')
                .append(snapshot.get99thPercentile()).append(',')
                .append(snapshot.get999thPercentile()).toString())
                .println();
        stream.println();
        stream.flush();
    }

    @Override
    public void processTimer(MetricName name, Timer timer, Context context) throws IOException {
        final PrintStream stream = context.getStream("# time,getMin,getMax,getMean,median,stddev,95%,99%,99.9%");
        final Snapshot snapshot = timer.getSnapshot();
        stream.append(new StringBuilder()
                .append(timer.getMin()).append(',')
                .append(timer.getMax()).append(',')
                .append(timer.getMean()).append(',')
                .append(snapshot.getMedian()).append(',')
                .append(timer.getStdDev()).append(',')
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
