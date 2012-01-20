package com.yammer.metrics.spring;

import org.springframework.util.ReflectionUtils.MethodFilter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public class AnnotationMethodFilter implements MethodFilter {

    private final Class<? extends Annotation> clazz;

    public AnnotationMethodFilter(final Class<? extends Annotation> clazz) {
        this.clazz = clazz;
    }

    @Override
    public boolean matches(Method method) {
        return method.isAnnotationPresent(clazz);
    }

}
