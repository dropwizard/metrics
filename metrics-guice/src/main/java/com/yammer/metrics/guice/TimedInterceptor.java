package com.yammer.metrics.guice;

import com.yammer.metrics.core.TimerMetric;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.util.concurrent.TimeUnit;

/**
 * A method interceptor which creates a timer for the declaring class with the
 * given name (or the method's name, if none was provided), and which times
 * the execution of the annotated method.
 */
public class TimedInterceptor implements MethodInterceptor {
    private final TimerMetric timer;

    public TimedInterceptor(TimerMetric timer) {
        this.timer = timer;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        final long startTime = System.nanoTime();
        try {
            return invocation.proceed();
        } finally {
            timer.update(System.nanoTime() - startTime, TimeUnit.NANOSECONDS);
        }
    }
}
