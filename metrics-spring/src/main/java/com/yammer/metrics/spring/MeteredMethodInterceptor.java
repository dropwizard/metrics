package com.yammer.metrics.spring;

import com.yammer.metrics.annotation.Metered;
import com.yammer.metrics.core.Meter;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricsRegistry;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.MethodCallback;
import org.springframework.util.ReflectionUtils.MethodFilter;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class MeteredMethodInterceptor implements MethodInterceptor, MethodCallback {

    private static final MethodFilter filter = new AnnotationFilter(Metered.class);

    protected final MetricsRegistry metrics;
    protected final Class<?> targetClass;
    protected final Map<String, Meter> meters;

    public MeteredMethodInterceptor(final MetricsRegistry metrics, final Class<?> targetClass) {
        this.metrics = metrics;
        this.targetClass = targetClass;
        this.meters = new HashMap<String, Meter>();

        ReflectionUtils.doWithMethods(targetClass, this, filter);
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        meters.get(invocation.getMethod().getName()).mark();
        return invocation.proceed();
    }

    @Override
    public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
        final Metered metered = method.getAnnotation(Metered.class);

        final String methodName = method.getName();
        final String group = MetricName.chooseGroup(metered.group(), targetClass);
        final String type = MetricName.chooseType(metered.type(), targetClass);
        final String name = metered.name() == null || metered.name().equals("") ? methodName : metered.name();
        final MetricName metricName = new MetricName(group, type, name);
        final Meter meter = metrics.newMeter(metricName, metered.eventType(), metered.rateUnit());

        meters.put(methodName, meter);
    }

}
