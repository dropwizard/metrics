package com.codahale.metrics.collectd;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import org.collectd.api.ValueList;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import static com.codahale.metrics.collectd.ValueListAssert.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CollectdReporterTest {

    @ClassRule
    public static Receiver receiver = new Receiver(25826);

    private MetricRegistry registry;
    private CollectdReporter reporter;

    @Before
    public void setUp() {
        registry = new MetricRegistry();
        reporter = CollectdReporter.forRegistry(registry).build(new Collectd("localhost", 25826));
    }

    @Test
    public void handlesCounter() throws Exception {
        Counter counter = registry.counter("api.rest.requests");
        counter.inc(42);

        reporter.report();

        ValueList values = receiver.next();
        assertNotNull(values.getHost());
        assertEquals("api", values.getPlugin());
        assertEquals("rest", values.getPluginInstance());
        assertEquals("requests", values.getType());
        assertThat(values.getTypeInstance()).isEmpty();
        assertEquals(42, values.getValues().get(0).intValue());
    }

    @Test
    public void handlesMeter() throws Exception {
        Meter meter = registry.meter("api.rest.requests");
        meter.mark(112);

        reporter.report();

        ValueList values = receiver.next();
        assertNotNull(values.getHost());
        assertEquals("api", values.getPlugin());
        assertEquals("rest", values.getPluginInstance());
        assertEquals("requests", values.getType());
        assertEquals("count", values.getTypeInstance());
        assertThat(receiver.next()).hasType("requests", "m1_rate");
        assertThat(receiver.next()).hasType("requests", "m5_rate");
        assertThat(receiver.next()).hasType("requests", "m15_rate");
        assertThat(receiver.next()).hasType("requests", "mean_rate");
    }

    @Test
    public void handlesGauge() throws Exception {
        Gauge<Double> gauge = registry.register("gauge", new Gauge<Double>() {
            @Override
            public Double getValue() {
                return Double.valueOf(0.25);
            }
        });

        reporter.report();

        ValueList values = receiver.next();
        assertThat(values).fromPlugin("gauge").hasType("gauge");
        assertThat(values.getValues()).hasSize(1).contains(0.25);
    }

    @Test
    public void handlesIntegerGauge() throws Exception {
        Gauge<Integer> gauge = registry.register("gauge", new Gauge<Integer>() {
            @Override
            public Integer getValue() {
                return Integer.valueOf(13);
            }
        });

        reporter.report();

        ValueList values = receiver.next();
        assertThat(values).fromPlugin("gauge").hasType("gauge");
        assertThat(values.getValues()).hasSize(1).contains(13.0);
    }

    @Test
    public void handlesHistogram() throws Exception {
        Histogram histogram = registry.histogram("request.size");
        histogram.update(999);

        reporter.report();

        assertThat(receiver.next()).hasType("size", "count");
        assertThat(receiver.next()).hasType("size", "max");
        assertThat(receiver.next()).hasType("size", "mean");
        assertThat(receiver.next()).hasType("size", "min");
        assertThat(receiver.next()).hasType("size", "stddev");
        assertThat(receiver.next()).hasType("size", "p50");
        assertThat(receiver.next()).hasType("size", "p75");
        assertThat(receiver.next()).hasType("size", "p95");
        assertThat(receiver.next()).hasType("size", "p98");
        assertThat(receiver.next()).hasType("size", "p99");
        assertThat(receiver.next()).hasType("size", "p999");
    }

    @Test
    public void handlesTimer() throws Exception {
        Timer.Context timer = registry.timer("api.rest.request.duration").time();
        timer.stop();

        reporter.report();

        assertThat(receiver.next()).hasType("request.duration", "max");
        assertThat(receiver.next()).hasType("request.duration", "mean");
        assertThat(receiver.next()).hasType("request.duration", "min");
        assertThat(receiver.next()).hasType("request.duration", "stddev");
        assertThat(receiver.next()).hasType("request.duration", "p50");
        assertThat(receiver.next()).hasType("request.duration", "p75");
        assertThat(receiver.next()).hasType("request.duration", "p95");
        assertThat(receiver.next()).hasType("request.duration", "p98");
        assertThat(receiver.next()).hasType("request.duration", "p99");
        assertThat(receiver.next()).hasType("request.duration", "p999");
        assertThat(receiver.next()).hasType("request.duration", "count");
        assertThat(receiver.next()).hasType("request.duration", "m1_rate");
        assertThat(receiver.next()).hasType("request.duration", "m5_rate");
        assertThat(receiver.next()).hasType("request.duration", "m15_rate");
        assertThat(receiver.next()).hasType("request.duration", "mean_rate");
    }


    @Test
    public void sanitizesMetricName() throws Exception {
        Counter counter = registry.counter("dash-illegal.slash/illegal");
        counter.inc();

        reporter.report();

        ValueList values = receiver.next();
        assertThat(values.getPlugin()).isEqualTo("dash_illegal");
        assertThat(values.getType()).isEqualTo("slash_illegal");
    }

}


