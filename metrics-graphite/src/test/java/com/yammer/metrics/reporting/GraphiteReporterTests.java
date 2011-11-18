package com.yammer.metrics.reporting;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.yammer.metrics.core.Clock;
import com.yammer.metrics.core.CounterMetric;
import com.yammer.metrics.core.GaugeMetric;
import com.yammer.metrics.core.HistogramMetric;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.util.MetricPredicate;

public class GraphiteReporterTests
{
    private static GraphiteReporter getMockGraphiteReporter(final OutputStream outputStream, MetricsRegistry metricsRegistry) throws IOException
    {
        GraphiteReporter graphiteReporter = new GraphiteReporter(metricsRegistry, "prefix", MetricPredicate.ALL, new SocketProvider()
        {
            @Override
            public Socket get() throws Exception
            {
                Socket socket = mock(Socket.class);

                when(socket.getOutputStream()).thenReturn(outputStream);

                return socket;
            }
        }, new Clock()
        {
            @Override
            public long tick()
            {
                return 123000;
            }
        });

        graphiteReporter.printVMMetrics = false;

        return graphiteReporter;
    }

    @Test
    public void canRenderCounter() throws IOException
    {
        StringBuilder expected = new StringBuilder();

        expected.append("prefix.com.yammer.metrics.reporting.GraphiteReporterTests.test.count 11 123\n");

        MetricsRegistry metricsRegistry = new MetricsRegistry();
        CounterMetric metric = metricsRegistry.newCounter(getClass(), "test");
        metric.inc(11);

        OutputStream outputStream = new ByteArrayOutputStream();

        getMockGraphiteReporter(outputStream, metricsRegistry).run();

        assertEquals(expected.toString(), outputStream.toString());
    }

    @Test
    public void canRenderHistogram() throws IOException
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

        MetricsRegistry metricsRegistry = new MetricsRegistry();
        HistogramMetric metric = metricsRegistry.newHistogram(getClass(), "test");
        metric.update(10);

        OutputStream outputStream = new ByteArrayOutputStream();

        getMockGraphiteReporter(outputStream, metricsRegistry).run();

        assertEquals(expected.toString(), outputStream.toString());
    }

    @Test
    public void canRendererTimed() throws IOException
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

        MetricsRegistry metricsRegistry = new MetricsRegistry();
        metricsRegistry.newTimer(getClass(), "test", "testevent");

        OutputStream outputStream = new ByteArrayOutputStream();

        getMockGraphiteReporter(outputStream, metricsRegistry).run();

        assertEquals(expected.toString(), outputStream.toString());
    }

    @Test
    public void canRendererMetered() throws IOException
    {
        StringBuilder expected = new StringBuilder();

        expected.append("prefix.com.yammer.metrics.reporting.GraphiteReporterTests.test.count 0 123\n");
        expected.append("prefix.com.yammer.metrics.reporting.GraphiteReporterTests.test.meanRate 0.00 123\n");
        expected.append("prefix.com.yammer.metrics.reporting.GraphiteReporterTests.test.1MinuteRate 0.00 123\n");
        expected.append("prefix.com.yammer.metrics.reporting.GraphiteReporterTests.test.5MinuteRate 0.00 123\n");
        expected.append("prefix.com.yammer.metrics.reporting.GraphiteReporterTests.test.15MinuteRate 0.00 123\n");

        MetricsRegistry metricsRegistry = new MetricsRegistry();
        metricsRegistry.newMeter(getClass(), "test", "testevent", TimeUnit.SECONDS);

        OutputStream outputStream = new ByteArrayOutputStream();

        getMockGraphiteReporter(outputStream, metricsRegistry).run();

        assertEquals(expected.toString(), outputStream.toString());
    }

    @Test
    public void canRendererGauge() throws IOException
    {
        String expected = "prefix.com.yammer.metrics.reporting.GraphiteReporterTests.test.value 5 123\n";

        MetricsRegistry metricsRegistry = new MetricsRegistry();

        metricsRegistry.newGauge(getClass(), "test", new GaugeMetric<Long>()
        {
            @Override
            public Long value()
            {
                return 5l;
            }
        });

        OutputStream outputStream = new ByteArrayOutputStream();

        getMockGraphiteReporter(outputStream, metricsRegistry).run();

        assertEquals(expected, outputStream.toString());
    }
}
