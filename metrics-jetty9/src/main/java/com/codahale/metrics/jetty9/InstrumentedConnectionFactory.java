package com.codahale.metrics.jetty9;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Timer;
import org.eclipse.jetty.io.Connection;
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.util.component.ContainerLifeCycle;

import java.util.List;

public class InstrumentedConnectionFactory extends ContainerLifeCycle implements ConnectionFactory {
    private final ConnectionFactory connectionFactory;
    private final Timer timer;
    private final Counter counter;

    public InstrumentedConnectionFactory(ConnectionFactory connectionFactory, Timer timer) {
        this(connectionFactory, timer, null);
    }

    public InstrumentedConnectionFactory(ConnectionFactory connectionFactory, Timer timer, Counter counter) {
        this.connectionFactory = connectionFactory;
        this.timer = timer;
        this.counter = counter;
        addBean(connectionFactory);
    }

    @Override
    public String getProtocol() {
        return connectionFactory.getProtocol();
    }

    @Override
    public List<String> getProtocols() {
        return connectionFactory.getProtocols();
    }

    @Override
    public Connection newConnection(Connector connector, EndPoint endPoint) {
        final Connection connection = connectionFactory.newConnection(connector, endPoint);
        connection.addListener(new Connection.Listener() {
            private Timer.Context context;

            @Override
            public void onOpened(Connection connection) {
                this.context = timer.time();
                if (counter != null) {
                    counter.inc();
                }
            }

            @Override
            public void onClosed(Connection connection) {
                context.stop();
                if (counter != null) {
                    counter.dec();
                }
            }
        });
        return connection;
    }
}
