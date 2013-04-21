package com.codahale.metrics.jetty9;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import org.eclipse.jetty.io.ByteBufferPool;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.util.annotation.Name;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.Scheduler;

import java.util.concurrent.Executor;

import static com.codahale.metrics.MetricRegistry.name;
import static org.eclipse.jetty.server.AbstractConnectionFactory.getFactories;

public class InstrumentedConnector extends ServerConnector {
    public InstrumentedConnector(@Name("registry") MetricRegistry registry,
                                 @Name("name") String name,
                                 @Name("server") Server server) {
        this(registry, name, server, null, null, null, 0, 0, new HttpConnectionFactory());
    }

    public InstrumentedConnector(@Name("registry") MetricRegistry registry,
                                 @Name("name") String name,
                                 @Name("server") Server server,
                                 @Name("factories") ConnectionFactory... factories) {
        this(registry, name, server, null, null, null, 0, 0, factories);
    }

    public InstrumentedConnector(@Name("registry") MetricRegistry registry,
                                 @Name("name") String name,
                                 @Name("server") Server server,
                                 @Name("sslContextFactory") SslContextFactory sslContextFactory) {
        this(registry, name, server, null, null, null, 0, 0,
             getFactories(sslContextFactory, new HttpConnectionFactory()));
    }

    public InstrumentedConnector(@Name("registry") MetricRegistry registry,
                                 @Name("name") String name,
                                 @Name("server") Server server,
                                 @Name("sslContextFactory") SslContextFactory sslContextFactory,
                                 @Name("factories") ConnectionFactory... factories) {
        this(registry, name, server, null, null, null, 0, 0, getFactories(sslContextFactory, factories));

    }

    public InstrumentedConnector(@Name("registry") MetricRegistry registry,
                                 @Name("name") String name,
                                 @Name("server") Server server,
                                 @Name("executor") Executor executor,
                                 @Name("scheduler") Scheduler scheduler,
                                 @Name("bufferPool") ByteBufferPool bufferPool,
                                 @Name("acceptors") int acceptors,
                                 @Name("selectors") int selectors,
                                 @Name("factories") ConnectionFactory... factories) {
        super(server, executor, scheduler, bufferPool, acceptors, selectors,
              instrument(factories, registry, name));
        setName(name);
    }

    private static ConnectionFactory[] instrument(ConnectionFactory[] factories,
                                                  MetricRegistry registry,
                                                  String name) {
        final ConnectionFactory[] instrumented = new ConnectionFactory[factories.length];
        for (int i = 0; i < factories.length; i++) {
            final Timer timer = registry.timer(name(ServerConnector.class, name, "connections"));
            instrumented[i] = new InstrumentedConnectionFactory(factories[i], timer);
        }
        return instrumented;
    }
}
