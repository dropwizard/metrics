package com.yammer.metrics.spring;

import com.yammer.metrics.core.Gauge;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

public class GaugeField extends Gauge<Object> {

    private final Object bean;
    private final Field field;

    public GaugeField(final Object bean, final Field field) {
        this.bean = bean;
        this.field = field;

        ReflectionUtils.makeAccessible(field);
    }

    @Override
    public Object value() {
        return ReflectionUtils.getField(field, bean);
    }

}
