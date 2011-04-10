package com.yammer.metrics.guice;

import java.lang.reflect.Method;

import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.*;

/**
 * A listener which adds method interceptors to metered methods.
 */
public class MeteredListener implements TypeListener {
	@Override
	public <T> void hear(TypeLiteral<T> literal,
						 TypeEncounter<T> encounter) {
		for (Method method : literal.getRawType().getMethods()) {
			final Metered annotation = method.getAnnotation(Metered.class);
			if (annotation != null) {
				final String name = annotation.name().isEmpty() ? method.getName() : annotation.name();
				final MeterMetric meter = Metrics.newMeter(new MetricName(literal.getRawType(), name), annotation.eventType(), annotation.rateUnit());
				encounter.bindInterceptor(Matchers.only(method), new MeteredInterceptor(meter));
			}
		}
	}
}
