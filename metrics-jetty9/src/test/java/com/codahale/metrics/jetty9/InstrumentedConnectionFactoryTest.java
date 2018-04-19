package com.codahale.metrics.jetty9;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

import static org.assertj.core.api.Assertions.assertThat;

public class InstrumentedConnectionFactoryTest {
    private final MetricRegistry registry = new MetricRegistry();
    private final Server server = new Server();
    private final ServerConnector connector =
            new ServerConnector(server, new InstrumentedConnectionFactory(new HttpConnectionFactory(),
                    registry.timer("http.connections"),
                    registry.counter("http.active-connections")));
    private final HttpClient client = new HttpClient();

    @Before
    public void setUp() throws Exception {
        server.setHandler(new AbstractHandler() {
            @Override
            public void handle(String target,
                               Request baseRequest,
                               HttpServletRequest request,
                               HttpServletResponse response) throws IOException, ServletException {
                try (PrintWriter writer = response.getWriter()) {
                    writer.println("OK");
                }
            }
        });

        server.addConnector(connector);
        server.start();

        client.start();
    }

    @After
    public void tearDown() throws Exception {
        server.stop();
        client.stop();
    }

    @Test
    public void instrumentsConnectionTimes() throws Exception {
        final ContentResponse response = client.GET("http://localhost:" + connector.getLocalPort() + "/hello");
        assertThat(response.getStatus())
                .isEqualTo(200);

        client.stop(); // close the connection

        Thread.sleep(100); // make sure the connection is closed

        final Timer timer = registry.timer(MetricRegistry.name("http.connections"));
        assertThat(timer.getCount())
                .isEqualTo(1);
    }

    @Test
    public void instrumentsActiveConnections() throws Exception {
        final Counter counter = registry.counter("http.active-connections");

        final ContentResponse response = client.GET("http://localhost:" + connector.getLocalPort() + "/hello");
        assertThat(response.getStatus())
                .isEqualTo(200);

        assertThat(counter.getCount())
                .isEqualTo(1);

        client.stop(); // close the connection

        Thread.sleep(100); // make sure the connection is closed

        assertThat(counter.getCount())
                .isEqualTo(0);
    }
}
