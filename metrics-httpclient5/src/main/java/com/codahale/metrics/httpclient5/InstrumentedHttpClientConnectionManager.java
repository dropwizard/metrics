package com.codahale.metrics.httpclient5;

import com.codahale.metrics.MetricRegistry;
import org.apache.hc.client5.http.DnsResolver;
import org.apache.hc.client5.http.SchemePortResolver;
import org.apache.hc.client5.http.impl.io.DefaultHttpClientConnectionOperator;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.io.HttpClientConnectionOperator;
import org.apache.hc.client5.http.io.ManagedHttpClientConnection;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.URIScheme;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.http.io.HttpConnectionFactory;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.pool.PoolConcurrencyPolicy;
import org.apache.hc.core5.pool.PoolReusePolicy;
import org.apache.hc.core5.util.TimeValue;

import static com.codahale.metrics.MetricRegistry.name;
import static java.util.Objects.requireNonNull;

/**
 * A {@link HttpClientConnectionManager} which monitors the number of open connections.
 */
public class InstrumentedHttpClientConnectionManager extends PoolingHttpClientConnectionManager {
    private static final String METRICS_PREFIX = HttpClientConnectionManager.class.getName();

    protected static Registry<ConnectionSocketFactory> getDefaultRegistry() {
        return RegistryBuilder.<ConnectionSocketFactory>create()
                .register(URIScheme.HTTP.id, PlainConnectionSocketFactory.getSocketFactory())
                .register(URIScheme.HTTPS.id, SSLConnectionSocketFactory.getSocketFactory())
                .build();
    }

    private final MetricRegistry metricsRegistry;
    private final String name;

    InstrumentedHttpClientConnectionManager(final MetricRegistry metricRegistry,
                                            final String name,
                                            final HttpClientConnectionOperator httpClientConnectionOperator,
                                            final PoolConcurrencyPolicy poolConcurrencyPolicy,
                                            final PoolReusePolicy poolReusePolicy,
                                            final TimeValue timeToLive,
                                            final HttpConnectionFactory<ManagedHttpClientConnection> connFactory) {

        super(httpClientConnectionOperator, poolConcurrencyPolicy, poolReusePolicy, timeToLive, connFactory);
        this.metricsRegistry = requireNonNull(metricRegistry, "metricRegistry");
        this.name = name;

        // this acquires a lock on the connection pool; remove if contention sucks
        metricRegistry.registerGauge(name(METRICS_PREFIX, name, "available-connections"),
                () -> {
                    return getTotalStats().getAvailable();
                });
        // this acquires a lock on the connection pool; remove if contention sucks
        metricRegistry.registerGauge(name(METRICS_PREFIX, name, "leased-connections"),
                () -> getTotalStats().getLeased());
        // this acquires a lock on the connection pool; remove if contention sucks
        metricRegistry.registerGauge(name(METRICS_PREFIX, name, "max-connections"),
                () -> getTotalStats().getMax()
        );
        // this acquires a lock on the connection pool; remove if contention sucks
        metricRegistry.registerGauge(name(METRICS_PREFIX, name, "pending-connections"),
                () -> getTotalStats().getPending());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        close(CloseMode.GRACEFUL);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close(CloseMode closeMode) {
        super.close(closeMode);
        metricsRegistry.remove(name(METRICS_PREFIX, name, "available-connections"));
        metricsRegistry.remove(name(METRICS_PREFIX, name, "leased-connections"));
        metricsRegistry.remove(name(METRICS_PREFIX, name, "max-connections"));
        metricsRegistry.remove(name(METRICS_PREFIX, name, "pending-connections"));
    }

    public static Builder builder(MetricRegistry metricsRegistry) {
        return new Builder().metricsRegistry(metricsRegistry);
    }

    public static class Builder {
        private MetricRegistry metricsRegistry;
        private String name;
        private HttpClientConnectionOperator httpClientConnectionOperator;
        private Registry<ConnectionSocketFactory> socketFactoryRegistry = getDefaultRegistry();
        private SchemePortResolver schemePortResolver;
        private DnsResolver dnsResolver;
        private PoolConcurrencyPolicy poolConcurrencyPolicy;
        private PoolReusePolicy poolReusePolicy;
        private TimeValue timeToLive = TimeValue.NEG_ONE_MILLISECOND;
        private HttpConnectionFactory<ManagedHttpClientConnection> connFactory;

        Builder() {
        }

        public Builder metricsRegistry(MetricRegistry metricRegistry) {
            this.metricsRegistry = requireNonNull(metricRegistry, "metricRegistry");
            return this;
        }

        public Builder name(final String name) {
            this.name = name;
            return this;
        }

        public Builder socketFactoryRegistry(Registry<ConnectionSocketFactory> socketFactoryRegistry) {
            this.socketFactoryRegistry = requireNonNull(socketFactoryRegistry, "socketFactoryRegistry");
            return this;
        }

        public Builder connFactory(HttpConnectionFactory<ManagedHttpClientConnection> connFactory) {
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

        public Builder timeToLive(TimeValue timeToLive) {
            this.timeToLive = timeToLive;
            return this;
        }

        public Builder httpClientConnectionOperator(HttpClientConnectionOperator httpClientConnectionOperator) {
            this.httpClientConnectionOperator = httpClientConnectionOperator;
            return this;
        }

        public Builder poolConcurrencyPolicy(PoolConcurrencyPolicy poolConcurrencyPolicy) {
            this.poolConcurrencyPolicy = poolConcurrencyPolicy;
            return this;
        }

        public Builder poolReusePolicy(PoolReusePolicy poolReusePolicy) {
            this.poolReusePolicy = poolReusePolicy;
            return this;
        }

        public InstrumentedHttpClientConnectionManager build() {
            if (httpClientConnectionOperator == null) {
                httpClientConnectionOperator = new DefaultHttpClientConnectionOperator(
                        socketFactoryRegistry,
                        schemePortResolver,
                        dnsResolver);
            }

            return new InstrumentedHttpClientConnectionManager(
                    metricsRegistry,
                    name,
                    httpClientConnectionOperator,
                    poolConcurrencyPolicy,
                    poolReusePolicy,
                    timeToLive,
                    connFactory);
        }
    }
}
