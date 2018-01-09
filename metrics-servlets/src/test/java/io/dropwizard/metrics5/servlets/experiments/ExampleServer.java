package io.dropwizard.metrics5.servlets.experiments;

import io.dropwizard.metrics5.Counter;
import io.dropwizard.metrics5.Gauge;
import io.dropwizard.metrics5.MetricRegistry;
import io.dropwizard.metrics5.health.HealthCheckRegistry;
import io.dropwizard.metrics5.jetty9.InstrumentedConnectionFactory;
import io.dropwizard.metrics5.jetty9.InstrumentedHandler;
import io.dropwizard.metrics5.jetty9.InstrumentedQueuedThreadPool;
import io.dropwizard.metrics5.servlets.AdminServlet;
import io.dropwizard.metrics5.servlets.HealthCheckServlet;
import io.dropwizard.metrics5.servlets.MetricsServlet;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.ThreadPool;

import static io.dropwizard.metrics5.MetricRegistry.name;

public class ExampleServer {
    private static final MetricRegistry REGISTRY = new MetricRegistry();
    private static final Counter COUNTER_1 = REGISTRY.counter(name(ExampleServer.class, "wah", "doody"));
    private static final Counter COUNTER_2 = REGISTRY.counter(name(ExampleServer.class, "woo"));

    static {
        REGISTRY.register(name(ExampleServer.class, "boo"), (Gauge<Integer>) () -> {
            throw new RuntimeException("asplode!");
        });
    }

    public static void main(String[] args) throws Exception {
        COUNTER_1.inc();
        COUNTER_2.inc();

        final ThreadPool threadPool = new InstrumentedQueuedThreadPool(REGISTRY);
        final Server server = new Server(threadPool);

        final Connector connector = new ServerConnector(server, new InstrumentedConnectionFactory(
                new HttpConnectionFactory(), REGISTRY.timer("http.connection")));
        server.addConnector(connector);

        final ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/initial");
        context.setAttribute(MetricsServlet.METRICS_REGISTRY, REGISTRY);
        context.setAttribute(HealthCheckServlet.HEALTH_CHECK_REGISTRY, new HealthCheckRegistry());

        final ServletHolder holder = new ServletHolder(new AdminServlet());
        context.addServlet(holder, "/dingo/*");

        final InstrumentedHandler handler = new InstrumentedHandler(REGISTRY);
        handler.setHandler(context);
        server.setHandler(handler);

        server.start();
        server.join();
    }
}
