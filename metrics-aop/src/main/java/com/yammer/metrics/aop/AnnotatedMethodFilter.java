package com.yammer.metrics.aop;

import com.yammer.metrics.aop.annotation.ExceptionMetered;
import com.yammer.metrics.aop.annotation.Metered;
import com.yammer.metrics.aop.annotation.Timed;
import javassist.util.proxy.MethodFilter;

import java.lang.reflect.Method;

class AnnotatedMethodFilter implements MethodFilter {
    @Override
    public boolean isHandled(Method method) {
        return method.isAnnotationPresent(Timed.class) ||
                method.isAnnotationPresent(Metered.class) ||
                method.isAnnotationPresent(ExceptionMetered.class);
    }
}
