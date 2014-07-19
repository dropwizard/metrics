package com.codahale.metrics.jetty9;

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

        assertThat(registry.getNames())
                .containsOnly(
                        "org.eclipse.jetty.server.handler.DefaultHandler.handler.1xx-responses",
                        "org.eclipse.jetty.server.handler.DefaultHandler.handler.2xx-responses",
                        "org.eclipse.jetty.server.handler.DefaultHandler.handler.3xx-responses",
                        "org.eclipse.jetty.server.handler.DefaultHandler.handler.4xx-responses",
                        "org.eclipse.jetty.server.handler.DefaultHandler.handler.5xx-responses",
                        "org.eclipse.jetty.server.handler.DefaultHandler.handler.requests",
                        "org.eclipse.jetty.server.handler.DefaultHandler.handler.active-suspended",
                        "org.eclipse.jetty.server.handler.DefaultHandler.handler.async-dispatches",
                        "org.eclipse.jetty.server.handler.DefaultHandler.handler.async-timeouts",
                        "org.eclipse.jetty.server.handler.DefaultHandler.handler.get-requests",
                        "org.eclipse.jetty.server.handler.DefaultHandler.handler.put-requests",
                        "org.eclipse.jetty.server.handler.DefaultHandler.handler.active-dispatches",
                        "org.eclipse.jetty.server.handler.DefaultHandler.handler.trace-requests",
                        "org.eclipse.jetty.server.handler.DefaultHandler.handler.other-requests",
                        "org.eclipse.jetty.server.handler.DefaultHandler.handler.connect-requests",
                        "org.eclipse.jetty.server.handler.DefaultHandler.handler.dispatches",
                        "org.eclipse.jetty.server.handler.DefaultHandler.handler.head-requests",
                        "org.eclipse.jetty.server.handler.DefaultHandler.handler.post-requests",
                        "org.eclipse.jetty.server.handler.DefaultHandler.handler.options-requests",
                        "org.eclipse.jetty.server.handler.DefaultHandler.handler.active-requests",
                        "org.eclipse.jetty.server.handler.DefaultHandler.handler.delete-requests",
                        "org.eclipse.jetty.server.handler.DefaultHandler.handler.move-requests"
                );
    }
}
