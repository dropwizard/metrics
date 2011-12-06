package com.yammer.metrics.guice;

import com.yammer.metrics.core.MeterMetric;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * A method interceptor which creates a meter for the declaring class with the given name (or the
 * method's name, if none was provided), and which measures the rate at which the annotated method
 * is invoked.
 */
public class MeteredInterceptor implements MethodInterceptor {
    private final MeterMetric meter;

    public MeteredInterceptor(MeterMetric meter) {
        this.meter = meter;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        meter.mark();
        return invocation.proceed();
    }
}
