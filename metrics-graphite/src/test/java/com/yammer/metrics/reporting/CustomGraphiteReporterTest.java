package com.yammer.metrics.reporting;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.OutputStream;
import java.net.Socket;

import com.yammer.metrics.core.Clock;
import com.yammer.metrics.core.CounterMetric;
import com.yammer.metrics.core.GaugeMetric;
import com.yammer.metrics.core.HistogramMetric;
import com.yammer.metrics.core.MeterMetric;
import com.yammer.metrics.core.Metered;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.core.TimerMetric;
import com.yammer.metrics.reporting.tests.AbstractPollingReporterTest;
import com.yammer.metrics.util.MetricPredicate;

public class CustomGraphiteReporterTest extends AbstractPollingReporterTest {

    @Override
    protected AbstractPollingReporter createReporter(MetricsRegistry registry, OutputStream out, Clock clock) throws Exception {
        final Socket socket = mock(Socket.class);
        when(socket.getOutputStream()).thenReturn(out);

        final SocketProvider provider = mock(SocketProvider.class);
        when(provider.get()).thenReturn(socket);

        final GraphiteReporter reporter = new GraphiteReporter(registry, "prefix", MetricPredicate.ALL, provider, clock);

        reporter.registerRenderer(CounterMetric.class, new GraphiteMetricRenderer<CounterMetric>() {
            @Override
            public void renderMetric(MetricName name, CounterMetric counter, GraphiteRendererContext context) {
                sendToGraphite(String.format("%s%s.count %d %d", context.prefix, sanitizeName(name), counter.count(), context.epoch), context);
            }
        });
        reporter.registerRenderer(HistogramMetric.class, new GraphiteMetricRenderer<HistogramMetric>() {
            @Override
            public void renderMetric(MetricName name, HistogramMetric histogram, GraphiteRendererContext context) {
                sendToGraphite(String.format("%s%s.stddev %2.2f %d", context.prefix, sanitizeName(name), histogram.stdDev(), context.epoch), context);
            }
        });
        reporter.registerRenderer(MeterMetric.class, new GraphiteMetricRenderer<Metered>() {
            @Override
            public void renderMetric(MetricName name, Metered meter, GraphiteRendererContext context) {
                sendToGraphite(String.format("%s%s.count %d %d\n", context.prefix, sanitizeName(name), meter.count(), context.epoch), context);
            }
        });
        reporter.registerRenderer(TimerMetric.class, new GraphiteMetricRenderer<TimerMetric>() {
            @Override
            public void renderMetric(MetricName name, TimerMetric timer, GraphiteRendererContext context) {
                sendToGraphite(String.format("%s%s.min %2.2f %d", context.prefix, sanitizeName(name), timer.min(), context.epoch), context);
            }
        });
        reporter.registerRenderer(GaugeMetric.class, new GraphiteMetricRenderer<GaugeMetric<?>>() {
            @Override
            public void renderMetric(MetricName name, GaugeMetric<?> metric, GraphiteRendererContext context) {
                sendToGraphite(String.format("%s%s.value %s %d", context.prefix, sanitizeName(name), metric.value(), context.epoch), context);
            }
        });

        reporter.printVMMetrics = false;
        return reporter;
    }

    @Override
    public String[] expectedGaugeResult(String value) {
        return new String[] { String.format("prefix.java.lang.Object.metric.value %s 5", value) };
    }

    @Override
    public String[] expectedTimerResult() {
        return new String[] { "prefix.java.lang.Object.metric.count 1 5", "prefix.java.lang.Object.metric.min 1.00 5" };
    }

    @Override
    public String[] expectedMeterResult() {
        return new String[] { "prefix.java.lang.Object.metric.count 1 5" };
    }

    @Override
    public String[] expectedHistogramResult() {
        return new String[] { "prefix.java.lang.Object.metric.stddev 1.50 5" };
    }

    @Override
    public String[] expectedCounterResult(long count) {
        return new String[] { String.format("prefix.java.lang.Object.metric.count %d 5", count) };
    }

}
