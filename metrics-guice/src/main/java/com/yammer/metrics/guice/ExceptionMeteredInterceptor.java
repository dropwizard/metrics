package com.yammer.metrics.guice;

import com.yammer.metrics.core.MeterMetric;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * A method interceptor which creates a meter for the declaring class with the
 * given name (or the method's name, if none was provided), and which measures
 * the rate at which the annotated method throws exceptions of a given type.
 */
public class ExceptionMeteredInterceptor implements MethodInterceptor {
    private final MeterMetric meter;
    private final Class<? extends Throwable> exceptionClazz;

    public ExceptionMeteredInterceptor(MeterMetric meter, Class<? extends Throwable> exceptionClazz) {
        this.meter = meter;
        this.exceptionClazz = exceptionClazz;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
    	try {
    		return invocation.proceed();
    	} catch (Throwable t) {
    		Class<? extends Throwable> caught = t.getClass();
    		if (exceptionClazz.isAssignableFrom(caught)) {
    			meter.mark();
    		}
    		throw t;
    	}
    }
}
