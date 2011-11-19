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

public class GraphiteReporterTests
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

        GraphiteReporter graphiteReporter = new GraphiteReporter(registry, "prefix", MetricPredicate.ALL, provider, clock);
        graphiteReporter.printVMMetrics = false;
        reporter = graphiteReporter;
    }

    @Test
    public void canRenderCounter() throws Exception
    {
        String expected = "prefix.com.yammer.metrics.reporting.GraphiteReporterTests.test.count 11 123\n";

        CounterMetric metric = registry.newCounter(getClass(), "test");
        metric.inc(11);
        reporter.run();
        assertEquals(expected, out.toString());
    }

    @Test
    public void canRenderHistogram() throws Exception
    {
        StringBuilder expected = new StringBuilder();

        expected.append("prefix.com.yammer.metrics.reporting.GraphiteReporterTests.test.min 10.00 123\n");
        expected.append("prefix.com.yammer.metrics.reporting.GraphiteReporterTests.test.max 10.00 123\n");
        expected.append("prefix.com.yammer.metrics.reporting.GraphiteReporterTests.test.mean 10.00 123\n");
        expected.append("prefix.com.yammer.metrics.reporting.GraphiteReporterTests.test.stddev 0.00 123\n");
        expected.append("prefix.com.yammer.metrics.reporting.GraphiteReporterTests.test.median 10.00 123\n");
        expected.append("prefix.com.yammer.metrics.reporting.GraphiteReporterTests.test.75percentile 10.00 123\n");
        expected.append("prefix.com.yammer.metrics.reporting.GraphiteReporterTests.test.95percentile 10.00 123\n");
        expected.append("prefix.com.yammer.metrics.reporting.GraphiteReporterTests.test.98percentile 10.00 123\n");
        expected.append("prefix.com.yammer.metrics.reporting.GraphiteReporterTests.test.99percentile 10.00 123\n");
        expected.append("prefix.com.yammer.metrics.reporting.GraphiteReporterTests.test.999percentile 10.00 123\n");

        HistogramMetric metric = registry.newHistogram(getClass(), "test");
        metric.update(10);

        reporter.run();

        assertEquals(expected.toString(), out.toString());
    }

    @Test
    public void canRendererTimed() throws Exception
    {
        StringBuilder expected = new StringBuilder();

        expected.append("prefix.com.yammer.metrics.reporting.GraphiteReporterTests.testevent.test.count 0 123\n");
        expected.append("prefix.com.yammer.metrics.reporting.GraphiteReporterTests.testevent.test.meanRate 0.00 123\n");
        expected.append("prefix.com.yammer.metrics.reporting.GraphiteReporterTests.testevent.test.1MinuteRate 0.00 123\n");
        expected.append("prefix.com.yammer.metrics.reporting.GraphiteReporterTests.testevent.test.5MinuteRate 0.00 123\n");
        expected.append("prefix.com.yammer.metrics.reporting.GraphiteReporterTests.testevent.test.15MinuteRate 0.00 123\n");
        expected.append("prefix.com.yammer.metrics.reporting.GraphiteReporterTests.testevent.test.min 0.00 123\n");
        expected.append("prefix.com.yammer.metrics.reporting.GraphiteReporterTests.testevent.test.max 0.00 123\n");
        expected.append("prefix.com.yammer.metrics.reporting.GraphiteReporterTests.testevent.test.mean 0.00 123\n");
        expected.append("prefix.com.yammer.metrics.reporting.GraphiteReporterTests.testevent.test.stddev 0.00 123\n");
        expected.append("prefix.com.yammer.metrics.reporting.GraphiteReporterTests.testevent.test.median 0.00 123\n");
        expected.append("prefix.com.yammer.metrics.reporting.GraphiteReporterTests.testevent.test.75percentile 0.00 123\n");
        expected.append("prefix.com.yammer.metrics.reporting.GraphiteReporterTests.testevent.test.95percentile 0.00 123\n");
        expected.append("prefix.com.yammer.metrics.reporting.GraphiteReporterTests.testevent.test.98percentile 0.00 123\n");
        expected.append("prefix.com.yammer.metrics.reporting.GraphiteReporterTests.testevent.test.99percentile 0.00 123\n");
        expected.append("prefix.com.yammer.metrics.reporting.GraphiteReporterTests.testevent.test.999percentile 0.00 123\n");

        registry.newTimer(getClass(), "test", "testevent");

        reporter.run();

        assertEquals(expected.toString(), out.toString());
    }

    @Test
    public void canRendererMetered() throws Exception
    {
        StringBuilder expected = new StringBuilder();

        expected.append("prefix.com.yammer.metrics.reporting.GraphiteReporterTests.test.count 0 123\n");
        expected.append("prefix.com.yammer.metrics.reporting.GraphiteReporterTests.test.meanRate 0.00 123\n");
        expected.append("prefix.com.yammer.metrics.reporting.GraphiteReporterTests.test.1MinuteRate 0.00 123\n");
        expected.append("prefix.com.yammer.metrics.reporting.GraphiteReporterTests.test.5MinuteRate 0.00 123\n");
        expected.append("prefix.com.yammer.metrics.reporting.GraphiteReporterTests.test.15MinuteRate 0.00 123\n");

        registry.newMeter(getClass(), "test", "testevent", TimeUnit.SECONDS);

        reporter.run();

        assertEquals(expected.toString(), out.toString());
    }

    @Test
    public void canRendererGauge() throws Exception
    {
        String expected = "prefix.com.yammer.metrics.reporting.GraphiteReporterTests.test.value 5 123\n";

        registry.newGauge(getClass(), "test", new GaugeMetric<Long>()
        {
            @Override
            public Long value()
            {
                return 5l;
            }
        });

        reporter.run();

        assertEquals(expected, out.toString());
    }
}
