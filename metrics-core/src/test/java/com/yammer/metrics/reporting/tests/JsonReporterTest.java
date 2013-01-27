package com.yammer.metrics.reporting.tests;

import com.yammer.metrics.core.Clock;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricPredicate;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.reporting.AbstractPollingReporter;
import com.yammer.metrics.reporting.JsonReporter;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

public class JsonReporterTest extends AbstractPollingReporterTest {

   @Override
   protected AbstractPollingReporter createReporter(MetricsRegistry registry, final OutputStream out, Clock clock) throws Exception {

      return new JsonReporter(registry, MetricPredicate.ALL, new File("/tmp"), clock) {
         @Override
         protected PrintStream createStreamForMetric(MetricName metricName) throws IOException {
            return new PrintStream(out);
         }

         @Override
         protected InputStream getInputStream(MetricName metricName) throws IOException {
            return  new ByteArrayInputStream("".getBytes("UTF-8"));
         }
      };
   }

   @Override
   public String[] expectedCounterResult(long count) {
      return new String[]{String.format("[{\"time\":\"5\", \"count\":\"%s\"}]", count)};
   }

   @Override
   public String[] expectedHistogramResult() {
      return new String[]{"[{\"time\":\"5\", \"min\":\"1.0\",\"max\":\"3.0\",\"mean\":\"2.0\",\"median\":\"0.4995\",\"stddev\":\"1.5\",\"95%\":\"0.9499499999999999\",\"99%\":\"0.98999\",\"99.9%\":\"0.998999\"}]"};
   }

   @Override
   public String[] expectedMeterResult() {
      return new String[]{"[{\"time\":\"5\", \"count\":\"1\",\"1 min rate\":\"1.0\",\"mean rate\":\"2.0\",\"5 min rate\":\"5.0\",\"15 min rate\":\"15.0\"}]"};
   }

   @Override
   public String[] expectedTimerResult() {
      return new String[]{"[{\"time\":\"5\", \"count\":\"1\",\"1 min rate\":\"1.0\",\"mean rate\":\"2.0\",\"5 min rate\":\"5.0\",\"15 min rate\":\"15.0\"\"min\":\"1.0\",\"max\":\"3.0\",\"mean\":\"2.0\",\"median\":\"0.4995\",\"stddev\":\"1.5\",\"95%\":\"0.9499499999999999\",\"99%\":\"0.98999\",\"99.9%\":\"0.998999\"}]"};
   }

   @Override
   public String[] expectedGaugeResult(String value) {
      return new String[]{String.format("[{\"time\":\"5\", \"value\":\"%s\"}]", value)};
   }
}
