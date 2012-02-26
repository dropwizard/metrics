package com.yammer.metrics.spring;

import com.yammer.metrics.annotation.Timed;
import com.yammer.metrics.core.*;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.core.Ordered;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.MethodCallback;
import org.springframework.util.ReflectionUtils.MethodFilter;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class TimedMethodInterceptor implements MethodInterceptor, MethodCallback, Ordered {

    private static final MethodFilter filter = new AnnotationFilter(Timed.class);

    private final MetricsRegistry metrics;
    private final Class<?> targetClass;
    private final Map<String, Timer> timers;

    public TimedMethodInterceptor(final MetricsRegistry metrics, final Class<?> targetClass) {
        this.metrics = metrics;
        this.targetClass = targetClass;
        this.timers = new HashMap<String, Timer>();

        ReflectionUtils.doWithMethods(targetClass, this, filter);
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        final TimerContext tc = timers.get(invocation.getMethod().getName()).time();
        try {
            return invocation.proceed();
        } finally {
            tc.stop();
        }
    }

    @Override
    public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
        final Timed timed = method.getAnnotation(Timed.class);

        final String methodName = method.getName();
        final String group = MetricName.chooseGroup(timed.group(), targetClass);
        final String type = MetricName.chooseType(timed.type(), targetClass);
        final String name = timed.name() == null || timed.name().equals("") ? methodName : timed.name();
        final MetricName metricName = new MetricName(group, type, name);

        final Timer timer = metrics.newTimer(metricName,
                                             timed.durationUnit(),
                                             timed.rateUnit());
        timers.put(methodName, timer);
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE;
    }

}
