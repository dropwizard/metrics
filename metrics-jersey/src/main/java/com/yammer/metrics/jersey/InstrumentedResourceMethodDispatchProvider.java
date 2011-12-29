package com.yammer.metrics.jersey;

import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.model.AbstractResourceMethod;
import com.sun.jersey.server.impl.model.method.dispatch.EntityParamDispatchProvider;
import com.sun.jersey.spi.container.JavaMethodInvoker;
import com.sun.jersey.spi.dispatch.RequestDispatcher;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.annotation.ExceptionMetered;
import com.yammer.metrics.annotation.Metered;
import com.yammer.metrics.annotation.Timed;
import com.yammer.metrics.core.MeterMetric;
import com.yammer.metrics.core.TimerContext;
import com.yammer.metrics.core.TimerMetric;
import sun.misc.Unsafe;

import javax.ws.rs.ext.Provider;
import java.util.concurrent.TimeUnit;

@Provider
public class InstrumentedResourceMethodDispatchProvider extends EntityParamDispatchProvider {
    private static class TimedRequestDispatcher implements RequestDispatcher {
        private final RequestDispatcher underlying;
        private final TimerMetric timer;

        private TimedRequestDispatcher(RequestDispatcher underlying, TimerMetric timer) {
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
        private final MeterMetric meter;

        private MeteredRequestDispatcher(RequestDispatcher underlying, MeterMetric meter) {
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
        private final MeterMetric meter;
        private final Class<? extends Throwable> exceptionClass;

        private ExceptionMeteredRequestDispatcher(RequestDispatcher underlying,
                                                  MeterMetric meter,
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


    @Override
    public RequestDispatcher create(AbstractResourceMethod method, JavaMethodInvoker invoker) {
        RequestDispatcher dispatcher = super.create(method, invoker);
        if (dispatcher == null) {
            return null;
        }

        if (method.getMethod().isAnnotationPresent(Timed.class)) {
            final Timed annotation = method.getMethod().getAnnotation(Timed.class);
            final TimerMetric timer = Metrics.newTimer(method.getDeclaringResource().getResourceClass(),
                                                       annotation.name() == null ?
                                                               method.getMethod().getName() : annotation.name(),
                                                       annotation.durationUnit() == null ?
                                                               TimeUnit.MILLISECONDS : annotation.durationUnit(),
                                                       annotation.rateUnit() == null ?
                                                               TimeUnit.SECONDS : annotation.rateUnit());
            dispatcher = new TimedRequestDispatcher(dispatcher, timer);
        }

        if (method.getMethod().isAnnotationPresent(Metered.class)) {
            final Metered annotation = method.getMethod().getAnnotation(Metered.class);
            final MeterMetric meter = Metrics.newMeter(method.getDeclaringResource().getResourceClass(),
                                                       annotation.name() == null ?
                                                               method.getMethod().getName() : annotation.name(),
                                                       annotation.eventType() == null ?
                                                               "requests" : annotation.eventType(),
                                                       annotation.rateUnit() == null ?
                                                               TimeUnit.SECONDS : annotation.rateUnit());
            dispatcher = new MeteredRequestDispatcher(dispatcher, meter);
        }

        if (method.getMethod().isAnnotationPresent(ExceptionMetered.class)) {
            final ExceptionMetered annotation = method.getMethod().getAnnotation(ExceptionMetered.class);
            final MeterMetric meter = Metrics.newMeter(method.getDeclaringResource().getResourceClass(),
                                                       annotation.name() == null ?
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
