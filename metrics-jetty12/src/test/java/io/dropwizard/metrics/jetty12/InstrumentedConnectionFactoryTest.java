package io.dropwizard.metrics.jetty12;

import io.dropwizard.metrics5.Counter;
import io.dropwizard.metrics5.MetricRegistry;
import io.dropwizard.metrics5.Timer;
import org.eclipse.jetty.client.ContentResponse;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.io.Content;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.Callback;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class InstrumentedConnectionFactoryTest {
    private final MetricRegistry registry = new MetricRegistry();
    private final Server server = new Server();
    private final ServerConnector connector =
            new ServerConnector(server, new InstrumentedConnectionFactory(new HttpConnectionFactory(),
                    registry.timer("http.connections"),
                    registry.counter("http.active-connections")));
    private final HttpClient client = new HttpClient();

    @BeforeEach
    public void setUp() throws Exception {
        server.setHandler(new Handler.Abstract() {
            @Override
            public boolean handle(Request request, Response response, Callback callback) throws Exception {
                Content.Sink.write(response, true, "OK", callback);
                return true;
            }
        });

        server.addConnector(connector);
        server.start();

        client.start();
    }

    @AfterEach
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
