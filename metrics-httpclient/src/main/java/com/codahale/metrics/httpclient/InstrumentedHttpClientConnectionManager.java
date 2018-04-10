package com.codahale.metrics.httpclient;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.DnsResolver;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.HttpClientConnectionOperator;
import org.apache.http.conn.HttpConnectionFactory;
import org.apache.http.conn.ManagedHttpClientConnection;
import org.apache.http.conn.SchemePortResolver;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.conn.DefaultHttpClientConnectionOperator;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.conn.SystemDefaultDnsResolver;

import java.util.concurrent.TimeUnit;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * A {@link HttpClientConnectionManager} which monitors the number of open connections.
 */
public class InstrumentedHttpClientConnectionManager extends PoolingHttpClientConnectionManager {


    protected static Registry<ConnectionSocketFactory> getDefaultRegistry() {
        return RegistryBuilder.<ConnectionSocketFactory>create()
            .register("http", PlainConnectionSocketFactory.getSocketFactory())
            .register("https", SSLConnectionSocketFactory.getSocketFactory())
            .build();
    }

    private final MetricRegistry metricsRegistry;
    private final String name;

    public InstrumentedHttpClientConnectionManager(MetricRegistry metricRegistry) {
        this(metricRegistry, getDefaultRegistry());
    }

    public InstrumentedHttpClientConnectionManager(MetricRegistry metricsRegistry,
                                                   Registry<ConnectionSocketFactory> socketFactoryRegistry) {
        this(metricsRegistry, socketFactoryRegistry, -1, TimeUnit.MILLISECONDS);
    }


    public InstrumentedHttpClientConnectionManager(MetricRegistry metricsRegistry,
                                                   Registry<ConnectionSocketFactory> socketFactoryRegistry,
                                                   long connTTL,
                                                   TimeUnit connTTLTimeUnit) {
        this(metricsRegistry, socketFactoryRegistry, null, null, SystemDefaultDnsResolver.INSTANCE, connTTL, connTTLTimeUnit, null);
    }


    public InstrumentedHttpClientConnectionManager(MetricRegistry metricsRegistry,
                                                   Registry<ConnectionSocketFactory> socketFactoryRegistry,
                                                   HttpConnectionFactory<HttpRoute, ManagedHttpClientConnection>
                                                           connFactory,
                                                   SchemePortResolver schemePortResolver,
                                                   DnsResolver dnsResolver,
                                                   long connTTL,
                                                   TimeUnit connTTLTimeUnit,
                                                   String name) {
        this(metricsRegistry,
             new DefaultHttpClientConnectionOperator(socketFactoryRegistry, schemePortResolver, dnsResolver),
             connFactory,
             connTTL,
             connTTLTimeUnit,
             name);
    }

    public InstrumentedHttpClientConnectionManager(MetricRegistry metricsRegistry,
                                                   HttpClientConnectionOperator httpClientConnectionOperator,
                                                   HttpConnectionFactory<HttpRoute, ManagedHttpClientConnection>
                                                           connFactory,
                                                   long connTTL,
                                                   TimeUnit connTTLTimeUnit,
                                                   String name) {
        super(httpClientConnectionOperator, connFactory, connTTL, connTTLTimeUnit);
        this.metricsRegistry = metricsRegistry;
        this.name = name;

        metricsRegistry.register(name(HttpClientConnectionManager.class, name, "available-connections"),
            (Gauge<Integer>) () -> {
                // this acquires a lock on the connection pool; remove if contention sucks
                return getTotalStats().getAvailable();
            });
        metricsRegistry.register(name(HttpClientConnectionManager.class, name, "leased-connections"),
            (Gauge<Integer>) () -> {
                // this acquires a lock on the connection pool; remove if contention sucks
                return getTotalStats().getLeased();
            });
        metricsRegistry.register(name(HttpClientConnectionManager.class, name, "max-connections"),
            (Gauge<Integer>) () -> {
                // this acquires a lock on the connection pool; remove if contention sucks
                return getTotalStats().getMax();
            });
        metricsRegistry.register(name(HttpClientConnectionManager.class, name, "pending-connections"),
            (Gauge<Integer>) () -> {
                // this acquires a lock on the connection pool; remove if contention sucks
                return getTotalStats().getPending();
            });
    }

    @Override
    public void shutdown() {
        super.shutdown();
        metricsRegistry.remove(name(HttpClientConnectionManager.class, name, "available-connections"));
        metricsRegistry.remove(name(HttpClientConnectionManager.class, name, "leased-connections"));
        metricsRegistry.remove(name(HttpClientConnectionManager.class, name, "max-connections"));
        metricsRegistry.remove(name(HttpClientConnectionManager.class, name, "pending-connections"));
    }
}
