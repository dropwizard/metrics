package com.yammer.metrics.spring;

import com.yammer.metrics.annotation.Timed;
import com.yammer.metrics.core.*;
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

public class TimedMethodInterceptor implements MethodInterceptor, MethodCallback, Ordered {

    private static final Log log = LogFactory.getLog(TimedMethodInterceptor.class);

    private static final MethodFilter filter = new AnnotationFilter(Timed.class);

    private final MetricsRegistry metrics;
    private final Class<?> targetClass;
    private final Map<String, Timer> timers;
    private final String scope;

    public TimedMethodInterceptor(final MetricsRegistry metrics, final Class<?> targetClass, final String scope) {
        this.metrics = metrics;
        this.targetClass = targetClass;
        this.timers = new HashMap<String, Timer>();
        this.scope = scope;

        if (log.isDebugEnabled()) {
            log.debug("Creating method interceptor for class " + targetClass.getCanonicalName());
            log.debug("Scanning for @Timed annotated methods");
        }

        ReflectionUtils.doWithMethods(targetClass, this, filter);
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        final Timer timer = timers.get(invocation.getMethod().getName());
        final TimerContext timerCtx = timer != null ? timer.time() : null;
        try {
            return invocation.proceed();
        } finally {
            if (timerCtx != null) {
                timerCtx.stop();
            }
        }
    }

    @Override
    public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
        final Timed timed = method.getAnnotation(Timed.class);

        final String group = MetricName.chooseGroup(timed.group(), targetClass);
        final String type = MetricName.chooseType(timed.type(), targetClass);
        final String name = MetricName.chooseName(timed.name(), method);
        final MetricName metricName = new MetricName(group, type, name, scope);

        final Timer timer = metrics.newTimer(metricName, timed.durationUnit(), timed.rateUnit());
        timers.put(method.getName(), timer);

        if (log.isDebugEnabled()) {
            log.debug("Created metric " + metricName + " for method " + method.getName());
        }
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE;
    }

}
