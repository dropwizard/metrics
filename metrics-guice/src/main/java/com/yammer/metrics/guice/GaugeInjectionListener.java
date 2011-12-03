package com.yammer.metrics.guice;

import com.google.inject.TypeLiteral;
import com.google.inject.spi.InjectionListener;
import com.yammer.metrics.core.GaugeMetric;
import com.yammer.metrics.core.MetricsRegistry;

import java.lang.reflect.Method;

/**
 * An injection listener which creates a gauge for the declaring class with the given name (or the
 * method's name, if none was provided) which returns the value returned by the annotated method.
 */
public class GaugeInjectionListener<I> implements InjectionListener<I> {
    private final MetricsRegistry metricsRegistry;
    private final TypeLiteral<I> literal;
    private final String name;
    private final Method method;

    public GaugeInjectionListener(MetricsRegistry metricsRegistry, TypeLiteral<I> literal, String name, Method method) {
        this.metricsRegistry = metricsRegistry;
        this.literal = literal;
        this.name = name;
        this.method = method;
    }

    @Override
    public void afterInjection(final I i) {
        metricsRegistry.newGauge(literal.getRawType(), name, new GaugeMetric<Object>() {
            @Override
            public Object value() {
                try {
                    return method.invoke(i);
                } catch (Exception e) {
                    return new RuntimeException(e);
                }
            }
        });
    }
}
