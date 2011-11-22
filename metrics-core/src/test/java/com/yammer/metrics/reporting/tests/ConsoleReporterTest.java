package com.yammer.metrics.reporting.tests;

import com.yammer.metrics.core.Clock;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.reporting.AbstractPollingReporter;
import com.yammer.metrics.reporting.ConsoleReporter;
import com.yammer.metrics.util.MetricPredicate;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.TimeZone;

public class ConsoleReporterTest extends AbstractPollingReporterTest {

    @Override
    protected AbstractPollingReporter createReporter(MetricsRegistry registry, OutputStream out, Clock clock) {
        return new ConsoleReporter(registry, new PrintStream(out), MetricPredicate.ALL, clock, TimeZone.getTimeZone("UTC"));
    }
    
    @Override
    public String[] expectedCounterResult(int count) {
        return new String[] {
            "1/1/70 12:00:05 AM =============================================================",
            "java.lang.Object:",
            "metric:",
            "count = " + count
        };
    }

    @Override
    public String[] expectedHistogramResult() {
        return new String[] {
            "1/1/70 12:00:05 AM =============================================================",
            "java.lang.Object:",
            "metric:",
            "min = 1.00",
            "max = 1.00",
            "mean = 1.00",
            "stddev = 0.00",
            "median = 1.00",
            "75% <= 1.00",
            "95% <= 1.00",
            "98% <= 1.00",
            "99% <= 1.00",
            "99.9% <= 1.00"
        };
    }

    @Override
    public String[] expectedMeterResult() {
        return new String[] {
            "1/1/70 12:00:05 AM =============================================================",
            "java.lang.Object:",
            "metric:",
            "count = 1",
            "mean rate = Infinity mock/ms",
            "1-minute rate = 0.00 mock/ms", 
            "5-minute rate = 0.00 mock/ms",
            "15-minute rate = 0.00 mock/ms"
        };
    }
    
    @Override
    public String[] expectedTimerResult() {
        return new String[] {
            "1/1/70 12:00:05 AM =============================================================",
            "java.lang.Object:","" +
            "metric:",
            "count = 0",
            "mean rate = 0.00 calls/s",
            "1-minute rate = 0.00 calls/s",
            "5-minute rate = 0.00 calls/s",
            "15-minute rate = 0.00 calls/s",
            "min = 0.00ms",
            "max = 0.00ms",
            "mean = 0.00ms",
            "stddev = 0.00ms",
            "median = 0.00ms",
            "75% <= 0.00ms",
            "95% <= 0.00ms",
            "98% <= 0.00ms",
            "99% <= 0.00ms",
            "99.9% <= 0.00ms"
        };
    }

    @Override
    public String[] expectedGaugeResult(String value) {
        return new String[] {
                "1/1/70 12:00:05 AM =============================================================",
                "java.lang.Object:",
                "metric:",
                String.format("value = %s", value)
        };
    }
}
