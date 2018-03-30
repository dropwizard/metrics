package io.dropwizard.metrics5.jetty9;

import java.util.List;
import java.util.Optional;

import org.eclipse.jetty.io.Connection;
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.util.component.ContainerLifeCycle;

import io.dropwizard.metrics5.Counter;
import io.dropwizard.metrics5.Timer;

public class InstrumentedConnectionFactory extends ContainerLifeCycle implements ConnectionFactory {
    private final ConnectionFactory connectionFactory;
    private final Timer timer;
    private final Optional<Counter> counterMaybe;

    public InstrumentedConnectionFactory(ConnectionFactory connectionFactory, Timer timer) {
        this(connectionFactory, timer, Optional.empty());
    }

    public InstrumentedConnectionFactory(ConnectionFactory connectionFactory, Timer timer, Counter counter) {
        this(connectionFactory, timer, Optional.of(counter));
    }

    private InstrumentedConnectionFactory(ConnectionFactory connectionFactory,
                                          Timer timer,
                                          Optional<Counter> counterMaybe) {
        this.connectionFactory = connectionFactory;
        this.timer = timer;
        this.counterMaybe = counterMaybe;
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
                counterMaybe.ifPresent(Counter::inc);
            }

            @Override
            public void onClosed(Connection connection) {
                context.stop();
                counterMaybe.ifPresent(Counter::dec);
            }
        });
        return connection;
    }
}
