package com.yammer.metrics.httpclient;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Meter;
import org.apache.http.conn.ClientConnectionOperator;
import org.apache.http.conn.params.ConnPerRoute;
import org.apache.http.impl.conn.tsccm.BasicPoolEntry;
import org.apache.http.impl.conn.tsccm.ConnPoolByRoute;
import org.apache.http.impl.conn.tsccm.RouteSpecificPool;

import java.util.concurrent.TimeUnit;

/**
 * A route-specific connection pool which monitors the rate at which connections are created.
 */
class InstrumentedConnByRoute extends ConnPoolByRoute {
    private static final Meter NEW_CONNECTIONS = Metrics.newMeter(ConnPoolByRoute.class,
                                                                  "new-connections",
                                                                  "connections",
                                                                  TimeUnit.SECONDS);

    InstrumentedConnByRoute(ClientConnectionOperator operator,
                            ConnPerRoute connPerRoute,
                            int maxTotalConnections,
                            long connTTL,
                            TimeUnit connTTLTimeUnit) {
        super(operator, connPerRoute, maxTotalConnections, connTTL, connTTLTimeUnit);
    }

    @Override
    protected BasicPoolEntry createEntry(RouteSpecificPool pool,
                                         ClientConnectionOperator op) {
        NEW_CONNECTIONS.mark();
        return super.createEntry(pool, op);
    }
}
