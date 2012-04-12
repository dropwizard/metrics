package com.yammer.metrics.guice;

import java.lang.reflect.Method;

import com.google.inject.spi.InjectionListener;
import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricsRegistry;

/**
 * An injection listener which creates a gauge for the declaring class with the given name (or the
 * method's name, if none was provided) which returns the value returned by the annotated method.
 */
class GaugeInjectionListener<I> implements InjectionListener<I> {
    private final MetricsRegistry metricsRegistry;
    private final MetricName metricName;
    private final Method method;

    GaugeInjectionListener(MetricsRegistry metricsRegistry, MetricName metricName, Method method) {
        this.metricsRegistry = metricsRegistry;
        this.metricName = metricName;
        this.method = method;
    }

    @Override
    public void afterInjection(final I i) {
        metricsRegistry.newGauge(metricName, new Gauge<Object>() {
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
