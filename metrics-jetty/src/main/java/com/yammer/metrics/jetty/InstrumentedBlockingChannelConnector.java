package com.yammer.metrics.jetty;

import com.yammer.metrics.*;
import org.eclipse.jetty.io.Connection;
import org.eclipse.jetty.server.nio.BlockingChannelConnector;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static com.yammer.metrics.MetricRegistry.name;

public class InstrumentedBlockingChannelConnector extends BlockingChannelConnector {
    private final Timer duration;
    private final Meter accepts, connects, disconnects;
    private final Counter connections;
    private final Clock clock;

    public InstrumentedBlockingChannelConnector(MetricRegistry registry,
                                                int port,
                                                Clock clock) {
        super();
        this.clock = clock;
        setPort(port);
        this.duration = registry.timer(name(BlockingChannelConnector.class,
                                            Integer.toString(port),
                                            "connection-duration"));
        this.accepts = registry.meter(name(BlockingChannelConnector.class,
                                           Integer.toString(port),
                                           "accepts"));
        this.connects = registry.meter(name(BlockingChannelConnector.class,
                                            Integer.toString(port),
                                            "connects"));
        this.disconnects = registry.meter(name(BlockingChannelConnector.class,
                                               Integer.toString(port),
                                               "disconnects"));
        this.connections = registry.counter(name(BlockingChannelConnector.class,
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
