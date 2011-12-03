package com.yammer.metrics.httpclient;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.GaugeMetric;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.conn.SchemeRegistryFactory;
import org.apache.http.impl.conn.tsccm.ConnPoolByRoute;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;

import java.util.concurrent.TimeUnit;

/**
 * A {@link org.apache.http.conn.ClientConnectionManager} which monitors the number of open
 * connections.
 */
public class InstrumentedClientConnManager extends ThreadSafeClientConnManager {
    public InstrumentedClientConnManager(SchemeRegistry registry) {
        this(registry, -1, TimeUnit.MILLISECONDS);
    }

    public InstrumentedClientConnManager() {
        this(SchemeRegistryFactory.createDefault());
    }

    public InstrumentedClientConnManager(SchemeRegistry registry,
                                         long connTTL,
                                         TimeUnit connTTLTimeUnit) {
        super(registry, connTTL, connTTLTimeUnit);
        Metrics.newGauge(InstrumentedClientConnManager.class,
                         "connections",
                         new GaugeMetric<Integer>() {
                             @Override
                             public Integer value() {
                                 // this acquires a lock on the connection pool; remove if contention sucks
                                 return getConnectionsInPool();
                             }
                         });
    }

    @Override
    protected ConnPoolByRoute createConnectionPool(long connTTL,
                                                   TimeUnit connTTLTimeUnit) {
        return new InstrumentedConnByRoute(connOperator,
                                           connPerRoute,
                                           20,
                                           connTTL,
                                           connTTLTimeUnit);
    }
}
