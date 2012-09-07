package com.yammer.metrics.jersey;

import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.model.AbstractResourceMethod;
import com.sun.jersey.spi.container.ResourceMethodDispatchProvider;
import com.sun.jersey.spi.dispatch.RequestDispatcher;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.annotation.ExceptionMetered;
import com.yammer.metrics.annotation.Metered;
import com.yammer.metrics.annotation.Timed;
import com.yammer.metrics.core.Meter;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.core.TimerContext;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

class InstrumentedResourceMethodDispatchProvider implements ResourceMethodDispatchProvider {
    private static class TimedRequestDispatcher implements RequestDispatcher {
        private final RequestDispatcher underlying;
        private final Timer timer;

        private TimedRequestDispatcher(RequestDispatcher underlying, Timer timer) {
            this.underlying = underlying;
            this.timer = timer;
        }

        @Override
        public void dispatch(Object resource, HttpContext httpContext) {
            final TimerContext context = timer.time();
            try {
                underlying.dispatch(resource, httpContext);
            } finally {
                context.stop();
            }
        }
    }

    private static class MeteredRequestDispatcher implements RequestDispatcher {
        private final RequestDispatcher underlying;
        private final Meter meter;

        private MeteredRequestDispatcher(RequestDispatcher underlying, Meter meter) {
            this.underlying = underlying;
            this.meter = meter;
        }

        @Override
        public void dispatch(Object resource, HttpContext httpContext) {
            meter.mark();
            underlying.dispatch(resource, httpContext);
        }
    }

    private static class ExceptionMeteredRequestDispatcher implements RequestDispatcher {
        private final RequestDispatcher underlying;
        private final Meter meter;
        private final Class<? extends Throwable> exceptionClass;

        private ExceptionMeteredRequestDispatcher(RequestDispatcher underlying,
                                                  Meter meter,
                                                  Class<? extends Throwable> exceptionClass) {
            this.underlying = underlying;
            this.meter = meter;
            this.exceptionClass = exceptionClass;
        }

        @Override
        public void dispatch(Object resource, HttpContext httpContext) {
            try {
                underlying.dispatch(resource, httpContext);
            } catch (Throwable e) {
                if (exceptionClass.isAssignableFrom(e.getClass()) ||
                        (e.getCause() != null && exceptionClass.isAssignableFrom(e.getCause().getClass()))) {
                    meter.mark();
                }
                getUnsafe().throwException(e);
            }
        }
    }

    private static Unsafe getUnsafe() {
        try {
            final Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            return (Unsafe) field.get(null);
        } catch (Exception ex) {
            throw new RuntimeException("can't get Unsafe instance", ex);
        }
    }

    private final ResourceMethodDispatchProvider provider;

    public InstrumentedResourceMethodDispatchProvider(ResourceMethodDispatchProvider provider) {
        this.provider = provider;
    }

    @Override
    public RequestDispatcher create(AbstractResourceMethod method) {
        RequestDispatcher dispatcher = provider.create(method);
        if (dispatcher == null) {
            return null;
        }

        if (method.getMethod().isAnnotationPresent(Timed.class) || 
            method.getDeclaringResource().getResourceClass().isAnnotationPresent(Timed.class)) {
            dispatcher = getTimedRequestDispatcher(method, dispatcher);
        }

        if (method.getMethod().isAnnotationPresent(Metered.class) ||
        	method.getDeclaringResource().getResourceClass().isAnnotationPresent(Metered.class)) {
            dispatcher = getMeteredRequestDispatcher(method, dispatcher);
        }

        if (method.getMethod().isAnnotationPresent(ExceptionMetered.class)  ||
            method.getDeclaringResource().getResourceClass().isAnnotationPresent(ExceptionMetered.class)) {
            dispatcher = getExceptionMeteredRequestDispatcher(method, dispatcher);
        }

        return dispatcher;
    }

	private RequestDispatcher getTimedRequestDispatcher(AbstractResourceMethod method, RequestDispatcher dispatcher) {
		Timed annotation = null; 
		if (method.getDeclaringResource().getResourceClass().isAnnotationPresent(Timed.class)) 
			annotation = method.getDeclaringResource().getResourceClass().getAnnotation(Timed.class);
		
		if (method.getMethod().isAnnotationPresent(Timed.class))
			annotation = method.getMethod().getAnnotation(Timed.class);
		
		Class<?> klass = method.getDeclaringResource().getResourceClass();
		String group = MetricName.chooseGroup(annotation.group(), klass);
		String type = MetricName.chooseType(annotation.type(), klass);
		String name = MetricName.chooseName(annotation.name(), method.getMethod());            
		MetricName metricName = new MetricName(group, type, name);
		
		final Timer timer = Metrics.newTimer(metricName,
		                                           annotation.durationUnit() == null ?
		                                                   TimeUnit.MILLISECONDS : annotation.durationUnit(),
		                                           annotation.rateUnit() == null ?
		                                                   TimeUnit.SECONDS : annotation.rateUnit());
		dispatcher = new TimedRequestDispatcher(dispatcher, timer);
		return dispatcher;
	}
	
	private RequestDispatcher getMeteredRequestDispatcher(AbstractResourceMethod method, RequestDispatcher dispatcher) {
		Metered annotation = null; 
		if (method.getDeclaringResource().getResourceClass().isAnnotationPresent(Metered.class)) 
			annotation = method.getDeclaringResource().getResourceClass().getAnnotation(Metered.class);
		
		if (method.getMethod().isAnnotationPresent(Metered.class))
			annotation = method.getMethod().getAnnotation(Metered.class);
		
		Class<?> klass = method.getDeclaringResource().getResourceClass();
		String group = MetricName.chooseGroup(annotation.group(), klass);
		String type = MetricName.chooseType(annotation.type(), klass);
		String name = MetricName.chooseName(annotation.name(), method.getMethod());            
		MetricName metricName = new MetricName(group, type, name);
		
		final Meter meter = Metrics.newMeter(metricName,
		                                           annotation.eventType() == null ?
		                                                   "requests" : annotation.eventType(),
		                                           annotation.rateUnit() == null ?
		                                                   TimeUnit.SECONDS : annotation.rateUnit());
		dispatcher = new MeteredRequestDispatcher(dispatcher, meter);
		return dispatcher;
	}	

	private RequestDispatcher getExceptionMeteredRequestDispatcher(AbstractResourceMethod method, RequestDispatcher dispatcher) {
		ExceptionMetered annotation = null; 
		if (method.getDeclaringResource().getResourceClass().isAnnotationPresent(ExceptionMetered.class)) 
			annotation = method.getDeclaringResource().getResourceClass().getAnnotation(ExceptionMetered.class);
		
		if (method.getMethod().isAnnotationPresent(ExceptionMetered.class))
			annotation = method.getMethod().getAnnotation(ExceptionMetered.class);


		Class<?> klass = method.getDeclaringResource().getResourceClass();
		String group = MetricName.chooseGroup(annotation.group(), klass);
		String type = MetricName.chooseType(annotation.type(), klass);
		String name = annotation.name() == null || annotation.name().equals("") ?
		        method.getMethod().getName() + ExceptionMetered.DEFAULT_NAME_SUFFIX : annotation.name();            
		MetricName metricName = new MetricName(group, type, name);
		
		final Meter meter = Metrics.newMeter(metricName,
		                                           annotation.eventType() == null ?
		                                                   "requests" : annotation.eventType(),
		                                           annotation.rateUnit() == null ?
		                                                   TimeUnit.SECONDS : annotation.rateUnit());
		dispatcher = new ExceptionMeteredRequestDispatcher(dispatcher, meter, annotation.cause());
		return dispatcher;
	}	
}
