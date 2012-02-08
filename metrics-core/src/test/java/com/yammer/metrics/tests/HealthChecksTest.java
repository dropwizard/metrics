package com.yammer.metrics.tests;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.yammer.metrics.HealthChecks;
import com.yammer.metrics.core.HealthCheck;
import com.yammer.metrics.core.HealthCheckRegistry;
import com.yammer.metrics.core.MetricName;

public class HealthChecksTest {
    private static class ExampleHealthCheck extends HealthCheck {
        private ExampleHealthCheck() {
            super(ExampleHealthCheck.class, "example");
        }

        @Override
        protected Result check() throws Exception {
            return Result.healthy("whee");
        }
    }

    @Before
    public void setUp() throws Exception {
        HealthChecks.register(new ExampleHealthCheck());
    }

    @Test
    public void runsRegisteredHealthChecks() throws Exception {
        final Map<MetricName, HealthCheck.Result> results = HealthChecks.runHealthChecks();

        assertThat(results.get(new MetricName(ExampleHealthCheck.class, "example")),
                   is(HealthCheck.Result.healthy("whee")));
    }

    @Test
    public void hasADefaultRegistry() throws Exception {
        assertThat(HealthChecks.defaultRegistry(),
                   is(instanceOf(HealthCheckRegistry.class)));
    }
}
