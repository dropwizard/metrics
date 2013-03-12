package com.yammer.metrics.jetty;

import com.yammer.metrics.*;
import org.eclipse.jetty.io.Connection;
import org.eclipse.jetty.server.nio.SelectChannelConnector;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static com.yammer.metrics.MetricRegistry.name;

public class InstrumentedSelectChannelConnector extends SelectChannelConnector {
    private final Timer duration;
    private final Meter accepts, connects, disconnects;
    private final Counter connections;
    private final Clock clock;

    public InstrumentedSelectChannelConnector(MetricRegistry registry,
                                              int port,
                                              Clock clock) {
        super();
        this.clock = clock;
        setPort(port);

        this.duration = registry.timer(name(SelectChannelConnector.class,
                                            Integer.toString(port),
                                            "connection-duration"));
        this.accepts = registry.meter(name(SelectChannelConnector.class,
                                           Integer.toString(port),
                                           "accepts"));
        this.connects = registry.meter(name(SelectChannelConnector.class,
                                            Integer.toString(port),
                                            "connects"));
        this.disconnects = registry.meter(name(SelectChannelConnector.class,
                                               Integer.toString(port),
                                               "disconnects"));
        this.connections = registry.counter(name(SelectChannelConnector.class,
                                                 Integer.toString(port),
                                                 "active-connections"));
    }

    @Override
    public void accept(int acceptorID) throws IOException {
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
