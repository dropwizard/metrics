package com.yammer.metrics.reporting;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

import com.yammer.metrics.core.Clock;
import com.yammer.metrics.core.CounterMetric;
import com.yammer.metrics.core.GaugeMetric;
import com.yammer.metrics.core.HistogramMetric;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.util.MetricPredicate;

public class GraphiteReporterTest
{
    private MetricsRegistry registry;
    private GraphiteReporter reporter;
    private OutputStream out;

    @Before
    public void init() throws Exception
    {
        registry = new MetricsRegistry();
        out = new ByteArrayOutputStream();

        final Clock clock = mock(Clock.class);
        when(clock.time()).thenReturn(123456L);

        final Socket socket = mock(Socket.class);
        when(socket.getOutputStream()).thenReturn(out);

        final SocketProvider provider = mock(SocketProvider.class);
        when(provider.get()).thenReturn(socket);

        reporter = new GraphiteReporter(registry, "prefix", MetricPredicate.ALL, provider, clock);
        reporter.printVMMetrics = false;
    }

    @Test
    public void canRenderCounter() throws Exception
    {
        final String expected = "prefix.java.lang.Object.test.count 11 123\n";

        CounterMetric metric = registry.newCounter(Object.class, "test");
        metric.inc(11);
        assertOutput(expected);
    }

    @Test
    public void canRenderHistogram() throws Exception
    {
        final String expected = new StringBuilder()
                .append("prefix.java.lang.Object.test.min 10.00 123\n")
                .append("prefix.java.lang.Object.test.max 10.00 123\n")
                .append("prefix.java.lang.Object.test.mean 10.00 123\n")
                .append("prefix.java.lang.Object.test.stddev 0.00 123\n")
                .append("prefix.java.lang.Object.test.median 10.00 123\n")
                .append("prefix.java.lang.Object.test.75percentile 10.00 123\n")
                .append("prefix.java.lang.Object.test.95percentile 10.00 123\n")
                .append("prefix.java.lang.Object.test.98percentile 10.00 123\n")
                .append("prefix.java.lang.Object.test.99percentile 10.00 123\n")
                .append("prefix.java.lang.Object.test.999percentile 10.00 123\n")
                .toString();

        HistogramMetric metric = registry.newHistogram(Object.class, "test");
        metric.update(10);

        assertOutput(expected);
    }

    @Test
    public void canRendererTimed() throws Exception
    {
        final String expected = new StringBuilder()
                .append("prefix.java.lang.Object.testevent.test.count 0 123\n")
                .append("prefix.java.lang.Object.testevent.test.meanRate 0.00 123\n")
                .append("prefix.java.lang.Object.testevent.test.1MinuteRate 0.00 123\n")
                .append("prefix.java.lang.Object.testevent.test.5MinuteRate 0.00 123\n")
                .append("prefix.java.lang.Object.testevent.test.15MinuteRate 0.00 123\n")
                .append("prefix.java.lang.Object.testevent.test.min 0.00 123\n")
                .append("prefix.java.lang.Object.testevent.test.max 0.00 123\n")
                .append("prefix.java.lang.Object.testevent.test.mean 0.00 123\n")
                .append("prefix.java.lang.Object.testevent.test.stddev 0.00 123\n")
                .append("prefix.java.lang.Object.testevent.test.median 0.00 123\n")
                .append("prefix.java.lang.Object.testevent.test.75percentile 0.00 123\n")
                .append("prefix.java.lang.Object.testevent.test.95percentile 0.00 123\n")
                .append("prefix.java.lang.Object.testevent.test.98percentile 0.00 123\n")
                .append("prefix.java.lang.Object.testevent.test.99percentile 0.00 123\n")
                .append("prefix.java.lang.Object.testevent.test.999percentile 0.00 123\n")
                .toString();

        registry.newTimer(Object.class, "test", "testevent");

        assertOutput(expected);
    }

    @Test
    public void canRendererMetered() throws Exception
    {
        final String expected = new StringBuilder()
                .append("prefix.java.lang.Object.test.count 0 123\n")
                .append("prefix.java.lang.Object.test.meanRate 0.00 123\n")
                .append("prefix.java.lang.Object.test.1MinuteRate 0.00 123\n")
                .append("prefix.java.lang.Object.test.5MinuteRate 0.00 123\n")
                .append("prefix.java.lang.Object.test.15MinuteRate 0.00 123\n")
                .toString();

        registry.newMeter(Object.class, "test", "testevent", TimeUnit.SECONDS);

        assertOutput(expected);
    }

    @Test
    public void canRendererGauge() throws Exception
    {
        final String expected = "prefix.java.lang.Object.test.value 5 123\n";

        registry.newGauge(Object.class, "test", new GaugeMetric<Long>()
        {
            @Override
            public Long value()
            {
                return 5l;
            }
        });

        assertOutput(expected);
    }
    
    private void assertOutput(String expected) {
        reporter.run();
        assertEquals(expected, out.toString());
    }
}
