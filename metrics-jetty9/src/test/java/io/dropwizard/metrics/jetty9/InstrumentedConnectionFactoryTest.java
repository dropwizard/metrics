package io.dropwizard.metrics.jetty9;

import static io.dropwizard.metrics.MetricRegistry.name;

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

import io.dropwizard.metrics.jetty9.InstrumentedConnectionFactory;

import io.dropwizard.metrics.MetricRegistry;
import io.dropwizard.metrics.Timer;

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
                                                                          registry.timer("http.connections")));
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

        final Timer timer = registry.timer(name("http.connections"));
        assertThat(timer.getCount())
                .isEqualTo(1);
    }
}
