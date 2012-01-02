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
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.core.TimerContext;
import sun.misc.Unsafe;

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
                if (exceptionClass.isAssignableFrom(e.getClass())) {
                    meter.mark();
                }
                Unsafe.getUnsafe().throwException(e);
            }
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

        if (method.getMethod().isAnnotationPresent(Timed.class)) {
            final Timed annotation = method.getMethod().getAnnotation(Timed.class);
            final Timer timer = Metrics.newTimer(method.getDeclaringResource().getResourceClass(),
                                                       annotation.name() == null || annotation.name().equals("")  ?
                                                               method.getMethod().getName() : annotation.name(),
                                                       annotation.durationUnit() == null ?
                                                               TimeUnit.MILLISECONDS : annotation.durationUnit(),
                                                       annotation.rateUnit() == null ?
                                                               TimeUnit.SECONDS : annotation.rateUnit());
            dispatcher = new TimedRequestDispatcher(dispatcher, timer);
        }

        if (method.getMethod().isAnnotationPresent(Metered.class)) {
            final Metered annotation = method.getMethod().getAnnotation(Metered.class);
            final Meter meter = Metrics.newMeter(method.getDeclaringResource().getResourceClass(),
                                                       annotation.name() == null || annotation.name().equals("") ?
                                                               method.getMethod().getName() : annotation.name(),
                                                       annotation.eventType() == null ?
                                                               "requests" : annotation.eventType(),
                                                       annotation.rateUnit() == null ?
                                                               TimeUnit.SECONDS : annotation.rateUnit());
            dispatcher = new MeteredRequestDispatcher(dispatcher, meter);
        }

        if (method.getMethod().isAnnotationPresent(ExceptionMetered.class)) {
            final ExceptionMetered annotation = method.getMethod().getAnnotation(ExceptionMetered.class);
            final Meter meter = Metrics.newMeter(method.getDeclaringResource().getResourceClass(),
                                                       annotation.name() == null || annotation.name().equals("") ?
                                                               method.getMethod().getName() + ExceptionMetered.DEFAULT_NAME_SUFFIX : annotation.name(),
                                                       annotation.eventType() == null ?
                                                               "requests" : annotation.eventType(),
                                                       annotation.rateUnit() == null ?
                                                               TimeUnit.SECONDS : annotation.rateUnit());
            dispatcher = new ExceptionMeteredRequestDispatcher(dispatcher, meter, annotation.cause());
        }

        return dispatcher;
    }
}
