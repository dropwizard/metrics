package com.yammer.metrics.guice;

import java.lang.reflect.Method;

import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.*;

/**
 * A listener which adds method interceptors to timed methods.
 */
public class TimedListener implements TypeListener {
	@Override
	public <T> void hear(TypeLiteral<T> literal,
						 TypeEncounter<T> encounter) {
		for (Method method : literal.getRawType().getMethods()) {
			final Timed annotation = method.getAnnotation(Timed.class);
			if (annotation != null) {
				final String name = annotation.name().isEmpty() ? method.getName() : annotation.name();
				final TimerMetric timer = Metrics.newTimer(new MetricName(literal.getRawType(), name), annotation.durationUnit(), annotation.rateUnit());
				encounter.bindInterceptor(Matchers.only(method), new TimedInterceptor(timer));
			}
		}
	}
}
