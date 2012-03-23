package com.yammer.metrics.reporting.tests;

import com.yammer.metrics.core.Clock;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricPredicate;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.reporting.AbstractPollingReporter;
import com.yammer.metrics.reporting.CsvReporter;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class CsvReporterTest extends AbstractPollingReporterTest {

    @Override
    protected AbstractPollingReporter createReporter(MetricsRegistry registry, final OutputStream out, Clock clock) throws Exception {
        return new CsvReporter(registry, MetricPredicate.ALL, new File("/tmp"), clock) {
            @Override
            protected PrintStream createStreamForMetric(MetricName metricName) throws IOException {
                return new PrintStream(out);
            }
        };
    }

    @Override
    public String[] expectedCounterResult(long count) {
        return new String[]{"# time,getCount", String.format("5,%s\n", count)};
    }

    @Override
    public String[] expectedHistogramResult() {
        return new String[]{"# time,getMin,getMax,getMean,median,stddev,95%,99%,99.9%",
                            "5,1.0,3.0,2.0,0.4995,1.5,0.9499499999999999,0.98999,0.998999\n"};
    }

    @Override
    public String[] expectedMeterResult() {
        return new String[]{"# time,getCount,1 getMin rate,getMean rate,5 getMin rate,15 getMin rate",
                            "5,1,1.0,2.0,5.0,15.0\n"};
    }

    @Override
    public String[] expectedTimerResult() {
        return new String[]{"# time,getMin,getMax,getMean,median,stddev,95%,99%,99.9%",
                            "5,1.0,3.0,2.0,0.4995,1.5,0.9499499999999999,0.98999,0.998999\n"};
    }

    @Override
    public String[] expectedGaugeResult(String value) {
        return new String[]{"# time,value", String.format("5,%s\n", value)};
    }
}
