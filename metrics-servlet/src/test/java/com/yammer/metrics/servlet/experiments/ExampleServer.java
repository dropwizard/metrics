package com.yammer.metrics.servlet.experiments;

import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.core.MetricsRegistry;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.ThreadPool;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Counter;
import com.yammer.metrics.jetty.InstrumentedHandler;
import com.yammer.metrics.jetty.InstrumentedQueuedThreadPool;
import com.yammer.metrics.jetty.InstrumentedSelectChannelConnector;
import com.yammer.metrics.servlet.AdminServlet;

public class ExampleServer {
    private static final MetricsRegistry REGISTRY = Metrics.defaultRegistry();
    private static final Counter COUNTER_1 = REGISTRY.newCounter(ExampleServer.class, "wah", "doody");
    private static final Counter COUNTER_2 = REGISTRY.newCounter(ExampleServer.class, "woo");
    static {
        Metrics.defaultRegistry().newGauge(ExampleServer.class, "boo", new Gauge<Integer>() {
            @Override
            public Integer getValue() {
                throw new RuntimeException("asplode!");
            }
        });
    }

    public static void main(String[] args) throws Exception {
        COUNTER_1.inc();
        COUNTER_2.inc();

        final Server server = new Server();

        final Connector connector = new InstrumentedSelectChannelConnector(8080);
        server.addConnector(connector);

        final ThreadPool threadPool = new InstrumentedQueuedThreadPool();
        server.setThreadPool(threadPool);

        final ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/initial");

        final ServletHolder holder = new ServletHolder(new AdminServlet());
        context.addServlet(holder, "/dingo/*");
        
        server.setHandler(new InstrumentedHandler(context));
        
        server.start();
        server.join();
    }
}
