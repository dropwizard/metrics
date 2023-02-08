package io.dropwizard.metrics5.jetty11;

import io.dropwizard.metrics5.Counter;
import io.dropwizard.metrics5.MetricRegistry;
import io.dropwizard.metrics5.Timer;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.PrintWriter;

import static org.assertj.core.api.Assertions.assertThat;

class InstrumentedConnectionFactoryTest {
    private final MetricRegistry registry = new MetricRegistry();
    private final Server server = new Server();
    private final ServerConnector connector =
            new ServerConnector(server, new InstrumentedConnectionFactory(new HttpConnectionFactory(),
                    registry.timer("http.connections"),
                    registry.counter("http.active-connections")));
    private final HttpClient client = new HttpClient();

    @BeforeEach
    void setUp() throws Exception {
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

    @AfterEach
    void tearDown() throws Exception {
        server.stop();
        client.stop();
    }

    @Test
    void instrumentsConnectionTimes() throws Exception {
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
    void instrumentsActiveConnections() throws Exception {
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
