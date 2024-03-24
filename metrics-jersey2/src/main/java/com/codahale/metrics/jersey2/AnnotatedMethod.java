package com.codahale.metrics.jersey2;

import com.codahale.metrics.Metric;
import org.glassfish.jersey.server.model.Invocable;
import org.glassfish.jersey.server.model.ResourceMethod;
import org.glassfish.jersey.server.monitoring.RequestEvent;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentMap;

final class AnnotatedMethod<T extends Annotation> {

    private T       annotation;
    private Method  method;
    private boolean methodFoundOnDefinition;

    private AnnotatedMethod(Invocable invocable, Class<T> annotationClass) {
        Method tmpMethod = invocable.getHandlingMethod();

        if (null != tmpMethod) {
            T tmpAnnotation = tmpMethod.getAnnotation(annotationClass);

            if (null != tmpAnnotation) {
                annotation              = tmpAnnotation;
                method                  = tmpMethod;
                methodFoundOnDefinition = false;

                return;
            }
        }

        tmpMethod = invocable.getDefinitionMethod();

        if (null != tmpMethod) {
            T tmpAnnotation = tmpMethod.getAnnotation(annotationClass);

            if (null != tmpAnnotation) {
                annotation              = tmpAnnotation;
                method                  = tmpMethod;
                methodFoundOnDefinition = true;
            }
        }
    }

    public T getAnnotation() {
        return annotation;
    }

    public Method getMethod() {
        return method;
    }

    public boolean hasAnnotation() {
        return ((null != annotation));
    }

    public boolean hasMethod() {
        return (null != method);
    }

    public boolean isMethodFoundOnDefinition() {
        return methodFoundOnDefinition;
    }

    public static <A extends Annotation> AnnotatedMethod<A> get(Invocable invocable, Class<A> annotationClass) {
        return new AnnotatedMethod<A>(invocable, annotationClass);
    }

    public static <A extends Annotation> AnnotatedMethod<A> get(ResourceMethod resourceMethod, Class<A> annotationClass) {
        return new AnnotatedMethod<A>(
            resourceMethod.getInvocable(),
            annotationClass
        );
    }

    public static <A extends Annotation> AnnotatedMethod<A> get(RequestEvent requestEvent, Class<A> annotationClass) {
        return new AnnotatedMethod<A>(
            requestEvent.getUriInfo().getMatchedResourceMethod().getInvocable(),
            annotationClass
        );
    }

    public static <M extends Metric, A extends Annotation> M get(ConcurrentMap<Method, M> metricsMap, RequestEvent requestEvent) {
        Invocable invocable = requestEvent.getUriInfo().getMatchedResourceMethod().getInvocable();

        M metric = metricsMap.get(invocable.getHandlingMethod());

        if (null == metric) {
            metric = metricsMap.get(invocable.getDefinitionMethod());
        }

        return metric;
    }

}
