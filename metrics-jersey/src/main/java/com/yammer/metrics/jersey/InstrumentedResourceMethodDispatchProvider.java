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
            } catch (Exception e) {
                if (exceptionClass.isAssignableFrom(e.getClass()) ||
                        (e.getCause() != null && exceptionClass.isAssignableFrom(e.getCause()
                                                                                  .getClass()))) {
                    meter.mark();
                }
                InstrumentedResourceMethodDispatchProvider.<RuntimeException>rethrow(e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <E extends Exception> void rethrow(Exception e) throws E {
        throw (E) e;
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

            final Class<?> klass = method.getDeclaringResource().getResourceClass();
            final String group = MetricName.chooseGroup(annotation.group(), klass);
            final String type = MetricName.chooseType(annotation.type(), klass);
            final String name = MetricName.chooseName(annotation.name(), method.getMethod());
            final MetricName metricName = new MetricName(group, type, name);

            final Timer timer = Metrics.newTimer(metricName,
                                                 annotation.durationUnit() == null ?
                                                         TimeUnit.MILLISECONDS : annotation.durationUnit(),
                                                 annotation.rateUnit() == null ?
                                                         TimeUnit.SECONDS : annotation.rateUnit());
            dispatcher = new TimedRequestDispatcher(dispatcher, timer);
        }

        if (method.getMethod().isAnnotationPresent(Metered.class)) {
            final Metered annotation = method.getMethod().getAnnotation(Metered.class);

            final Class<?> klass = method.getDeclaringResource().getResourceClass();
            final String group = MetricName.chooseGroup(annotation.group(), klass);
            final String type = MetricName.chooseType(annotation.type(), klass);
            final String name = MetricName.chooseName(annotation.name(), method.getMethod());
            final MetricName metricName = new MetricName(group, type, name);

            final Meter meter = Metrics.newMeter(metricName,
                                                 annotation.eventType() == null ?
                                                         "requests" : annotation.eventType(),
                                                 annotation.rateUnit() == null ?
                                                         TimeUnit.SECONDS : annotation.rateUnit());
            dispatcher = new MeteredRequestDispatcher(dispatcher, meter);
        }

        if (method.getMethod().isAnnotationPresent(ExceptionMetered.class)) {
            final ExceptionMetered annotation = method.getMethod()
                                                      .getAnnotation(ExceptionMetered.class);

            final Class<?> klass = method.getDeclaringResource().getResourceClass();
            final String group = MetricName.chooseGroup(annotation.group(), klass);
            final String type = MetricName.chooseType(annotation.type(), klass);
            final String name = annotation.name() == null || annotation.name().equals("") ?
                    method.getMethod().getName() + ExceptionMetered.DEFAULT_NAME_SUFFIX : annotation
                    .name();
            final MetricName metricName = new MetricName(group, type, name);

            final Meter meter = Metrics.newMeter(metricName,
                                                 annotation.eventType() == null ?
                                                         "requests" : annotation.eventType(),
                                                 annotation.rateUnit() == null ?
                                                         TimeUnit.SECONDS : annotation.rateUnit());
            dispatcher = new ExceptionMeteredRequestDispatcher(dispatcher,
                                                               meter,
                                                               annotation.cause());
        }

        return dispatcher;
    }

}
