package com.yammer.metrics.httpclient;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.core.MetricsRegistry;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.DnsResolver;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.impl.conn.SchemeRegistryFactory;
import org.apache.http.impl.conn.SystemDefaultDnsResolver;

import java.util.concurrent.TimeUnit;

/**
 * A {@link ClientConnectionManager} which monitors the number of open connections.
 */
public class InstrumentedClientConnManager extends PoolingClientConnectionManager {
    public InstrumentedClientConnManager() {
        this(SchemeRegistryFactory.createDefault());
    }

    public InstrumentedClientConnManager(SchemeRegistry registry) {
        this(registry, -1, TimeUnit.MILLISECONDS);
    }

    public InstrumentedClientConnManager(SchemeRegistry registry,
                                         long connTTL,
                                         TimeUnit connTTLTimeUnit) {
        this(Metrics.defaultRegistry(), registry, connTTL, connTTLTimeUnit);
    }

    public InstrumentedClientConnManager(MetricsRegistry metricsRegistry,
                                         SchemeRegistry registry,
                                         long connTTL,
                                         TimeUnit connTTLTimeUnit) {
        this(metricsRegistry, registry, connTTL, connTTLTimeUnit, new SystemDefaultDnsResolver());
    }

    public InstrumentedClientConnManager(MetricsRegistry metricsRegistry,
                                         SchemeRegistry schemeRegistry,
                                         long connTTL,
                                         TimeUnit connTTLTimeUnit,
                                         DnsResolver dnsResolver) {
        super(schemeRegistry, connTTL, connTTLTimeUnit, dnsResolver);
        metricsRegistry.newGauge(ClientConnectionManager.class,
                                 "available-connections",
                                 new Gauge<Integer>() {
                                     @Override
                                     public Integer value() {
                                         // this acquires a lock on the connection pool; remove if contention sucks
                                         return getTotalStats().getAvailable();
                                     }
                                 });
        metricsRegistry.newGauge(ClientConnectionManager.class,
                                 "leased-connections",
                                 new Gauge<Integer>() {
                                     @Override
                                     public Integer value() {
                                         // this acquires a lock on the connection pool; remove if contention sucks
                                         return getTotalStats().getLeased();
                                     }
                                 });
        metricsRegistry.newGauge(ClientConnectionManager.class,
                                 "max-connections",
                                 new Gauge<Integer>() {
                                     @Override
                                     public Integer value() {
                                         // this acquires a lock on the connection pool; remove if contention sucks
                                         return getTotalStats().getMax();
                                     }
                                 });
        metricsRegistry.newGauge(ClientConnectionManager.class,
                                 "pending-connections",
                                 new Gauge<Integer>() {
                                     @Override
                                     public Integer value() {
                                         // this acquires a lock on the connection pool; remove if contention sucks
                                         return getTotalStats().getPending();
                                     }
                                 });
    }
}
