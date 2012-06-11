package com.yammer.metrics.reporting;


import com.yammer.metrics.core.Clock;
import com.yammer.metrics.core.MetricPredicate;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.core.VirtualMachineMetrics;
import com.yammer.metrics.reporting.AbstractPollingReporter;
import com.yammer.metrics.reporting.SocketProvider;
import com.yammer.metrics.reporting.TsdbReporter;
import com.yammer.metrics.reporting.tests.AbstractPollingReporterTest;

import java.io.OutputStream;
import java.net.Socket;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TSDBReporterTest extends AbstractPollingReporterTest {
    @Override
    protected AbstractPollingReporter createReporter(MetricsRegistry registry, OutputStream out, Clock clock) throws Exception {
        final Socket socket = mock(Socket.class);
        when(socket.getOutputStream()).thenReturn(out);

        final SocketProvider provider = mock(SocketProvider.class);
        when(provider.get()).thenReturn(socket);

        final TsdbReporter reporter = new TsdbReporter(registry,
                                                               "prefix",
                                                               MetricPredicate.ALL,
                                                               provider,
                                                               clock,"wally");
        reporter.printVMMetrics = false;
        return reporter;
    }

    @Override
    public String[] expectedGaugeResult(String value) {
        return new String[]{String.format("put prefix.java.lang.Object.value 5 %s name=metric host=wally", value)};
    }

    @Override
    public String[] expectedTimerResult() {
        return new String[]{
                "put prefix.java.lang.Object.count 5 1 name=metric host=wally",
                "put prefix.java.lang.Object.meanRate 5 2.000000 name=metric host=wally",
                "put prefix.java.lang.Object.1MinuteRate 5 1.000000 name=metric host=wally",
                "put prefix.java.lang.Object.5MinuteRate 5 5.000000 name=metric host=wally",
                "put prefix.java.lang.Object.15MinuteRate 5 15.000000 name=metric host=wally",
                "put prefix.java.lang.Object.min 5 1.000000 name=metric host=wally",
                "put prefix.java.lang.Object.max 5 3.000000 name=metric host=wally",
                "put prefix.java.lang.Object.mean 5 2.000000 name=metric host=wally",
                "put prefix.java.lang.Object.stddev 5 1.500000 name=metric host=wally",
                "put prefix.java.lang.Object.median 5 0.499500 name=metric host=wally",
                "put prefix.java.lang.Object.75percentile 5 0.749750 name=metric host=wally",
                "put prefix.java.lang.Object.95percentile 5 0.949950 name=metric host=wally",
                "put prefix.java.lang.Object.98percentile 5 0.979980 name=metric host=wally",
                "put prefix.java.lang.Object.99percentile 5 0.989990 name=metric host=wally",
                "put prefix.java.lang.Object.999percentile 5 0.998999 name=metric host=wally"
        };
    }

    @Override
    public String[] expectedMeterResult() {
        return new String[]{
                "put prefix.java.lang.Object.count 5 1 name=metric host=wally",
                "put prefix.java.lang.Object.meanRate 5 2.000000 name=metric host=wally",
                "put prefix.java.lang.Object.1MinuteRate 5 1.000000 name=metric host=wally",
                "put prefix.java.lang.Object.5MinuteRate 5 5.000000 name=metric host=wally",
                "put prefix.java.lang.Object.15MinuteRate 5 15.000000 name=metric host=wally",
        };
    }

    @Override
    public String[] expectedHistogramResult() {
        return new String[]{
                "put prefix.java.lang.Object.min 5 1.000000 name=metric host=wally",
                "put prefix.java.lang.Object.max 5 3.000000 name=metric host=wally",
                "put prefix.java.lang.Object.mean 5 2.000000 name=metric host=wally",
                "put prefix.java.lang.Object.stddev 5 1.500000 name=metric host=wally",
                "put prefix.java.lang.Object.median 5 0.499500 name=metric host=wally",
                "put prefix.java.lang.Object.75percentile 5 0.749750 name=metric host=wally",
                "put prefix.java.lang.Object.95percentile 5 0.949950 name=metric host=wally",
                "put prefix.java.lang.Object.98percentile 5 0.979980 name=metric host=wally",
                "put prefix.java.lang.Object.99percentile 5 0.989990 name=metric host=wally",
                "put prefix.java.lang.Object.999percentile 5 0.998999 name=metric host=wally"
        };
    }

    @Override
    public String[] expectedCounterResult(long count) {
        return new String[]{
                String.format("put prefix.java.lang.Object.count 5 %d name=metric host=wally", count)
        };
    }
}
