package com.yammer.metrics.guice;

import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.core.MeterMetric;

import java.lang.reflect.Method;

/**
 * A listener which adds method interceptors to methods that should be instrumented for exceptions
 */
public class ExceptionMeteredListener implements TypeListener {
    private final MetricsRegistry metricsRegistry;

    public ExceptionMeteredListener(MetricsRegistry metricsRegistry) {
        this.metricsRegistry = metricsRegistry;
    }

    @Override
    public <T> void hear(TypeLiteral<T> literal,
                         TypeEncounter<T> encounter) {
        for (Method method : literal.getRawType().getDeclaredMethods()) {
            final ExceptionMetered annotation = method.getAnnotation(ExceptionMetered.class);
            if (annotation != null) {
                final String name = determineName(annotation, method);
                final MeterMetric meter = metricsRegistry.newMeter(literal.getRawType(), name, annotation.eventType(), annotation.rateUnit());
                ExceptionMeteredInterceptor interceptor = new ExceptionMeteredInterceptor(meter, annotation.cause());
				encounter.bindInterceptor(Matchers.only(method), interceptor);
            }
        }
    }
    
    private String determineName(final ExceptionMetered annotation, final Method method) {
    	if (annotation.name().isEmpty()) {
    		return method.getName() + ExceptionMetered.DEFAULT_NAME_SUFFIX;
    	} else {
    		return annotation.name();
    	}
    }
}
