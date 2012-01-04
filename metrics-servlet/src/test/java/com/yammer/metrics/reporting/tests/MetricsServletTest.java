package com.yammer.metrics.reporting.tests;

import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.core.HealthCheckRegistry;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.reporting.MetricsServlet;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class MetricsServletTest {
    private MetricsRegistry registry;
    private MetricsServlet servlet;
    private StringWriter result;
    private JsonGenerator json;

    @Before
    public void setUp() throws Exception {
        this.registry = new MetricsRegistry();
        this.result = new StringWriter();
        this.json = new JsonFactory(new ObjectMapper()).createJsonGenerator(result);
        this.servlet = new MetricsServlet(registry, new HealthCheckRegistry(), "", "", "", "", false);
    }

    @After
    public void tearDown() {
        registry.shutdown();
    }

    @Test
    public void gaugesAreWrittenAsJSON() throws IOException {
        registry.newGauge(Object.class, "test", new Gauge<String>() {
            @Override
            public String value() {
                return "foo";
            }
        });
        assertJson("{\"java.lang.Object\":{\"test\":{\"type\":\"gauge\",\"value\":\"foo\"}}}");
    }

    @Test
    public void countersAreWrittenAsJSON() throws IOException {
        registry.newCounter(Object.class, "test").inc(50);
        assertJson("{\"java.lang.Object\":{\"test\":{\"type\":\"counter\",\"count\":50}}}");
    }

    @Test
    public void histogramsAreWrittenAsJSON() throws Exception {
        registry.newHistogram(Object.class, "histogram");

        assertJson("{\"java.lang.Object\":{\"histogram\":{\"type\":\"histogram\",\"count\":0," +
                           "\"min\":0.0,\"max\":0.0,\"mean\":0.0,\"std_dev\":0.0,\"median\":0.0," +
                           "\"p75\":0.0,\"p95\":0.0,\"p98\":0.0,\"p99\":0.0,\"p999\":0.0}}}");
    }

    @Test
    public void metersAreWrittenAsJSON() throws Exception {
        registry.newMeter(Object.class, "meter", "things", TimeUnit.SECONDS);

        assertJson("{\"java.lang.Object\":{\"meter\":{\"type\":\"meter\",\"event_type\":\"things\"," +
                           "\"unit\":\"seconds\",\"count\":0,\"mean\":0.0,\"m1\":0.0," +
                           "\"m5\":0.0,\"m15\":0.0}}}");
    }

    @Test
    public void timersAreWrittenAsJSON() throws Exception {
        registry.newTimer(Object.class, "timer", TimeUnit.MICROSECONDS, TimeUnit.MINUTES);

        assertJson("{\"java.lang.Object\":{\"timer\":{\"type\":\"timer\",\"duration\":" +
                           "{\"unit\":\"microseconds\",\"min\":0.0,\"max\":0.0,\"mean\":0.0," +
                           "\"std_dev\":0.0,\"median\":0.0,\"p75\":0.0,\"p95\":0.0,\"p98\":0.0," +
                           "\"p99\":0.0,\"p999\":0.0},\"rate\":{\"unit\":\"minutes\",\"count\":0," +
                           "\"mean\":0.0,\"m1\":0.0,\"m5\":0.0,\"m15\":0.0}}}}");
    }

    private void assertJson(String expected) throws IOException {
        json.writeStartObject();
        servlet.writeRegularMetrics(json, null, false);
        json.writeEndObject();
        json.close();
        assertThat(new ObjectMapper().readTree(result.toString()),
                   is(new ObjectMapper().readTree(expected)));
    }
}
