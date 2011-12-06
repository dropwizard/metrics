package com.yammer.metrics.guice;

import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import com.yammer.metrics.core.MetricsRegistry;

import java.lang.reflect.Method;

/**
 * A listener which adds gauge injection listeners to classes with gauges.
 */
public class GaugeListener implements TypeListener {
    private final MetricsRegistry metricsRegistry;

    public GaugeListener(MetricsRegistry metricsRegistry) {
        this.metricsRegistry = metricsRegistry;
    }

    @Override
    public <I> void hear(final TypeLiteral<I> literal, TypeEncounter<I> encounter) {
        for (final Method method : literal.getRawType().getMethods()) {
            final Gauge annotation = method.getAnnotation(Gauge.class);
            if (annotation != null) {
                if (method.getParameterTypes().length == 0) {
                    final String name = annotation.name()
                                                  .isEmpty() ? method.getName() : annotation.name();
                    encounter.register(new GaugeInjectionListener<I>(metricsRegistry,
                                                                     literal,
                                                                     name,
                                                                     method));
                } else {
                    encounter.addError("Method %s is annotated with @Gauge but requires parameters.",
                                       method);
                }
            }
        }
    }

}
