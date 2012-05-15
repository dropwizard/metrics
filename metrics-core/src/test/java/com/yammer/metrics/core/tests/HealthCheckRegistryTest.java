package com.yammer.metrics.core.tests;

import com.yammer.metrics.core.HealthCheck;
import com.yammer.metrics.core.HealthCheckRegistry;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static com.yammer.metrics.core.HealthCheck.Result;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HealthCheckRegistryTest {
    private final HealthCheckRegistry registry = new HealthCheckRegistry();
    
    private final HealthCheck hc1 = mock(HealthCheck.class);
    private final HealthCheck hc2 = mock(HealthCheck.class);

    @Before
    public void setUp() throws Exception {
        when(hc1.getName()).thenReturn("hc1");
        when(hc2.getName()).thenReturn("hc2");
    }

    @Test
    public void emptyHealthChecks() throws Exception {

        assertThat(registry.size(), is(0));

    }

    @Test
    public void registersHealthChecks() throws Exception {

        registry.register(hc1);

        assertThat(registry.size(), is(1));

    }

    @Test
    public void removesRegisteredHealthChecks() throws Exception {

        registry.register(hc1);
        registry.register(hc2);

        registry.unregister(hc1);

        assertThat(registry.size(), is(1));

    }

}
