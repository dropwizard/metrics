package com.yammer.metrics.httpclient;

import com.yammer.metrics.Gauge;
import com.yammer.metrics.MetricRegistry;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.DnsResolver;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.impl.conn.SchemeRegistryFactory;
import org.apache.http.impl.conn.SystemDefaultDnsResolver;

import java.util.concurrent.TimeUnit;

import static com.yammer.metrics.MetricRegistry.name;

/**
 * A {@link ClientConnectionManager} which monitors the number of open connections.
 */
public class InstrumentedClientConnManager extends PoolingClientConnectionManager {
    public InstrumentedClientConnManager(MetricRegistry metricRegistry) {
        this(metricRegistry, SchemeRegistryFactory.createDefault());
    }

    public InstrumentedClientConnManager(MetricRegistry metricsRegistry,
                                         SchemeRegistry registry) {
        this(metricsRegistry, registry, -1, TimeUnit.MILLISECONDS);
    }

    public InstrumentedClientConnManager(MetricRegistry metricsRegistry,
                                         SchemeRegistry registry,
                                         long connTTL,
                                         TimeUnit connTTLTimeUnit) {
        this(metricsRegistry, registry, connTTL, connTTLTimeUnit, new SystemDefaultDnsResolver(), null);
    }

    public InstrumentedClientConnManager(MetricRegistry metricsRegistry,
                                         SchemeRegistry schemeRegistry,
                                         long connTTL,
                                         TimeUnit connTTLTimeUnit,
                                         DnsResolver dnsResolver,
                                         String name) {
        super(schemeRegistry, connTTL, connTTLTimeUnit, dnsResolver);
        metricsRegistry.register(name(ClientConnectionManager.class, name, "available-connections"),
                                 new Gauge<Integer>() {
                                     @Override
                                     public Integer getValue() {
                                         // this acquires a lock on the connection pool; remove if contention sucks
                                         return getTotalStats().getAvailable();
                                     }
                                 });
        metricsRegistry.register(name(ClientConnectionManager.class, name, "leased-connections"),
                                 new Gauge<Integer>() {
                                     @Override
                                     public Integer getValue() {
                                         // this acquires a lock on the connection pool; remove if contention sucks
                                         return getTotalStats().getLeased();
                                     }
                                 });
        metricsRegistry.register(name(ClientConnectionManager.class, name, "max-connections"),
                                 new Gauge<Integer>() {
                                     @Override
                                     public Integer getValue() {
                                         // this acquires a lock on the connection pool; remove if contention sucks
                                         return getTotalStats().getMax();
                                     }
                                 });
        metricsRegistry.register(name(ClientConnectionManager.class, name, "pending-connections"),
                                 new Gauge<Integer>() {
                                     @Override
                                     public Integer getValue() {
                                         // this acquires a lock on the connection pool; remove if contention sucks
                                         return getTotalStats().getPending();
                                     }
                                 });
    }
}
