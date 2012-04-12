package com.yammer.metrics.guice.tests;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.yammer.metrics.HealthChecks;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.HealthCheckRegistry;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.guice.InstrumentationModule;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

public class InstrumentationModuleTest {
    private final InstrumentationModule module = new InstrumentationModule();
    private final Injector injector = Guice.createInjector(module);

    @Test
    public void defaultsToTheDefaultMetricsRegistry() throws Exception {
        assertThat(injector.getInstance(MetricsRegistry.class),
                   is(sameInstance(Metrics.defaultRegistry())));
    }

    @Test
    public void defaultsToTheDefaultHealthCheckRegistry() throws Exception {
        assertThat(injector.getInstance(HealthCheckRegistry.class),
                   is(sameInstance(HealthChecks.defaultRegistry())));
    }
}
