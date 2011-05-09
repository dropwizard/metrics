package com.yammer.metrics.guice;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;

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
        bindListener(Matchers.any(), new MeteredListener());
        bindListener(Matchers.any(), new TimedListener());
        bindListener(Matchers.any(), new GaugeListener());
    }
}
