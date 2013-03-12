package com.yammer.metrics.jetty8;

import com.yammer.metrics.*;
import org.eclipse.jetty.io.Connection;
import org.eclipse.jetty.server.bio.SocketConnector;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static com.yammer.metrics.MetricRegistry.name;

public class InstrumentedSocketConnector extends SocketConnector {
    private final Timer duration;
    private final Meter accepts, connects, disconnects;
    private final Counter connections;
    private final Clock clock;

    public InstrumentedSocketConnector(MetricRegistry registry,
                                       int port,
                                       Clock clock) {
        super();
        this.clock = clock;
        setPort(port);
        this.duration = registry.timer(name(SocketConnector.class,
                                            Integer.toString(port),
                                            "connection-duration"));
        this.accepts = registry.meter(name(SocketConnector.class,
                                           Integer.toString(port),
                                           "accepts"));
        this.connects = registry.meter(name(SocketConnector.class,
                                            Integer.toString(port),
                                            "connects"));
        this.disconnects = registry.meter(name(SocketConnector.class,
                                               Integer.toString(port),
                                               "disconnects"));
        this.connections = registry.counter(name(SocketConnector.class,
                                                 Integer.toString(port),
                                                 "active-connections"));
    }

    @Override
    public void accept(int acceptorID) throws IOException, InterruptedException {
        super.accept(acceptorID);
        accepts.mark();
    }

    @Override
    protected void connectionOpened(Connection connection) {
        connections.inc();
        super.connectionOpened(connection);
        connects.mark();
    }

    @Override
    protected void connectionClosed(Connection connection) {
        super.connectionClosed(connection);
        disconnects.mark();
        final long duration = clock.getTime() - connection.getTimeStamp();
        this.duration.update(duration, TimeUnit.MILLISECONDS);
        connections.dec();
    }
}
