package com.yammer.metrics.reporting;

import com.yammer.metrics.core.Clock;
import com.yammer.metrics.core.MetricPredicate;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.reporting.tests.AbstractPollingReporterTest;

import java.io.OutputStream;
import java.net.DatagramSocket;
import java.net.Socket;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class StatsdReporterTest extends AbstractPollingReporterTest {
    @Override
    protected AbstractPollingReporter createReporter(MetricsRegistry registry, OutputStream out, Clock clock) throws Exception {
        final Socket socket = mock(Socket.class);
        when(socket.getOutputStream()).thenReturn(out);

        final StatsdReporter.UDPSocketProvider provider = mock(StatsdReporter.UDPSocketProvider.class);
        when(provider.get()).thenReturn(socket);

        final StatsdReporter reporter = new StatsdReporter(registry,
                                                           "prefix",
                                                           MetricPredicate.ALL,
                                                           provider,
                                                           clock);
        reporter.setPrintVMMetrics(false);
        return reporter;
    }

    @Override
    public String[] expectedGaugeResult(String value) {
        return new String[]{String.format("prefix.java.lang.Object.metric.count:%s|g", value)};
    }

    @Override
    public String[] expectedTimerResult() {
        return new String[]{
                "prefix.java.lang.Object.metric.count:1|g",
                "prefix.java.lang.Object.metric.meanRate:2.00|ms",
                "prefix.java.lang.Object.metric.1MinuteRate:1.00|ms",
                "prefix.java.lang.Object.metric.5MinuteRate:5.00|ms",
                "prefix.java.lang.Object.metric.15MinuteRate:15.00|ms",
                "prefix.java.lang.Object.metric.min:1.00|ms",
                "prefix.java.lang.Object.metric.max:3.00|ms",
                "prefix.java.lang.Object.metric.mean:2.00|ms",
                "prefix.java.lang.Object.metric.stddev:1.50|ms",
                "prefix.java.lang.Object.metric.median:0.50|ms",
                "prefix.java.lang.Object.metric.75percentile:0.75|ms",
                "prefix.java.lang.Object.metric.95percentile:0.95|ms",
                "prefix.java.lang.Object.metric.98percentile:0.98|ms",
                "prefix.java.lang.Object.metric.99percentile:0.99|ms",
                "prefix.java.lang.Object.metric.999percentile:1.00|ms"
        };
    }

    @Override
    public String[] expectedMeterResult() {
        return new String[]{
                "prefix.java.lang.Object.metric.count:1|g",
                "prefix.java.lang.Object.metric.meanRate:2.00|ms",
                "prefix.java.lang.Object.metric.1MinuteRate:1.00|ms",
                "prefix.java.lang.Object.metric.5MinuteRate:5.00|ms",
                "prefix.java.lang.Object.metric.15MinuteRate:15.00|ms",
        };
    }

    @Override
    public String[] expectedHistogramResult() {
        return new String[]{
                "prefix.java.lang.Object.metric.min:1.00|ms",
                "prefix.java.lang.Object.metric.max:3.00|ms",
                "prefix.java.lang.Object.metric.mean:2.00|ms",
                "prefix.java.lang.Object.metric.stddev:1.50|ms",
                "prefix.java.lang.Object.metric.median:0.50|ms",
                "prefix.java.lang.Object.metric.75percentile:0.75|ms",
                "prefix.java.lang.Object.metric.95percentile:0.95|ms",
                "prefix.java.lang.Object.metric.98percentile:0.98|ms",
                "prefix.java.lang.Object.metric.99percentile:0.99|ms",
                "prefix.java.lang.Object.metric.999percentile:1.00|ms"
        };
    }

    @Override
    public String[] expectedCounterResult(long count) {
        return new String[]{
                String.format("prefix.java.lang.Object.metric.count:%d|g", count)
        };
    }
}
