package com.yammer.metrics.guice;

import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import com.yammer.metrics.annotation.Gauge;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricsRegistry;

import java.lang.reflect.Method;

/**
 * A listener which adds gauge injection listeners to classes with gauges.
 */
class GaugeListener implements TypeListener {
    private final MetricsRegistry metricsRegistry;

    GaugeListener(MetricsRegistry metricsRegistry) {
        this.metricsRegistry = metricsRegistry;
    }

    @Override
    public <I> void hear(final TypeLiteral<I> literal, TypeEncounter<I> encounter) {
        for (final Method method : literal.getRawType().getMethods()) {
            final Gauge annotation = method.getAnnotation(Gauge.class);
            if (annotation != null) {
                if (method.getParameterTypes().length == 0) {
                    final String group = MetricName.chooseGroup(annotation.group(), literal.getRawType());
                    final String type = MetricName.chooseType(annotation.type(), literal.getRawType());
                    final String name = MetricName.chooseName(annotation.name(), method);            
                    final MetricName metricName = new MetricName(group, type, name);
                    encounter.register(new GaugeInjectionListener<I>(metricsRegistry,
                                                                     metricName,
                                                                     method));
                } else {
                    encounter.addError("Method %s is annotated with @Gauge but requires parameters.",
                                       method);
                }
            }
        }
    }

}
