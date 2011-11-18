package com.yammer.metrics.guice;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;
import com.yammer.metrics.core.HealthCheckRegistry;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.reporting.JmxReporter;

/**
 * A Guice module which instruments methods annotated with the {@link Metered}
 * and {@link Timed} annotations.
 *
 * @see Gauge
 * @see Metered
 * @see Timed
 * @see MeteredInterceptor
 * @see TimedInterceptor
 * @see GaugeInjectionListener
 */
public class InstrumentationModule extends AbstractModule {
    @Override
    protected void configure() {
        MetricsRegistry metricsRegistry = createMetricsRegistry();
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
    protected void bindJmxReporter()
    {
        bind(JmxReporter.class).toProvider(JmxReporterProvider.class).asEagerSingleton();
    }

    /**
     * Override to provide a custom {@link HealthCheckRegistry}
     * 
     * @return
     */
    protected HealthCheckRegistry createHealthCheckRegistry()
    {
        return new HealthCheckRegistry();
    }

    /**
     * Override to provide a custom {@link MetricsRegistry}
     * 
     * @return
     */
    protected MetricsRegistry createMetricsRegistry()
    {
        return new MetricsRegistry();
    }
}
