package com.yammer.metrics.core.tests;

import com.yammer.metrics.core.HealthCheck;
import com.yammer.metrics.core.HealthCheckRunner;
import com.yammer.metrics.core.SequentialHealthCheckRunner;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SequentialHealthCheckRunnerTest {

    private final ConcurrentMap<String, HealthCheck> checks = new ConcurrentHashMap<String, HealthCheck>();

    private final HealthCheckRunner runner = new SequentialHealthCheckRunner(checks);

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

        checks.put("hc1", hc1);
        checks.put("hc2", hc2);
    }

    @Test
    public void runsRegisteredHealthChecks() throws Exception {

        final Map<String,HealthCheck.Result> results = runner.run();

        assertThat(results, hasEntry("hc1", r1));
        assertThat(results, hasEntry("hc2", r2));

    }

}
