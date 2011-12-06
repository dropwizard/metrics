package com.yammer.metrics.core.tests;

import com.yammer.metrics.core.HealthCheck;
import com.yammer.metrics.core.HealthCheckRegistry;
import com.yammer.metrics.core.MetricName;

import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static com.yammer.metrics.core.HealthCheck.Result;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HealthCheckRegistryTest {
    private final HealthCheckRegistry registry = new HealthCheckRegistry();
    
    private final HealthCheck hc1 = mock(HealthCheck.class);
    private final HealthCheck hc2 = mock(HealthCheck.class);

    private final Result r1 = mock(Result.class);
    private final Result r2 = mock(Result.class);

    @Before
    public void setUp() throws Exception {
        when(hc1.name()).thenReturn(new MetricName(HealthCheck.class, "hc1"));
        when(hc1.execute()).thenReturn(r1);
        
        when(hc2.name()).thenReturn(new MetricName(HealthCheck.class, "hc2"));
        when(hc2.execute()).thenReturn(r2);

        registry.register(hc1);
        registry.register(hc2);
    }

    @Test
    public void runsRegisteredHealthChecks() throws Exception {
        final Map<MetricName,HealthCheck.Result> results = registry.runHealthChecks();

        assertThat(results,
                   hasEntry(new MetricName(HealthCheck.class, "hc1"), r1));

        assertThat(results,
                   hasEntry(new MetricName(HealthCheck.class, "hc2"), r2));
    }

    @Test
    public void removesRegisteredHealthChecks() throws Exception {
        registry.unregister(hc1);

        final Map<MetricName, HealthCheck.Result> results = registry.runHealthChecks();

        assertThat(results,
                   not(hasEntry(new MetricName(HealthCheck.class, "hc1"), r1)));

        assertThat(results,
                   hasEntry(new MetricName(HealthCheck.class, "hc2"), r2));
    }
}
