package com.codahale.metrics.httpasyncclient;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import static com.codahale.metrics.MetricRegistry.name;
import java.util.concurrent.TimeUnit;
import org.apache.http.config.Registry;
import org.apache.http.conn.DnsResolver;
import org.apache.http.conn.SchemePortResolver;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.nio.conn.ManagedNHttpClientConnection;
import org.apache.http.nio.conn.NHttpClientConnectionManager;
import org.apache.http.nio.conn.NHttpConnectionFactory;
import org.apache.http.nio.conn.SchemeIOSessionStrategy;
import org.apache.http.nio.reactor.ConnectingIOReactor;

public class InstrumentedNClientConnManager extends PoolingNHttpClientConnectionManager {

    public InstrumentedNClientConnManager(final ConnectingIOReactor ioreactor, final NHttpConnectionFactory<ManagedNHttpClientConnection> connFactory, final SchemePortResolver schemePortResolver, final MetricRegistry metricRegistry, final Registry<SchemeIOSessionStrategy> iosessionFactoryRegistry, final long timeToLive, final TimeUnit tunit, final DnsResolver dnsResolver, final String name) {
        super(ioreactor, connFactory, iosessionFactoryRegistry, schemePortResolver, dnsResolver, timeToLive, tunit);
        metricRegistry.register(name(NHttpClientConnectionManager.class, name, "available-connections"),
                (Gauge<Integer>) () -> {
                    // this acquires a lock on the connection pool; remove if contention sucks
                    return getTotalStats().getAvailable();
                });
        metricRegistry.register(name(NHttpClientConnectionManager.class, name, "leased-connections"),
                (Gauge<Integer>) () -> {
                    // this acquires a lock on the connection pool; remove if contention sucks
                    return getTotalStats().getLeased();
                });
        metricRegistry.register(name(NHttpClientConnectionManager.class, name, "max-connections"),
                (Gauge<Integer>) () -> {
                    // this acquires a lock on the connection pool; remove if contention sucks
                    return getTotalStats().getMax();
                });
        metricRegistry.register(name(NHttpClientConnectionManager.class, name, "pending-connections"),
                (Gauge<Integer>) () -> {
                    // this acquires a lock on the connection pool; remove if contention sucks
                    return getTotalStats().getPending();
                });
    }

}
