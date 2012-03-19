package com.yammer.metrics.spring;

import org.springframework.util.ReflectionUtils.FieldFilter;
import org.springframework.util.ReflectionUtils.MethodFilter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class AnnotationFilter implements MethodFilter, FieldFilter {

    private final Class<? extends Annotation> clazz;

    public AnnotationFilter(final Class<? extends Annotation> clazz) {
        this.clazz = clazz;
    }

    @Override
    public boolean matches(Method method) {
        return method.isAnnotationPresent(clazz);
    }

    @Override
    public boolean matches(Field field) {
        return field.isAnnotationPresent(clazz);
    }

}