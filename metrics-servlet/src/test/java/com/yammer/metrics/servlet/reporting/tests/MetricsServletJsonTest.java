package com.yammer.metrics.servlet.reporting.tests;

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

import static org.junit.Assert.assertEquals;

public class MetricsServletJsonTest {

    @Test
    public void counter() throws IOException {
        registry.newCounter(Object.class, "test").inc(50);
        assertJson("{\"java.lang.Object\":{\"test\":{\"type\":\"counter\",\"count\":50}}}");
    }

    @Test
    public void gauge() throws IOException {
        registry.newGauge(Object.class, "test", new Gauge<String>() {
            @Override
            public String value() {
                return "foo";
            }
        });
        assertJson("{\"java.lang.Object\":{\"test\":{\"type\":\"gauge\",\"value\":\"foo\"}}}");
    }

    private void assertJson(String expected) throws IOException {
        json.writeStartObject();
        servlet.writeRegularMetrics(json, null, false);
        json.writeEndObject();
        json.close();
        assertEquals(new ObjectMapper().readTree(expected),
                     new ObjectMapper().readTree(result.toString()));
    }

    @Before
    public void init() throws IOException {
        registry = new MetricsRegistry();
        result = new StringWriter();
        json = new JsonFactory(new ObjectMapper()).createJsonGenerator(result);
        servlet = new MetricsServlet(registry, new HealthCheckRegistry(), "", "", "", "", false);
    }

    @After
    public void tearDown() {
        registry.shutdown();
    }

    private MetricsRegistry registry;
    private MetricsServlet servlet;
    private StringWriter result;
    private JsonGenerator json;
}
