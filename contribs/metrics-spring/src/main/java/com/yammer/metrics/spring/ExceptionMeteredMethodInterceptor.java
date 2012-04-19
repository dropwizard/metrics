package com.yammer.metrics.spring;

import com.yammer.metrics.annotation.ExceptionMetered;
import com.yammer.metrics.core.Meter;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricsRegistry;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.Ordered;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.MethodCallback;
import org.springframework.util.ReflectionUtils.MethodFilter;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class ExceptionMeteredMethodInterceptor implements MethodInterceptor, MethodCallback,
                                                          Ordered {

    private static final Log log = LogFactory.getLog(ExceptionMeteredMethodInterceptor.class);

    private static final MethodFilter filter = new AnnotationFilter(ExceptionMetered.class);

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

        if (log.isDebugEnabled()) {
            log.debug("Creating method interceptor for class " + targetClass.getCanonicalName());
            log.debug("Scanning for @ExceptionMetered annotated methods");
        }

        ReflectionUtils.doWithMethods(targetClass, this, filter);
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        try {
            return invocation.proceed();
        } catch (Throwable t) {
            final String name = invocation.getMethod().getName();
            final Class<?> cause = causes.get(name);
            if (cause != null && cause.isAssignableFrom(t.getClass())) {
                // it may be safe to infer that `meter` is non-null if `cause` is non-null
                Meter meter = meters.get(name);
                if (meter != null) {
                    meter.mark();
                }
            }
            throw t;
        }
    }

    @Override
    public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
        final ExceptionMetered metered = method.getAnnotation(ExceptionMetered.class);

        final String methodName = method.getName();
        final String group = MetricName.chooseGroup(metered.group(), targetClass);
        final String type = MetricName.chooseType(metered.type(), targetClass);
        final String name = metered.name() == null || metered.name().equals("") ? methodName + ExceptionMetered.DEFAULT_NAME_SUFFIX : metered.name();
        final MetricName metricName = new MetricName(group, type, name, scope);
        final Meter meter = metrics.newMeter(metricName, metered.eventType(), metered.rateUnit());

        meters.put(methodName, meter);
        causes.put(methodName, metered.cause());

        if (log.isDebugEnabled()) {
            log.debug("Created metric " + metricName + " for method " + methodName);
        }
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE;
    }

}
