package io.dropwizard.metrics.jetty12.ee10;

import com.codahale.metrics.MetricRegistry;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.junit.After;
import org.junit.Before;

import static com.codahale.metrics.annotation.ResponseMeteredLevel.ALL;

abstract class AbstractIntegrationTest {

    protected final HttpClient client = new HttpClient();
    protected final MetricRegistry registry = new MetricRegistry();
    protected final Server server = new Server();
    protected final ServerConnector connector = new ServerConnector(server);
    protected final InstrumentedEE10Handler handler = new InstrumentedEE10Handler(registry, null, ALL);
    protected final ServletContextHandler servletContextHandler = new ServletContextHandler();

    @Before
    public void setUp() throws Exception {
        handler.setName("handler");

        // builds the following handler chain:
        // ServletContextHandler -> InstrumentedHandler -> TestHandler
        // the ServletContextHandler is needed to utilize servlet related classes
        servletContextHandler.setHandler(getHandler());
        servletContextHandler.insertHandler(handler);
        server.setHandler(servletContextHandler);

        server.addConnector(connector);
        server.start();
        client.start();
    }

    @After
    public void tearDown() throws Exception {
        server.stop();
        client.stop();
    }

    protected String uri(String path) {
        return "http://localhost:" + connector.getLocalPort() + path;
    }

    protected abstract Handler getHandler();
}
