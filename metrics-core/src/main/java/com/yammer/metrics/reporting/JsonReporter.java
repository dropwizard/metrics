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
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * A reporter which periodically appends data from each metric to a metric-specific JSON file in
 * an output directory. The data has a CSV-type structure, which makes it easier to load it into various
 * charting tools/libraries (ie.: AmCharts JS library).
 */
public class JsonReporter extends AbstractPollingReporter implements
      MetricProcessor<JsonReporter.Context> {

   private static final Logger LOGGER = LoggerFactory.getLogger(JsonReporter.class);

   private static final String CLOSING_JSON_BRACES = "}]";
   private static final String OPENING_JSON_BRACES = "[{";

   /**
    * The context used to output metrics.
    */
   public interface Context {
      /**
       * Returns a {@link java.lang.StringBuilder} for the metric already
       * written appended to it.
       *
       * @return an open {@link java.lang.StringBuilder}
       * @throws java.io.IOException if there is an error opening the writer or writing to it
       */
      StringBuilder getBuilder() throws IOException;
   }

   private final MetricPredicate predicate;
   private final File outputDir;
   private final Map<MetricName, StringBuilder> builderMap;
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
   public static void enable(final File outputDir, final long period, final TimeUnit unit) {
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
   public static void enable(final MetricsRegistry metricsRegistry,
                             final File outputDir,
                             final long period,
                             final TimeUnit unit) {
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
   public JsonReporter(final MetricsRegistry metricsRegistry, final File outputDir) {
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
   public JsonReporter(final MetricsRegistry metricsRegistry,
                       final MetricPredicate predicate,
                       final File outputDir) {
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
   public JsonReporter(final MetricsRegistry metricsRegistry,
                       final MetricPredicate predicate,
                       final File outputDir,
                       final Clock clock) {
      super(metricsRegistry, "json-reporter");
      if (outputDir.exists() && !outputDir.isDirectory()) {
         throw new IllegalArgumentException(outputDir + " is not a directory");
      }
      this.outputDir = outputDir;
      this.predicate = predicate;
      this.builderMap = new HashMap<MetricName, StringBuilder>();
      this.startTime = 0L;
      this.clock = clock;
   }

   @Override
   public void run() {
      final long time = TimeUnit.MILLISECONDS.toSeconds(clock.getTime() - startTime);

      final Set<Map.Entry<MetricName, Metric>> metrics = getMetricsRegistry().getAllMetrics().entrySet();
      final MetricDispatcher dispatcher = new MetricDispatcher();
      try {
         for (Map.Entry<MetricName, Metric> entry : metrics) {
            final MetricName metricName = entry.getKey();
            final Metric metric = entry.getValue();
            if (predicate.matches(metricName, metric)) {

               final Context context = new Context() {
                  @Override
                  public StringBuilder getBuilder() throws IOException {
                     return getStringBuilder(metricName);
                  }
               };

               final StringBuilder builder = context.getBuilder();
               builder
                     .append(OPENING_JSON_BRACES)
                     .append(constructJsonKeyValuePair("time", time))
                     .append(",");

               dispatcher.dispatch(metric, metricName, this, context);
               dumpUpdatedMetric(metricName, builder.toString());
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Override
   public void processCounter(final MetricName metricName, final Counter counter, final Context context) throws IOException {
      final StringBuilder builder = context.getBuilder();
      builder
            .append(constructJsonKeyValuePair("count", counter.getCount()))
            .append(CLOSING_JSON_BRACES);
   }

   @Override
   public void processMeter(final MetricName metricName, final Metered meter, final Context context) throws IOException {

      final StringBuilder builder = context.getBuilder();
      builder
            .append(constructJsonKeyValuePair("count", meter.getCount())).append(',')
            .append(constructJsonKeyValuePair("1 min rate", meter.getOneMinuteRate())).append(',')
            .append(constructJsonKeyValuePair("mean rate", meter.getMeanRate())).append(',')
            .append(constructJsonKeyValuePair("5 min rate", meter.getFiveMinuteRate())).append(',')
            .append(constructJsonKeyValuePair("15 min rate", meter.getFifteenMinuteRate()))
            .append(CLOSING_JSON_BRACES);
   }

   @Override
   public void processHistogram(MetricName metricName, Histogram histogram, Context context) throws IOException {

      final StringBuilder builder = context.getBuilder();
      final Snapshot snapshot = histogram.getSnapshot();
      builder
            .append(constructJsonKeyValuePair("min", histogram.getMin())).append(',')
            .append(constructJsonKeyValuePair("max", histogram.getMax())).append(',')
            .append(constructJsonKeyValuePair("mean", histogram.getMean())).append(',')
            .append(constructJsonKeyValuePair("median", snapshot.getMedian())).append(',')
            .append(constructJsonKeyValuePair("stddev", histogram.getStdDev())).append(',')
            .append(constructJsonKeyValuePair("95%", snapshot.get95thPercentile())).append(',')
            .append(constructJsonKeyValuePair("99%", snapshot.get99thPercentile())).append(',')
            .append(constructJsonKeyValuePair("99.9%", snapshot.get999thPercentile()))
            .append(CLOSING_JSON_BRACES);
   }

   @Override
   public void processTimer(final MetricName metricName, final Timer timer, final Context context) throws IOException {
      final StringBuilder builder = context.getBuilder();
      final Snapshot snapshot = timer.getSnapshot();
      builder
            .append(constructJsonKeyValuePair("count", timer.getCount())).append(',')
            .append(constructJsonKeyValuePair("1 min rate", timer.getOneMinuteRate())).append(',')
            .append(constructJsonKeyValuePair("mean rate", timer.getMean())).append(',')
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
            .append(CLOSING_JSON_BRACES);
   }

   @Override
   public void processGauge(final MetricName metricName, final Gauge<?> gauge, final Context context) throws IOException {
      final StringBuilder builder = context.getBuilder();

      builder
            .append(constructJsonKeyValuePair("value", gauge.getValue()))
            .append(CLOSING_JSON_BRACES);
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
         for (final StringBuilder builder : builderMap.values()) {
            try {
               builder.setLength(0);
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
      final String filename = String.format("%s.json", metricName.getName());

      return new File(outputDir, filename);
   }


   private void dumpUpdatedMetric(final MetricName metricName, final String updatedMetric) throws IOException {
      final File newFile = obtainMetricFile(metricName);
      if (newFile.createNewFile() || (newFile.isFile() && newFile.canWrite())) {

         final PrintStream stream = new PrintStream(new FileOutputStream(newFile));
         final String cleansedUpdatedMetric = updatedMetric.replaceAll("\\}\\]\\[\\{", "\\},\\{");

         stream.print(cleansedUpdatedMetric);
         stream.flush();
         stream.close();
      }
   }

   private StringBuilder getStringBuilder(final MetricName metricName) throws IOException {

      StringBuilder builder;
      synchronized (builderMap) {
         builder = builderMap.get(metricName);
         if (builder == null) {
            builder = new StringBuilder();
            builderMap.put(metricName, builder);
         } else if (builder.length() == 0) {
            builder.append(loadedMetricFromFile(metricName));
         }
      }

      return builder;
   }

   private String loadedMetricFromFile(final MetricName metricName) throws IOException {

      final File newFile = obtainMetricFile(metricName);
      if (!newFile.createNewFile() && !(newFile.isFile() && newFile.canRead())) {
         throw new IOException("Unable to create a new file " + newFile);
      }

      final InputStream stream = newFile.toURI().toURL().openStream();
      if (stream.available() == 0) {
         return "";
      }

      return new Scanner(stream).useDelimiter("\\Z").next();
   }
}