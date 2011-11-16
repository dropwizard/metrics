package com.yammer.metrics.jetty;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.CounterMetric;
import com.yammer.metrics.core.MeterMetric;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.core.TimerMetric;
import org.eclipse.jetty.io.Connection;
import org.eclipse.jetty.server.nio.SelectChannelConnector;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class InstrumentedSelectChannelConnector extends SelectChannelConnector {
    private final TimerMetric duration;
    private final MeterMetric accepts, connects, disconnects;
    private final CounterMetric connections;

    public InstrumentedSelectChannelConnector(int port) {
        this(Metrics.defaultRegistry(), port);
    }

    public InstrumentedSelectChannelConnector(MetricsRegistry registry,
                                              int port) {
        super();
        setPort(port);
        this.duration = registry.newTimer(SelectChannelConnector.class,
                                          "connection-duration",
                                          Integer.toString(port),
                                          TimeUnit.MILLISECONDS,
                                          TimeUnit.SECONDS);
        this.accepts = registry.newMeter(SelectChannelConnector.class,
                                         "accepts",
                                         Integer.toString(port),
                                         "connections",
                                         TimeUnit.SECONDS);
        this.connects = registry.newMeter(SelectChannelConnector.class,
                                          "connects",
                                          Integer.toString(port),
                                          "connections",
                                          TimeUnit.SECONDS);
        this.disconnects = registry.newMeter(SelectChannelConnector.class,
                                             "disconnects",
                                             Integer.toString(port),
                                             "connections",
                                             TimeUnit.SECONDS);
        this.connections = registry.newCounter(SelectChannelConnector.class,
                                               "active-connections",
                                               Integer.toString(port));
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
        long duration = System.currentTimeMillis() - connection.getTimeStamp();
        this.duration.update(duration, TimeUnit.MILLISECONDS);
        connections.dec();
    }
}
