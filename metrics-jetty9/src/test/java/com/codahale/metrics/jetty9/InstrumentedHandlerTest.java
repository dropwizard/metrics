package com.codahale.metrics.jetty9;

import com.codahale.metrics.MetricName;
import com.codahale.metrics.MetricRegistry;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class InstrumentedHandlerTest {
    private final HttpClient client = new HttpClient();
    private final MetricRegistry registry = new MetricRegistry();
    private final Server server = new Server();
    private final ServerConnector connector = new ServerConnector(server);
    private final InstrumentedHandler handler = new InstrumentedHandler(registry);

    @Before
    public void setUp() throws Exception {
        handler.setName("handler");
        handler.setHandler(new DefaultHandler());
        server.addConnector(connector);
        server.setHandler(handler);
        server.start();
        client.start();
    }

    @After
    public void tearDown() throws Exception {
        server.stop();
        client.stop();
    }

    @Test
    public void hasAName() throws Exception {
        assertThat(handler.getName())
                .isEqualTo("handler");
    }

    @Test
    public void createsMetricsForTheHandler() throws Exception {
        final ContentResponse response = client.GET("http://localhost:" + connector.getLocalPort() + "/hello");

        assertThat(response.getStatus())
                .isEqualTo(404);

        final MetricName prefix = MetricName.build("org.eclipse.jetty.server.handler.DefaultHandler.handler");
        
        assertThat(registry.getNames())
                .containsOnly(
                        prefix.resolve("1xx-responses"),
                        prefix.resolve("2xx-responses"),
                        prefix.resolve("3xx-responses"),
                        prefix.resolve("4xx-responses"),
                        prefix.resolve("5xx-responses"),
                        prefix.resolve("requests"),
                        prefix.resolve("active-suspended"),
                        prefix.resolve("async-dispatches"),
                        prefix.resolve("async-timeouts"),
                        prefix.resolve("get-requests"),
                        prefix.resolve("put-requests"),
                        prefix.resolve("active-dispatches"),
                        prefix.resolve("trace-requests"),
                        prefix.resolve("other-requests"),
                        prefix.resolve("connect-requests"),
                        prefix.resolve("dispatches"),
                        prefix.resolve("head-requests"),
                        prefix.resolve("post-requests"),
                        prefix.resolve("options-requests"),
                        prefix.resolve("active-requests"),
                        prefix.resolve("delete-requests"),
                        prefix.resolve("move-requests")
                );
    }
}
