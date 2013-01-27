package com.yammer.metrics.reporting;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Clock;
import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.core.Histogram;
import com.yammer.metrics.core.Metered;
import com.yammer.metrics.core.Metric;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricPredicate;
import com.yammer.metrics.core.MetricProcessor;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.stats.Snapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * A reporter which periodically appends data from each metric to a metric-specific JSON file in
 * an output directory. The data has a CSV-type structure, which makes it easier to load it into various
 * charting tools/libraries (ie.:AmCharts).
 */
public class JsonReporter extends AbstractPollingReporter implements
      MetricProcessor<JsonReporter.Context> {

   /**
    * The context used to output metrics.
    */
   public interface Context {
      /**
       * Returns an open {@link java.io.PrintStream} for the metric already written
       * to it.
       *
       * @return an open {@link java.io.PrintStream}
       * @throws java.io.IOException if there is an error opening the writer or writing to it
       */
      PrintStream getStream() throws IOException;

      InputStream openStreamToWrittenMetric() throws IOException;
   }

   private static final Logger LOGGER = LoggerFactory.getLogger(JsonReporter.class);

   private final MetricPredicate predicate;
   private final File outputDir;
   private final Map<MetricName, PrintStream> streamMap;
   private final Clock clock;
   private long startTime;

   /**
    * Enables the JSON reporter for the default metrics registry, and causes it to write to files in
    * {@code outputDir} with the specified period.
    *
    * @param outputDir the directory in which {@code .json} files will be created
    * @param period    the period between successive outputs
    * @param unit      the time unit of {@code period}
    */
   public static void enable(File outputDir, long period, TimeUnit unit) {
      enable(Metrics.defaultRegistry(), outputDir, period, unit);
   }

   /**
    * Enables the JSON reporter for the given metrics registry, and causes it to write to files in
    * {@code outputDir} with the specified period.
    *
    * @param metricsRegistry the metrics registry
    * @param outputDir       the directory in which {@code .json} files will be created
    * @param period          the period between successive outputs
    * @param unit            the time unit of {@code period}
    */
   public static void enable(MetricsRegistry metricsRegistry, File outputDir, long period, TimeUnit unit) {
      final JsonReporter reporter = new JsonReporter(metricsRegistry, outputDir);
      reporter.start(period, unit);
   }


   /**
    * Creates a new {@link com.yammer.metrics.reporting.JsonReporter} which will write all metrics from the given
    * {@link com.yammer.metrics.core.MetricsRegistry} to JSON files in the given output directory.
    *
    * @param outputDir       the directory to which files will be written
    * @param metricsRegistry the {@link com.yammer.metrics.core.MetricsRegistry} containing the metrics this reporter
    *                        will report
    */
   public JsonReporter(MetricsRegistry metricsRegistry, File outputDir) {
      this(metricsRegistry, MetricPredicate.ALL, outputDir);
   }

   /**
    * Creates a new {@link com.yammer.metrics.reporting.JsonReporter} which will write metrics from the given
    * {@link com.yammer.metrics.core.MetricsRegistry} which match the given {@link com.yammer.metrics.core.MetricPredicate} to JSON files in the
    * given output directory.
    *
    * @param metricsRegistry the {@link com.yammer.metrics.core.MetricsRegistry} containing the metrics this reporter
    *                        will report
    * @param predicate       the {@link com.yammer.metrics.core.MetricPredicate} which metrics are required to match
    *                        before being written to files
    * @param outputDir       the directory to which files will be written
    */
   public JsonReporter(MetricsRegistry metricsRegistry,
                       MetricPredicate predicate,
                       File outputDir) {
      this(metricsRegistry, predicate, outputDir, Clock.defaultClock());
   }

   /**
    * Creates a new {@link com.yammer.metrics.reporting.JsonReporter} which will write metrics from the given
    * {@link com.yammer.metrics.core.MetricsRegistry} which match the given {@link com.yammer.metrics.core.MetricPredicate} to JSON files in the
    * given output directory.
    *
    * @param metricsRegistry the {@link com.yammer.metrics.core.MetricsRegistry} containing the metrics this reporter
    *                        will report
    * @param predicate       the {@link com.yammer.metrics.core.MetricPredicate} which metrics are required to match
    *                        before being written to files
    * @param outputDir       the directory to which files will be written
    * @param clock           the clock used to measure time
    */
   public JsonReporter(MetricsRegistry metricsRegistry,
                       MetricPredicate predicate,
                       File outputDir,
                       Clock clock) {
      super(metricsRegistry, "json-reporter");
      if (outputDir.exists() && !outputDir.isDirectory()) {
         throw new IllegalArgumentException(outputDir + " is not a directory");
      }
      this.outputDir = outputDir;
      this.predicate = predicate;
      this.streamMap = Collections.synchronizedMap(new HashMap<MetricName, PrintStream>());
      this.startTime = 0L;
      this.clock = clock;
   }

   /**
    * Returns an opened {@link java.io.PrintStream} for the given {@link com.yammer.metrics.core.MetricName} which outputs data
    * to a metric-specific {@code .json} file in the output directory.
    *
    * @param metricName the name of the metric
    * @return an opened {@link java.io.PrintStream} specific to {@code metricName}
    * @throws java.io.IOException if there is an error opening the stream
    */
   protected PrintStream createStreamForMetric(final MetricName metricName) throws IOException {
      final File newFile = obtainMetricFile(metricName);
      if ((newFile.isFile() && newFile.canRead()) || newFile.createNewFile()) {
         return new PrintStream(new FileOutputStream(newFile));
      }

      throw new IOException("Unable to create a new file " + newFile);
   }

   /**
    * Returns an opened {@link java.io.InputStream} for the given {@link com.yammer.metrics.core.MetricName} which
    * consumes the content from a metric-specific {@code .json} file in the output directory.
    *
    * @param metricName the name of the metric
    * @return an opened {@link java.io.InputStream} specific to {@code metricName}
    * @throws java.io.IOException if there is an error opening the stream
    */
   protected InputStream getInputStream(final MetricName metricName) throws IOException {
      final File newFile = obtainMetricFile(metricName);

      if ((newFile.isFile() && newFile.canRead())) {
         return newFile.toURI().toURL().openStream();
      }

      throw new IOException("Unable to create a new file " + newFile);
   }

   @Override
   public void run() {
      final long time = TimeUnit.MILLISECONDS.toSeconds(clock.getTime() - startTime);
      final Set<Entry<MetricName, Metric>> metrics = getMetricsRegistry().getAllMetrics().entrySet();
      final MetricDispatcher dispatcher = new MetricDispatcher();
      try {
         for (Entry<MetricName, Metric> entry : metrics) {
            final MetricName metricName = entry.getKey();
            final Metric metric = entry.getValue();
            if (predicate.matches(metricName, metric)) {
               final Context context = new Context() {
                  @Override
                  public PrintStream getStream() throws IOException {
                     final PrintStream stream = getPrintStream(metricName);

                     stream
                           .append(new StringBuilder()
                                 .append("{")
                                 .append(constructJsonKeyValuePair("time", time)).append(", ").toString());

                     return stream;
                  }

                  @Override
                  public InputStream openStreamToWrittenMetric() throws IOException {
                     return getInputStream(metricName);
                  }

               };
               dispatcher.dispatch(metric, metricName, this, context);
               cleanseWrittenMetricJson(metricName, context);
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Override
   public void processCounter(MetricName metricName, Counter counter, Context context) throws IOException {

      final PrintStream stream = context.getStream();
      stream.append(new StringBuilder()
            .append(constructJsonKeyValuePair("count", counter.getCount()))
            .append("}]")).toString();
      stream.flush();
   }

   @Override
   public void processMeter(MetricName name, Metered meter, Context context) throws IOException {

      final PrintStream stream = context.getStream();
      stream
            .append(new StringBuilder()
                  .append(constructJsonKeyValuePair("count", meter.getCount())).append(',')
                  .append(constructJsonKeyValuePair("1 min rate", meter.getOneMinuteRate())).append(',')
                  .append(constructJsonKeyValuePair("mean rate", meter.getMeanRate())).append(',')
                  .append(constructJsonKeyValuePair("5 min rate", meter.getFiveMinuteRate())).append(',')
                  .append(constructJsonKeyValuePair("15 min rate", meter.getFifteenMinuteRate()))
                  .append("}]")).toString();
      stream.flush();
   }

   @Override
   public void processHistogram(MetricName name, Histogram histogram, Context context) throws IOException {
      final PrintStream stream = context.getStream();
      final Snapshot snapshot = histogram.getSnapshot();
      stream.append(new StringBuilder()
            .append(constructJsonKeyValuePair("min", histogram.getMin())).append(',')
            .append(constructJsonKeyValuePair("max", histogram.getMax())).append(',')
            .append(constructJsonKeyValuePair("mean", histogram.getMean())).append(',')
            .append(constructJsonKeyValuePair("median", snapshot.getMedian())).append(',')
            .append(constructJsonKeyValuePair("stddev", histogram.getStdDev())).append(',')
            .append(constructJsonKeyValuePair("95%", snapshot.get95thPercentile())).append(',')
            .append(constructJsonKeyValuePair("99%", snapshot.get99thPercentile())).append(',')
            .append(constructJsonKeyValuePair("99.9%", snapshot.get999thPercentile()))
            .append("}]")).toString();
      stream.flush();
   }

   @Override
   public void processTimer(MetricName name, Timer timer, Context context) throws IOException {
      final PrintStream stream = context.getStream();
      final Snapshot snapshot = timer.getSnapshot();
      stream.append(new StringBuilder()
            .append(constructJsonKeyValuePair("count", timer.getCount())).append(',')
            .append(constructJsonKeyValuePair("1 min rate", timer.getOneMinuteRate())).append(',')
            .append(constructJsonKeyValuePair("mean rate", timer.getMeanRate())).append(',')
            .append(constructJsonKeyValuePair("5 min rate", timer.getFiveMinuteRate())).append(',')
            .append(constructJsonKeyValuePair("15 min rate", timer.getFifteenMinuteRate())).append(',')
            .append(constructJsonKeyValuePair("min", timer.getMin())).append(',')
            .append(constructJsonKeyValuePair("max", timer.getMax())).append(',')
            .append(constructJsonKeyValuePair("mean", timer.getMean())).append(',')
            .append(constructJsonKeyValuePair("median", snapshot.getMedian())).append(',')
            .append(constructJsonKeyValuePair("stddev", timer.getStdDev())).append(',')
            .append(constructJsonKeyValuePair("95%", snapshot.get95thPercentile())).append(',')
            .append(constructJsonKeyValuePair("99%", snapshot.get99thPercentile())).append(',')
            .append(constructJsonKeyValuePair("99.9%", snapshot.get999thPercentile()))
            .append("}]")).toString();
      stream.flush();
   }

   @Override
   public void processGauge(MetricName name, Gauge<?> gauge, Context context) throws IOException {
      final PrintStream stream = context.getStream();

      stream.append(new StringBuilder()
            .append(constructJsonKeyValuePair("value", gauge.getValue()))
            .append("}]")).toString();
      stream.flush();
   }

   @Override
   public void start(long period, TimeUnit unit) {
      this.startTime = clock.getTime();
      super.start(period, unit);
   }

   @Override
   public void shutdown() {
      try {
         super.shutdown();
      } finally {
         for (final PrintStream stream : streamMap.values()) {
            try {
               stream.close();
            } catch (Throwable t) {
               LOGGER.warn("Failed to close writer", t);
            }
         }
      }
   }

   private String constructJsonKeyValuePair(final String key, final Object value) {
      return MessageFormat.format("\"{0}\":\"{1}\"", key, value.toString());
   }

   private File obtainMetricFile(final MetricName metricName) {
      final String filename = String.format("%s.json", metricName.toString());

      return new File(outputDir, filename);
   }

   private void cleanseWrittenMetricJson(final MetricName metricName, final Context context) throws IOException {
      final InputStream metricInputStream = context.openStreamToWrittenMetric();
      if (metricInputStream.available() == 0) {
         return;
      }

      final String writtenMetric = new Scanner(metricInputStream).useDelimiter("\\A").next();
      final File newFile = obtainMetricFile(metricName);

      if ((newFile.isFile() && newFile.canWrite())) {
         final PrintStream stream = new PrintStream(new FileOutputStream(newFile));

         stream.print(writtenMetric.replaceAll("}]\\{", "},{"));
         stream.flush();
         stream.close();
      }
   }

   private PrintStream getPrintStream(final MetricName metricName)
         throws IOException {

      PrintStream stream = streamMap.get(metricName);

      if (stream == null) {
         stream = createStreamForMetric(metricName);
         stream.print("[");
         streamMap.put(metricName, stream);
      }

      return stream;
   }
}