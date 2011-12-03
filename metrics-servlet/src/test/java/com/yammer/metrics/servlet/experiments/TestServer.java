package com.yammer.metrics.servlet.experiments;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.ThreadPool;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.CounterMetric;
import com.yammer.metrics.core.GaugeMetric;
import com.yammer.metrics.jetty.InstrumentedHandler;
import com.yammer.metrics.jetty.InstrumentedQueuedThreadPool;
import com.yammer.metrics.jetty.InstrumentedSelectChannelConnector;
import com.yammer.metrics.reporting.MetricsServlet;

public class TestServer {
    private static final CounterMetric COUNTER1 = Metrics.newCounter(TestServer.class, "wah", "doody");
    private static final CounterMetric COUNTER2 = Metrics.newCounter(TestServer.class, "woo");
    static {
        Metrics.newGauge(TestServer.class, "boo", new GaugeMetric<Integer>() {
            @Override
            public Integer value() {
                throw new RuntimeException("asplode!");
            }
        });
    }

    public static void main(String[] args) throws Exception {
        COUNTER1.inc();
        COUNTER2.inc();

        final Server server = new Server();

        final Connector connector = new InstrumentedSelectChannelConnector(8080);
        server.addConnector(connector);

        final ThreadPool threadPool = new InstrumentedQueuedThreadPool();
        server.setThreadPool(threadPool);

        final ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/initial");

        final ServletHolder holder = new ServletHolder(MetricsServlet.class);
        context.addServlet(holder, "/dingo/*");
        
        server.setHandler(new InstrumentedHandler(context));
        
        server.start();
        server.join();
    }
}
