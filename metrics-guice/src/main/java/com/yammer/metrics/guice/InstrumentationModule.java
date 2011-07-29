package com.yammer.metrics.guice;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;
import com.yammer.metrics.MetricsRegistry;

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
        MetricsRegistry metricsRegistry = new MetricsRegistry();
        bind(MetricsRegistry.class).toInstance(metricsRegistry);
        bindListener(Matchers.any(), new MeteredListener(metricsRegistry));
        bindListener(Matchers.any(), new TimedListener(metricsRegistry));
        bindListener(Matchers.any(), new GaugeListener(metricsRegistry));
    }
}
