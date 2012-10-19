package com.yammer.metrics.tests;

import com.yammer.metrics.HealthChecks;
import com.yammer.metrics.core.HealthCheck;
import com.yammer.metrics.core.HealthCheckRegistry;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class HealthChecksTest {
    private final HealthCheckRegistry registry = HealthChecks.defaultRegistry();

    private static class ExampleHealthCheck extends HealthCheck {
        private ExampleHealthCheck() {
            super("example");
        }

        @Override
        protected Result check() throws Exception {
            return Result.healthy("whee");
        }
    }

    @Before
    public void setUp() throws Exception {
        registry.register(new ExampleHealthCheck());
    }

    @Test
    public void runsRegisteredHealthChecks() throws Exception {
        final Map<String, HealthCheck.Result> results = registry.runHealthChecks();

        assertThat(results.get("example"),
                   is(HealthCheck.Result.healthy("whee")));
    }
}
