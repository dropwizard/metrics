package com.yammer.metrics.reporting.tests;

import com.yammer.metrics.core.Clock;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.reporting.AbstractPollingReporter;
import com.yammer.metrics.reporting.CsvReporter;
import com.yammer.metrics.util.MetricPredicate;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class CsvReporterTest extends AbstractPollingReporterTest {

    @Override
    protected AbstractPollingReporter createReporter(MetricsRegistry registry, final OutputStream out, Clock clock) throws Exception {
        return new CsvReporter((File) null, registry, MetricPredicate.ALL, clock) {
            @Override
            protected PrintStream createStreamForMetric(MetricName metricName) throws IOException {
                return new PrintStream(out);
            }
        };
    }

    @Override
    public String[] expectedCounterResult(long count) {
        return new String[]{"# time,count", String.format("5,%s\n", count)};
    }

    @Override
    public String[] expectedHistogramResult() {
        return new String[]{"# time,min,max,mean,median,stddev,90%,95%,99%", "5,1.0,3.0,2.0,0.5,1.5,0.9,0.95,0.99\n"};
    }

    @Override
    public String[] expectedMeterResult() {
        return new String[]{"# time,count,1 min rate,mean rate,5 min rate,15 min rate", "5,1,1.0,2.0,5.0,15.0\n"};
    }

    @Override
    public String[] expectedTimerResult() {
        return new String[]{"# time,min,max,mean,median,stddev,90%,95%,99%", "5,1.0,3.0,2.0,0.5,1.5,0.9,0.95,0.99\n"};
    }

    @Override
    public String[] expectedGaugeResult(String value) {
        return new String[]{"# time,value", String.format("5,%s\n", value)};
    }
}
