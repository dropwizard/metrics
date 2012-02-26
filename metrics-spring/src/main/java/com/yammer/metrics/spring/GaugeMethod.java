package com.yammer.metrics.spring;

import com.yammer.metrics.core.Gauge;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

public class GaugeMethod extends Gauge<Object> {

    private final Object bean;
    private final Method method;

    public GaugeMethod(final Object bean, final Method method) {
        this.bean = bean;
        this.method = method;

        ReflectionUtils.makeAccessible(method);
    }

    @Override
    public Object value() {
        return ReflectionUtils.invokeMethod(method, bean);
    }

}
