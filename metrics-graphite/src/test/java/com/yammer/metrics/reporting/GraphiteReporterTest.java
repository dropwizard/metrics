package com.yammer.metrics.reporting;

import com.yammer.metrics.core.Clock;
import com.yammer.metrics.core.MetricPredicate;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.reporting.tests.AbstractPollingReporterTest;

import java.io.OutputStream;
import java.net.Socket;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GraphiteReporterTest extends AbstractPollingReporterTest {
    @Override
    protected AbstractPollingReporter createReporter(MetricsRegistry registry, OutputStream out, Clock clock) throws Exception {
        final Socket socket = mock(Socket.class);
        when(socket.getOutputStream()).thenReturn(out);

        final SocketProvider provider = mock(SocketProvider.class);
        when(provider.get()).thenReturn(socket);

        final GraphiteReporter reporter = new GraphiteReporter(registry,
                                                               "prefix",
                                                               MetricPredicate.ALL,
                                                               provider,
                                                               clock);
        reporter.printVMMetrics = false;
        return reporter;
    }

    @Override
    public String[] expectedGaugeResult(String value) {
        return new String[]{String.format("prefix.java.lang.Object.metric.value %s 5", value)};
    }

    @Override
    public String[] expectedTimerResult() {
        return new String[]{
                "prefix.java.lang.Object.metric.count 1 5",
                "prefix.java.lang.Object.metric.meanRate 2.00 5",
                "prefix.java.lang.Object.metric.1MinuteRate 1.00 5",
                "prefix.java.lang.Object.metric.5MinuteRate 5.00 5",
                "prefix.java.lang.Object.metric.15MinuteRate 15.00 5",
                "prefix.java.lang.Object.metric.min 1.00 5",
                "prefix.java.lang.Object.metric.max 3.00 5",
                "prefix.java.lang.Object.metric.mean 2.00 5",
                "prefix.java.lang.Object.metric.stddev 1.50 5",
                "prefix.java.lang.Object.metric.median 0.50 5",
                "prefix.java.lang.Object.metric.75percentile 0.75 5",
                "prefix.java.lang.Object.metric.95percentile 0.95 5",
                "prefix.java.lang.Object.metric.98percentile 0.98 5",
                "prefix.java.lang.Object.metric.99percentile 0.99 5",
                "prefix.java.lang.Object.metric.999percentile 1.00 5"
        };
    }

    @Override
    public String[] expectedMeterResult() {
        return new String[]{
                "prefix.java.lang.Object.metric.count 1 5",
                "prefix.java.lang.Object.metric.meanRate 2.00 5",
                "prefix.java.lang.Object.metric.1MinuteRate 1.00 5",
                "prefix.java.lang.Object.metric.5MinuteRate 5.00 5",
                "prefix.java.lang.Object.metric.15MinuteRate 15.00 5",
        };
    }

    @Override
    public String[] expectedHistogramResult() {
        return new String[]{
                "prefix.java.lang.Object.metric.min 1.00 5",
                "prefix.java.lang.Object.metric.max 3.00 5",
                "prefix.java.lang.Object.metric.mean 2.00 5",
                "prefix.java.lang.Object.metric.stddev 1.50 5",
                "prefix.java.lang.Object.metric.median 0.50 5",
                "prefix.java.lang.Object.metric.75percentile 0.75 5",
                "prefix.java.lang.Object.metric.95percentile 0.95 5",
                "prefix.java.lang.Object.metric.98percentile 0.98 5",
                "prefix.java.lang.Object.metric.99percentile 0.99 5",
                "prefix.java.lang.Object.metric.999percentile 1.00 5"
        };
    }

    @Override
    public String[] expectedCounterResult(long count) {
        return new String[]{
                String.format("prefix.java.lang.Object.metric.count %d 5", count)
        };
    }
}
