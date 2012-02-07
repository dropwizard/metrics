package com.yammer.metrics.spring;

import com.yammer.metrics.annotation.Timed;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.core.TimerContext;
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

    private static final MethodFilter filter = new AnnotationMethodFilter(Timed.class);

    private final MetricsRegistry metrics;
    private final Class<?> targetClass;
    private final Map<String, Timer> timers;
    private final String scope;

    public TimedMethodInterceptor(final MetricsRegistry metrics, final Class<?> targetClass, final String scope) {
        this.metrics = metrics;
        this.targetClass = targetClass;
        this.timers = new HashMap<String, Timer>();
        this.scope = scope;

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
        final String timerName = timed.name().isEmpty() ? methodName : timed.name();
        final Timer timer = metrics.newTimer(targetClass,
                                             timerName,
                                             scope,
                                             timed.durationUnit(),
                                             timed.rateUnit());
        timers.put(methodName, timer);
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE;
    }

}
