package com.yammer.metrics.guice;

import com.yammer.metrics.annotation.ExceptionMetered;
import com.yammer.metrics.core.Meter;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricsRegistry;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.Method;

/**
 * A method interceptor which creates a meter for the declaring class with the given name (or the
 * method's name, if none was provided), and which measures the rate at which the annotated method
 * throws exceptions of a given type.
 */
class ExceptionMeteredInterceptor implements MethodInterceptor {
    static MethodInterceptor forMethod(MetricsRegistry metricsRegistry, Class<?> klass, Method method) {
        final ExceptionMetered annotation = method.getAnnotation(ExceptionMetered.class);
        if (annotation != null) {
            final String group = MetricName.chooseGroup(annotation.group(), klass);
            final String type = MetricName.chooseType(annotation.type(), klass);
            final String name = determineName(annotation, method);
            final MetricName metricName = new MetricName(group, type, name);
            final Meter meter = metricsRegistry.newMeter(metricName,
                                                               annotation.eventType(),
                                                               annotation.rateUnit());
            return new ExceptionMeteredInterceptor(meter, annotation.cause());
        }
        return null;
    }

    private static String determineName(ExceptionMetered annotation, Method method) {
        if (annotation.name().isEmpty()) {
            return method.getName() + ExceptionMetered.DEFAULT_NAME_SUFFIX;
        } else {
            return annotation.name();
        }
    }

    private final Meter meter;
    private final Class<? extends Throwable> klass;

    private ExceptionMeteredInterceptor(Meter meter, Class<? extends Throwable> klass) {
        this.meter = meter;
        this.klass = klass;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        try {
            return invocation.proceed();
        } catch (Throwable t) {
            if (klass.isAssignableFrom(t.getClass())) {
                meter.mark();
            }
            throw t;
        }
    }
}
