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
    private final InstrumentedHandler handler = new InstrumentedHandler(registry,
                                                                        new DefaultHandler());

    @Before
    public void setUp() throws Exception {
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
    public void createsMetricsForTheHandler() throws Exception {
        final ContentResponse response = client.GET("http://localhost:" + connector.getLocalPort() + "/hello");

        assertThat(response.getStatus())
                .isEqualTo(404);

        assertThat(registry.getNames())
                .containsOnly(
                        "org.eclipse.jetty.server.handler.DefaultHandler.1xx-responses",
                        "org.eclipse.jetty.server.handler.DefaultHandler.2xx-responses",
                        "org.eclipse.jetty.server.handler.DefaultHandler.3xx-responses",
                        "org.eclipse.jetty.server.handler.DefaultHandler.4xx-responses",
                        "org.eclipse.jetty.server.handler.DefaultHandler.5xx-responses",
                        "org.eclipse.jetty.server.handler.DefaultHandler.requests",
                        "org.eclipse.jetty.server.handler.DefaultHandler.active-suspended",
                        "org.eclipse.jetty.server.handler.DefaultHandler.async-dispatches",
                        "org.eclipse.jetty.server.handler.DefaultHandler.async-timeouts",
                        "org.eclipse.jetty.server.handler.DefaultHandler.get-requests",
                        "org.eclipse.jetty.server.handler.DefaultHandler.put-requests",
                        "org.eclipse.jetty.server.handler.DefaultHandler.active-dispatches",
                        "org.eclipse.jetty.server.handler.DefaultHandler.trace-requests",
                        "org.eclipse.jetty.server.handler.DefaultHandler.other-requests",
                        "org.eclipse.jetty.server.handler.DefaultHandler.connect-requests",
                        "org.eclipse.jetty.server.handler.DefaultHandler.dispatches",
                        "org.eclipse.jetty.server.handler.DefaultHandler.head-requests",
                        "org.eclipse.jetty.server.handler.DefaultHandler.post-requests",
                        "org.eclipse.jetty.server.handler.DefaultHandler.options-requests",
                        "org.eclipse.jetty.server.handler.DefaultHandler.active-requests",
                        "org.eclipse.jetty.server.handler.DefaultHandler.delete-requests"
                );
    }
}
