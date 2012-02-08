package com.yammer.metrics.guice;

import com.yammer.metrics.annotation.Metered;
import com.yammer.metrics.core.Meter;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricsRegistry;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.Method;

/**
 * A method interceptor which creates a meter for the declaring class with the given name (or the
 * method's name, if none was provided), and which measures the rate at which the annotated method
 * is invoked.
 */
class MeteredInterceptor implements MethodInterceptor {
    static MethodInterceptor forMethod(MetricsRegistry metricsRegistry, Class<?> klass, Method method) {
        final Metered annotation = method.getAnnotation(Metered.class);
        if (annotation != null) {
            final String group = MetricName.chooseGroup(annotation.group(), klass);
            final String type = MetricName.chooseType(annotation.type(), klass);
            final String name = MetricName.chooseName(annotation.name(), method);            
            final MetricName metricName = new MetricName(group, type, name);
            final Meter meter = metricsRegistry.newMeter(metricName,
                                                               annotation.eventType(),
                                                               annotation.rateUnit());
            return new MeteredInterceptor(meter);
        }
        return null;
    }

    private final Meter meter;

    private MeteredInterceptor(Meter meter) {
        this.meter = meter;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        meter.mark();
        return invocation.proceed();
    }
}
