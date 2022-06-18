package com.codahale.metrics.httpclient5;

import com.codahale.metrics.MetricRegistry;
import org.apache.hc.client5.http.DnsResolver;
import org.apache.hc.client5.http.SchemePortResolver;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManager;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.nio.AsyncClientConnectionManager;
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.apache.hc.core5.http.URIScheme;
import org.apache.hc.core5.http.config.Lookup;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.http.nio.ssl.TlsStrategy;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.pool.PoolConcurrencyPolicy;
import org.apache.hc.core5.pool.PoolReusePolicy;
import org.apache.hc.core5.util.TimeValue;

import static com.codahale.metrics.MetricRegistry.name;
import static java.util.Objects.requireNonNull;

/**
 * A {@link HttpClientConnectionManager} which monitors the number of open connections.
 */
public class InstrumentedAsyncClientConnectionManager extends PoolingAsyncClientConnectionManager {
    private static final String METRICS_PREFIX = AsyncClientConnectionManager.class.getName();

    protected static Registry<TlsStrategy> getDefaultTlsStrategy() {
        return RegistryBuilder.<TlsStrategy>create()
                .register(URIScheme.HTTPS.id, DefaultClientTlsStrategy.getDefault())
                .build();
    }

    private final MetricRegistry metricsRegistry;
    private final String name;

    InstrumentedAsyncClientConnectionManager(final MetricRegistry metricRegistry,
                                             final String name,
                                             final Lookup<TlsStrategy> tlsStrategyLookup,
                                             final PoolConcurrencyPolicy poolConcurrencyPolicy,
                                             final PoolReusePolicy poolReusePolicy,
                                             final TimeValue timeToLive,
                                             final SchemePortResolver schemePortResolver,
                                             final DnsResolver dnsResolver) {

        super(tlsStrategyLookup, poolConcurrencyPolicy, poolReusePolicy, timeToLive, schemePortResolver, dnsResolver);
        this.metricsRegistry = requireNonNull(metricRegistry, "metricRegistry");
        this.name = name;

        // this acquires a lock on the connection pool; remove if contention sucks
        metricRegistry.registerGauge(name(METRICS_PREFIX, name, "available-connections"),
                () -> getTotalStats().getAvailable());
        // this acquires a lock on the connection pool; remove if contention sucks
        metricRegistry.registerGauge(name(METRICS_PREFIX, name, "leased-connections"),
                () -> getTotalStats().getLeased());
        // this acquires a lock on the connection pool; remove if contention sucks
        metricRegistry.registerGauge(name(METRICS_PREFIX, name, "max-connections"),
                () -> getTotalStats().getMax());
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
        private Lookup<TlsStrategy> tlsStrategyLookup = getDefaultTlsStrategy();
        private SchemePortResolver schemePortResolver;
        private DnsResolver dnsResolver;
        private PoolConcurrencyPolicy poolConcurrencyPolicy;
        private PoolReusePolicy poolReusePolicy;
        private TimeValue timeToLive = TimeValue.NEG_ONE_MILLISECOND;

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

        public Builder tlsStrategyLookup(Lookup<TlsStrategy> tlsStrategyLookup) {
            this.tlsStrategyLookup = tlsStrategyLookup;
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

        public InstrumentedAsyncClientConnectionManager build() {
            return new InstrumentedAsyncClientConnectionManager(
                    metricsRegistry,
                    name,
                    tlsStrategyLookup,
                    poolConcurrencyPolicy,
                    poolReusePolicy,
                    timeToLive,
                    schemePortResolver,
                    dnsResolver);
        }
    }

}
