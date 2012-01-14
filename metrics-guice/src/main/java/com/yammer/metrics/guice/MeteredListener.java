package com.yammer.metrics.guice;

import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import com.yammer.metrics.core.MetricsRegistry;
import org.aopalliance.intercept.MethodInterceptor;

import java.lang.reflect.Method;

/**
 * A listener which adds method interceptors to metered methods.
 */
class MeteredListener implements TypeListener {
    private final MetricsRegistry metricsRegistry;

    MeteredListener(MetricsRegistry metricsRegistry) {
        this.metricsRegistry = metricsRegistry;
    }

    @Override
    public <T> void hear(TypeLiteral<T> literal,
                         TypeEncounter<T> encounter) {
        final Class<? super T> klass = literal.getRawType();
        for (Method method : klass.getDeclaredMethods()) {
            final MethodInterceptor interceptor = MeteredInterceptor.forMethod(metricsRegistry,
                                                                               klass,
                                                                               method);
            if (interceptor != null) {
                encounter.bindInterceptor(Matchers.only(method), interceptor);
            }
        }
    }
}
