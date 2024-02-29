package com.codahale.metrics.httpasyncclient;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import org.apache.http.config.Registry;
import org.apache.http.conn.DnsResolver;
import org.apache.http.conn.SchemePortResolver;
import org.apache.http.nio.conn.ManagedNHttpClientConnection;
import org.apache.http.nio.conn.NHttpConnectionFactory;
import org.apache.http.nio.conn.SchemeIOSessionStrategy;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;

public class InstrumentedNClientConnManagerTest {

    @Mock
    private MetricRegistry metricRegistry;

    @Mock
    private ConnectingIOReactor ioreactor;

    @Mock
    private NHttpConnectionFactory<ManagedNHttpClientConnection> connFactory;

    @Mock
    private Registry<SchemeIOSessionStrategy> iosessionFactoryRegistry;

    @Mock
    private SchemePortResolver schemePortResolver;

    @Mock
    private DnsResolver dnsResolver;

    private InstrumentedNClientConnManager connManager;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        // Initialize the InstrumentedNClientConnManager instance
        connManager = new InstrumentedNClientConnManager(
                ioreactor, connFactory, schemePortResolver, metricRegistry,
                iosessionFactoryRegistry, 100, TimeUnit.MILLISECONDS, dnsResolver, "test");
    }

    /**
     * Test method to verify that metrics are registered correctly.
     */
    @Test
    public void testMetricsAreRegistered() {
        // Verify that registerGauge is called for each metric
        verify(metricRegistry, times(4)).registerGauge(anyString(), any(Gauge.class));
    }
}
