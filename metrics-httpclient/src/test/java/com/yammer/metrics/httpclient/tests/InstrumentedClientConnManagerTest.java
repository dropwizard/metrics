package com.yammer.metrics.httpclient.tests;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.httpclient.InstrumentedClientConnManager;
import org.apache.http.HttpHost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ClientConnectionRequest;
import org.apache.http.conn.ManagedClientConnection;
import org.apache.http.conn.routing.HttpRoute;
import org.junit.After;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class InstrumentedClientConnManagerTest {
    private final ClientConnectionManager manager = new InstrumentedClientConnManager();

    @After
    public void tearDown() throws Exception {
        manager.shutdown();
    }

    @Test
    public void what() throws Exception {
        final Gauge<Integer> gauge = Metrics.newGauge(ClientConnectionManager.class,
                                                      "connections",
                                                      null);

        final HttpRoute route = new HttpRoute(new HttpHost("example.com"));
        final Object state = new Object();
        final ClientConnectionRequest request = manager.requestConnection(route, state);
        final ManagedClientConnection connection = request.getConnection(100, TimeUnit.SECONDS);

        try {
            assertThat(gauge.value(),
                       is(1));
        } finally {
            connection.close();
            manager.releaseConnection(connection, 0, TimeUnit.SECONDS);
        }
    }
}
