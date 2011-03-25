package com.yammer.metrics.guice;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;

/**
 * A Guice module which instruments methods annotated with the {@link Metered}
 * and {@link Timed} annotations.
 *
 * @see Metered
 * @see Timed
 * @see MeteredInterceptor
 * @see TimedInterceptor
 */
public class InstrumentationModule extends AbstractModule {
	@Override
	protected void configure() {
		bindInterceptor(Matchers.any(), Matchers.annotatedWith(Metered.class), new MeteredInterceptor());
		bindInterceptor(Matchers.any(), Matchers.annotatedWith(Timed.class), new TimedInterceptor());
	}
}
