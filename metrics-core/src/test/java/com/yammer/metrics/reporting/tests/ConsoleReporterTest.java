package com.yammer.metrics.reporting.tests;

import com.yammer.metrics.core.Clock;
import com.yammer.metrics.core.MetricPredicate;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.reporting.AbstractPollingReporter;
import com.yammer.metrics.reporting.ConsoleReporter;
import org.junit.Test;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.fail;

public class ConsoleReporterTest extends AbstractPollingReporterTest {

    @Override
    protected AbstractPollingReporter createReporter(MetricsRegistry registry, OutputStream out, Clock clock) {
        return new ConsoleReporter(registry,
                                   new PrintStream(out),
                                   MetricPredicate.ALL,
                                   clock,
                                   TimeZone.getTimeZone("UTC"),
                                   Locale.US);
    }

    @Override
    public String[] expectedCounterResult(long count) {
        return new String[]{
                "1/1/70 12:00:05 AM =============================================================",
                "java.lang.Object:",
                "metric:",
                "getCount = " + count
        };
    }

    @Override
    public String[] expectedHistogramResult() {
        return new String[]{
                "1/1/70 12:00:05 AM =============================================================",
                "java.lang.Object:",
                "metric:",
                "getMin = 1.00",
                "getMax = 3.00",
                "getMean = 2.00",
                "stddev = 1.50",
                "median = 0.50",
                "75% <= 0.75",
                "95% <= 0.95",
                "98% <= 0.98",
                "99% <= 0.99",
                "99.9% <= 1.00"
        };
    }

    @Override
    public String[] expectedMeterResult() {
        return new String[]{
                "1/1/70 12:00:05 AM =============================================================",
                "java.lang.Object:",
                "metric:",
                "getCount = 1",
                "getMean rate = 2.00 eventType/s",
                "1-minute rate = 1.00 eventType/s",
                "5-minute rate = 5.00 eventType/s",
                "15-minute rate = 15.00 eventType/s"
        };
    }

    @Override
    public String[] expectedTimerResult() {
        return new String[]{
                "1/1/70 12:00:05 AM =============================================================",
                "java.lang.Object:", "" +
                "metric:",
                "getCount = 1",
                "getMean rate = 2.00 eventType/s",
                "1-minute rate = 1.00 eventType/s",
                "5-minute rate = 5.00 eventType/s",
                "15-minute rate = 15.00 eventType/s",
                "getMin = 1.00ms",
                "getMax = 3.00ms",
                "getMean = 2.00ms",
                "stddev = 1.50ms",
                "median = 0.50ms",
                "75% <= 0.75ms",
                "95% <= 0.95ms",
                "98% <= 0.98ms",
                "99% <= 0.99ms",
                "99.9% <= 1.00ms"
        };
    }

    @Override
    public String[] expectedGaugeResult(String value) {
        return new String[]{
                "1/1/70 12:00:05 AM =============================================================",
                "java.lang.Object:",
                "metric:",
                String.format("value = %s", value)
        };
    }

    @Test
    public void givenShutdownReporterWhenCreatingNewReporterExpectSuccess() {
        try {
            final ConsoleReporter reporter1 = new ConsoleReporter(System.out);
            reporter1.start(1, TimeUnit.SECONDS);
            reporter1.shutdown();
            final ConsoleReporter reporter2 = new ConsoleReporter(System.out);
            reporter2.start(1, TimeUnit.SECONDS);
            reporter2.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
            fail("should be able to start and shutdown reporters");
        }
    }
}
