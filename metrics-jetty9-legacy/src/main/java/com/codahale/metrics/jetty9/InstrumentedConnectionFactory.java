package com.codahale.metrics.jetty9;

import com.codahale.metrics.Timer;
import org.eclipse.jetty.io.Connection;
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.util.component.ContainerLifeCycle;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

public class InstrumentedConnectionFactory extends ContainerLifeCycle implements ConnectionFactory {
    private final ConnectionFactory connectionFactory;
    private final Timer timer;
    private Method getProtocols;

    public InstrumentedConnectionFactory(ConnectionFactory connectionFactory, Timer timer) {
        this.connectionFactory = connectionFactory;
        this.timer = timer;
        addBean(connectionFactory);
        try {
            getProtocols = connectionFactory.getClass().getMethod("getProtocols");
        } catch (NoSuchMethodException ignore) {
            getProtocols = null;
        }
    }

    @Override
    public String getProtocol() {
        return connectionFactory.getProtocol();
    }

    @SuppressWarnings("unchecked")
    public List<String> getProtocols() {
        try {
            return getProtocols != null ?
                    (List<String>) getProtocols.invoke(connectionFactory) :
                    Collections.<String>emptyList();
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Unable to invoke `connectionFactory#getProtocols`", e);
        }
    }

    @Override
    public Connection newConnection(Connector connector, EndPoint endPoint) {
        final Connection connection = connectionFactory.newConnection(connector, endPoint);
        connection.addListener(new Connection.Listener() {
            private Timer.Context context;

            @Override
            public void onOpened(Connection connection) {
                this.context = timer.time();
            }

            @Override
            public void onClosed(Connection connection) {
                context.stop();
            }
        });
        return connection;
    }
}
