package com.yammer.metrics.core;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ParallelHealthCheckRegistryTest {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final ParallelHealthCheckRegistry registry = new ParallelHealthCheckRegistry(executor, 10, TimeUnit.MILLISECONDS);

    private final HealthCheck hc1 = mock(HealthCheck.class);
    private final HealthCheck hc2 = mock(HealthCheck.class);

    private final HealthCheck.Result r1 = mock(HealthCheck.Result.class);
    private final HealthCheck.Result r2 = mock(HealthCheck.Result.class);

    @Before
    public void setUp() throws Exception {
        when(hc1.getName()).thenReturn("hc1");
        when(hc1.execute()).thenReturn(r1);

        when(hc2.getName()).thenReturn("hc2");
        when(hc2.execute()).thenReturn(r2);

        registry.register(hc1);
        registry.register(hc2);
    }

    @Test
    public void runsRegisteredHealthChecks() throws Exception {
        final Map<String,HealthCheck.Result> results = registry.runHealthChecks();

        assertThat(results,
                hasEntry("hc1", r1));

        assertThat(results,
                hasEntry("hc2", r2));
    }

    @Test
    public void removesRegisteredHealthChecks() throws Exception {
        registry.unregister(hc1);

        final Map<String, HealthCheck.Result> results = registry.runHealthChecks();

        assertThat(results,
                not(hasEntry("hc1", r1)));

        assertThat(results,
                hasEntry("hc2", r2));
    }

    @Test(timeout = 2000)
    public void healthCheckShouldBeUnhealthyWhenExecutionExceedsTimeout(){
        HealthCheck healthCheck = mock(HealthCheck.class);
        when(healthCheck.getName()).thenReturn("slowCheck");
        when(healthCheck.execute()).thenAnswer(new Answer<Object>() {
            @SuppressWarnings("InfiniteLoopStatement") @Override public Object answer(InvocationOnMock invocation) throws Throwable {
                while(true){}
            }
        });

        registry.register(healthCheck);

        final Map<String,HealthCheck.Result> results = registry.runHealthChecks();

        assertThat(results.get("slowCheck").getError(), instanceOf(TimeoutException.class));
    }
}
