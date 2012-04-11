package com.yammer.metrics.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.matcher.Matchers;
import com.yammer.metrics.HealthChecks;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.HealthCheckRegistry;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.reporting.JmxReporter;

/**
 * A Guice module which instruments methods annotated with the {@link com.yammer.metrics.annotation.Metered} and {@link com.yammer.metrics.annotation.Timed}
 * annotations.
 *
 * @see com.yammer.metrics.annotation.Gauge
 * @see com.yammer.metrics.annotation.Metered
 * @see com.yammer.metrics.annotation.Timed
 * @see MeteredInterceptor
 * @see TimedInterceptor
 * @see GaugeInjectionListener
 */
public class InstrumentationModule extends AbstractModule {
    @Override
    protected void configure() {
        final MetricsRegistry metricsRegistry = createMetricsRegistry();
        bind(MetricsRegistry.class).toInstance(metricsRegistry);
        bind(HealthCheckRegistry.class).toInstance(createHealthCheckRegistry());
        bindJmxReporter();
        bindListener(Matchers.any(), new MeteredListener(metricsRegistry));
        bindListener(Matchers.any(), new TimedListener(metricsRegistry));
        bindListener(Matchers.any(), new GaugeListener(metricsRegistry));
        bindListener(Matchers.any(), new ExceptionMeteredListener(metricsRegistry));
    }

    /**
     * Override to provide a custom binding for {@link JmxReporter}
     */
    protected void bindJmxReporter() {
        bind(JmxReporter.class).toProvider(JmxReporterProvider.class).in(Scopes.SINGLETON);
    }

    /**
     * Override to provide a custom {@link HealthCheckRegistry}
     */
    protected HealthCheckRegistry createHealthCheckRegistry() {
        return HealthChecks.defaultRegistry();
    }

    /**
     * Override to provide a custom {@link MetricsRegistry}
     */
    protected MetricsRegistry createMetricsRegistry() {
        return Metrics.defaultRegistry();
    }
}
