package com.yammer.metrics.spring;

import com.yammer.metrics.annotation.ExceptionMetered;
import com.yammer.metrics.core.Meter;
import com.yammer.metrics.core.MetricsRegistry;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.core.Ordered;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.MethodCallback;
import org.springframework.util.ReflectionUtils.MethodFilter;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class ExceptionMeteredMethodInterceptor implements MethodInterceptor, MethodCallback,
                                                          Ordered {

    private static final MethodFilter filter = new AnnotationMethodFilter(ExceptionMetered.class);

    private final MetricsRegistry metrics;
    private final Class<?> targetClass;
    private final Map<String, Meter> meters;
    private final Map<String, Class<? extends Throwable>> causes;
    private final String scope;

    public ExceptionMeteredMethodInterceptor(final MetricsRegistry metrics, final Class<?> targetClass, final String scope) {
        this.metrics = metrics;
        this.targetClass = targetClass;
        this.meters = new HashMap<String, Meter>();
        this.causes = new HashMap<String, Class<? extends Throwable>>();
        this.scope = scope;

        ReflectionUtils.doWithMethods(targetClass, this, filter);
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        try {
            return invocation.proceed();
        } catch (Throwable t) {
            final String name = invocation.getMethod().getName();
            if (causes.get(name).isAssignableFrom(t.getClass())) {
                meters.get(name).mark();
            }
            ReflectionUtils.rethrowException(t);
            return null;
        }
    }

    @Override
    public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
        final ExceptionMetered metered = method.getAnnotation(ExceptionMetered.class);
        final String methodName = method.getName();
        final String meterName = metered.name().isEmpty() ?
                methodName + ExceptionMetered.DEFAULT_NAME_SUFFIX :
                metered.name();
        final Meter meter = metrics.newMeter(targetClass,
                                             meterName,
                                             scope,
                                             metered.eventType(),
                                             metered.rateUnit());
        meters.put(methodName, meter);
        causes.put(methodName, metered.cause());
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE;
    }

}
