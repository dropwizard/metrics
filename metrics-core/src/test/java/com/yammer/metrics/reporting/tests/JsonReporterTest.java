package com.yammer.metrics.reporting.tests;

import com.yammer.metrics.core.Clock;
import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.core.Histogram;
import com.yammer.metrics.core.Meter;
import com.yammer.metrics.core.Metric;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricPredicate;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.reporting.AbstractPollingReporter;
import com.yammer.metrics.reporting.JsonReporter;
import org.junit.After;
import org.junit.Test;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.Callable;

import static org.junit.Assert.assertEquals;

public class JsonReporterTest extends AbstractPollingReporterTest {

   private static final String TEMP_DIR = "/tmp";
   private static final String METRIC_FILENAME = TEMP_DIR + "/metric.json";

   @After
   public void afterEach() throws Exception {
      new File(METRIC_FILENAME).delete();
   }

   @Override
   protected AbstractPollingReporter createReporter(MetricsRegistry registry, final OutputStream out, Clock clock) throws Exception {
      return new JsonReporter(registry, MetricPredicate.ALL, new File(TEMP_DIR), clock);
   }

   @Override
   protected <T extends Metric> void assertReporterOutput(Callable<T> action, String... expected) throws Exception {
      // Invoke the callable to trigger (ie, mark()/inc()/etc) and return the metric
      final T metric = action.call();
      try {
         // Add the metric to the registry, run the reporter and flush the result
         registry.add(new MetricName(Object.class, "metric"), metric);

         // Yes, run twice in order to get two JSON objects in the array in the target file
         reporter.run();
         reporter.run();

         final InputStream stream = new File(METRIC_FILENAME).toURI().toURL().openStream();
         final String metricContent = new Scanner(stream).useDelimiter("\\Z").next();

         assertEquals(expected[0].trim(), metricContent.trim());

      } finally {
         reporter.shutdown();
      }
   }

   @Test
   public final void counterToJson() throws Exception {
      final long count = new Random().nextInt(Integer.MAX_VALUE);
      assertReporterOutput(
            new Callable<Counter>() {
               @Override
               public Counter call() throws Exception {
                  return createCounter(count);
               }
            },
            expectedCounterResult(count));
   }

   @Test
   public final void histogramToJson() throws Exception {
      assertReporterOutput(
            new Callable<Histogram>() {
               @Override
               public Histogram call() throws Exception {
                  return createHistogram();
               }
            },
            expectedHistogramResult());
   }

   @Test
   public final void meterToJson() throws Exception {
      assertReporterOutput(
            new Callable<Meter>() {
               @Override
               public Meter call() throws Exception {
                  return createMeter();
               }
            },
            expectedMeterResult());
   }

   @Test
   public final void timerToJson() throws Exception {
      assertReporterOutput(
            new Callable<Timer>() {
               @Override
               public Timer call() throws Exception {
                  return createTimer();
               }
            },
            expectedTimerResult());
   }

   @Test
   public final void gaugeToJson() throws Exception {
      final String value = "gaugeValue";
      assertReporterOutput(
            new Callable<Gauge<String>>() {
               @Override
               public Gauge<String> call() throws Exception {
                  return createGauge();
               }
            },
            expectedGaugeResult(value));
   }

   @Override
   public String[] expectedCounterResult(long count) {
      return new String[]{String.format("[{\"time\":\"5\",\"count\":\"%s\"},{\"time\":\"5\",\"count\":\"%s\"}]", count, count)};
   }

   @Override
   public String[] expectedHistogramResult() {
      return new String[]{"[{\"time\":\"5\",\"min\":\"1.0\",\"max\":\"3.0\",\"mean\":\"2.0\",\"median\":\"0.4995\",\"stddev\":\"1.5\",\"95%\":\"0.9499499999999999\",\"99%\":\"0.98999\",\"99.9%\":\"0.998999\"},{\"time\":\"5\",\"min\":\"1.0\",\"max\":\"3.0\",\"mean\":\"2.0\",\"median\":\"0.4995\",\"stddev\":\"1.5\",\"95%\":\"0.9499499999999999\",\"99%\":\"0.98999\",\"99.9%\":\"0.998999\"}]"};
   }

   @Override
   public String[] expectedMeterResult() {
      return new String[]{"[{\"time\":\"5\",\"count\":\"1\",\"1 min rate\":\"1.0\",\"mean rate\":\"2.0\",\"5 min rate\":\"5.0\",\"15 min rate\":\"15.0\"},{\"time\":\"5\",\"count\":\"1\",\"1 min rate\":\"1.0\",\"mean rate\":\"2.0\",\"5 min rate\":\"5.0\",\"15 min rate\":\"15.0\"}]"};
   }

   @Override
   public String[] expectedTimerResult() {
      return new String[]{"[{\"time\":\"5\",\"count\":\"1\",\"1 min rate\":\"1.0\",\"mean rate\":\"2.0\",\"5 min rate\":\"5.0\",\"15 min rate\":\"15.0\",\"min\":\"1.0\",\"max\":\"3.0\",\"mean\":\"2.0\",\"median\":\"0.4995\",\"stddev\":\"1.5\",\"95%\":\"0.9499499999999999\",\"99%\":\"0.98999\",\"99.9%\":\"0.998999\"},{\"time\":\"5\",\"count\":\"1\",\"1 min rate\":\"1.0\",\"mean rate\":\"2.0\",\"5 min rate\":\"5.0\",\"15 min rate\":\"15.0\",\"min\":\"1.0\",\"max\":\"3.0\",\"mean\":\"2.0\",\"median\":\"0.4995\",\"stddev\":\"1.5\",\"95%\":\"0.9499499999999999\",\"99%\":\"0.98999\",\"99.9%\":\"0.998999\"}]"};
   }

   @Override
   public String[] expectedGaugeResult(String value) {
      return new String[]{String.format("[{\"time\":\"5\",\"value\":\"%s\"},{\"time\":\"5\",\"value\":\"%s\"}]", value, value)};
   }
}
