package com.yammer.metrics.httpclient;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.core.MetricName;
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

    private final String domain;

    public InstrumentedClientConnManager() {
        this(SchemeRegistryFactory.createDefault());
    }

    public InstrumentedClientConnManager(String domain) {
        this(SchemeRegistryFactory.createDefault(), domain);
    }

    public InstrumentedClientConnManager(SchemeRegistry registry) {
        this(registry, -1, TimeUnit.MILLISECONDS);
    }

    public InstrumentedClientConnManager(SchemeRegistry registry, String domain) {
        this(registry, -1, TimeUnit.MILLISECONDS, domain);
    }

    public InstrumentedClientConnManager(SchemeRegistry registry,
                                         long connTTL,
                                         TimeUnit connTTLTimeUnit) {
        this(Metrics.defaultRegistry(), registry, connTTL, connTTLTimeUnit);
    }

    public InstrumentedClientConnManager(SchemeRegistry registry,
                                         long connTTL,
                                         TimeUnit connTTLTimeUnit,
                                         String domain) {
        this(Metrics.defaultRegistry(), registry, connTTL, connTTLTimeUnit, domain);
    }

    public InstrumentedClientConnManager(MetricsRegistry metricsRegistry,
                                         SchemeRegistry registry,
                                         long connTTL,
                                         TimeUnit connTTLTimeUnit) {
        this(metricsRegistry, registry, connTTL, connTTLTimeUnit, new SystemDefaultDnsResolver(), null);
    }

    public InstrumentedClientConnManager(MetricsRegistry metricsRegistry,
                                         SchemeRegistry registry,
                                         long connTTL,
                                         TimeUnit connTTLTimeUnit,
                                         String domain) {
        this(metricsRegistry, registry, connTTL, connTTLTimeUnit, new SystemDefaultDnsResolver(), domain);
    }

    public InstrumentedClientConnManager(MetricsRegistry metricsRegistry,
                                         SchemeRegistry schemeRegistry,
                                         long connTTL,
                                         TimeUnit connTTLTimeUnit,
                                         DnsResolver dnsResolver,
                                         String domain) {
        super(schemeRegistry, connTTL, connTTLTimeUnit, dnsResolver);
        this.domain = domain;

        metricsRegistry.newGauge(metricName("available"),
                                 new Gauge<Integer>() {
                                     @Override
                                     public Integer getValue() {
                                         // this acquires a lock on the connection pool; remove if contention sucks
                                         return getTotalStats().getAvailable();
                                     }
                                 });
        metricsRegistry.newGauge(metricName("leased"),
                                 new Gauge<Integer>() {
                                     @Override
                                     public Integer getValue() {
                                         // this acquires a lock on the connection pool; remove if contention sucks
                                         return getTotalStats().getLeased();
                                     }
                                 });
        metricsRegistry.newGauge(metricName("max"),
                                 new Gauge<Integer>() {
                                     @Override
                                     public Integer getValue() {
                                         // this acquires a lock on the connection pool; remove if contention sucks
                                         return getTotalStats().getMax();
                                     }
                                 });
        metricsRegistry.newGauge(metricName("pending"),
                                 new Gauge<Integer>() {
                                     @Override
                                     public Integer getValue() {
                                         // this acquires a lock on the connection pool; remove if contention sucks
                                         return getTotalStats().getPending();
                                     }
                                 });
    }

    private MetricName metricName(String namePrefix) {
        String name = namePrefix + "-connections";
        if (domain == null) {
            return new MetricName(ClientConnectionManager.class, name);
        }

        return new MetricName(domain, "ClientConnectionManager", name);
    }
}
