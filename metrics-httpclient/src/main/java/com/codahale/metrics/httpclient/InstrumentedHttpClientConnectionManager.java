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

    /**
     * @deprecated Use {@link #builder(MetricRegistry)} instead.
     */
    @Deprecated
    public InstrumentedHttpClientConnectionManager(MetricRegistry metricRegistry) {
        this(metricRegistry, getDefaultRegistry());
    }

    /**
     * @deprecated Use {@link #builder(MetricRegistry)} instead.
     */
    @Deprecated
    public InstrumentedHttpClientConnectionManager(MetricRegistry metricsRegistry,
                                                   Registry<ConnectionSocketFactory> socketFactoryRegistry) {
        this(metricsRegistry, socketFactoryRegistry, -1, TimeUnit.MILLISECONDS);
    }


    /**
     * @deprecated Use {@link #builder(MetricRegistry)} instead.
     */
    @Deprecated
    public InstrumentedHttpClientConnectionManager(MetricRegistry metricsRegistry,
                                                   Registry<ConnectionSocketFactory> socketFactoryRegistry,
                                                   long connTTL,
                                                   TimeUnit connTTLTimeUnit) {
        this(metricsRegistry, socketFactoryRegistry, null, null, SystemDefaultDnsResolver.INSTANCE, connTTL, connTTLTimeUnit, null);
    }


    /**
     * @deprecated Use {@link #builder(MetricRegistry)} instead.
     */
    @Deprecated
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

    /**
     * @deprecated Use {@link #builder(MetricRegistry)} instead.
     */
    @Deprecated
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

    public static Builder builder(MetricRegistry metricsRegistry) {
        return new Builder().metricsRegistry(metricsRegistry);
    }

    public static class Builder {
        private MetricRegistry metricsRegistry;
        private HttpClientConnectionOperator httpClientConnectionOperator;
        private Registry<ConnectionSocketFactory> socketFactoryRegistry = getDefaultRegistry();
        private HttpConnectionFactory<HttpRoute, ManagedHttpClientConnection> connFactory;
        private SchemePortResolver schemePortResolver;
        private DnsResolver dnsResolver = SystemDefaultDnsResolver.INSTANCE;
        private long connTTL = -1;
        private TimeUnit connTTLTimeUnit = TimeUnit.MILLISECONDS;
        private String name;

        Builder() {
        }

        public Builder metricsRegistry(MetricRegistry metricsRegistry) {
            this.metricsRegistry = metricsRegistry;
            return this;
        }

        public Builder socketFactoryRegistry(Registry<ConnectionSocketFactory> socketFactoryRegistry) {
            this.socketFactoryRegistry = socketFactoryRegistry;
            return this;
        }

        public Builder connFactory(HttpConnectionFactory<HttpRoute, ManagedHttpClientConnection> connFactory) {
            this.connFactory = connFactory;
            return this;
        }

        public Builder schemePortResolver(SchemePortResolver schemePortResolver) {
            this.schemePortResolver = schemePortResolver;
            return this;
        }

        public Builder dnsResolver(DnsResolver dnsResolver) {
            this.dnsResolver = dnsResolver;
            return this;
        }

        public Builder connTTL(long connTTL) {
            this.connTTL = connTTL;
            return this;
        }

        public Builder connTTLTimeUnit(TimeUnit connTTLTimeUnit) {
            this.connTTLTimeUnit = connTTLTimeUnit;
            return this;
        }

        public Builder httpClientConnectionOperator(HttpClientConnectionOperator httpClientConnectionOperator) {
            this.httpClientConnectionOperator = httpClientConnectionOperator;
            return this;
        }

        public Builder name(final String name) {
            this.name = name;
            return this;
        }

        public InstrumentedHttpClientConnectionManager build() {
            if (httpClientConnectionOperator == null) {
                httpClientConnectionOperator = new DefaultHttpClientConnectionOperator(socketFactoryRegistry, schemePortResolver, dnsResolver);
            }
            return new InstrumentedHttpClientConnectionManager(metricsRegistry, httpClientConnectionOperator, connFactory, connTTL, connTTLTimeUnit, name);
        }
    }

}
