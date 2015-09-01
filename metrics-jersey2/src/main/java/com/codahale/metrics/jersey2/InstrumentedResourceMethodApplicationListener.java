package com.codahale.metrics.jersey2;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;
import org.glassfish.jersey.server.model.Invocable;
import org.glassfish.jersey.server.model.ModelProcessor;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.model.ResourceMethod;
import org.glassfish.jersey.server.model.ResourceModel;
import org.glassfish.jersey.server.monitoring.ApplicationEvent;
import org.glassfish.jersey.server.monitoring.ApplicationEventListener;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;

import javax.ws.rs.core.Configuration;
import javax.ws.rs.ext.Provider;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * An application event listener that listens for Jersey application initialization to
 * be finished, then creates a map of resource method that have metrics annotations.
 * <p/>
 * Finally, it listens for method start events, and returns a {@link RequestEventListener}
 * that updates the relevant metric for suitably annotated methods when it gets the
 * request events indicating that the method is about to be invoked, or just got done
 * being invoked.
 */
@Provider
public class InstrumentedResourceMethodApplicationListener implements ApplicationEventListener, ModelProcessor {

    private final MetricRegistry metrics;
    private final ConcurrentMap<String, Timer> timers = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Meter> meters = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, ExceptionMeterMetric> exceptionMeters = new ConcurrentHashMap<>();

    /**
     * Construct an application event listener using the given metrics registry.
     * <p/>
     * <p/>
     * When using this constructor, the {@link InstrumentedResourceMethodApplicationListener}
     * should be added to a Jersey {@code ResourceConfig} as a singleton.
     *
     * @param metrics a {@link MetricRegistry}
     */
    public InstrumentedResourceMethodApplicationListener(final MetricRegistry metrics) {
        this.metrics = metrics;
    }

    /**
     * A private class to maintain the metric for a method annotated with the
     * {@link ExceptionMetered} annotation, which needs to maintain both a meter
     * and a cause for which the meter should be updated.
     */
    private static class ExceptionMeterMetric {
        public final Meter meter;
        public final Class<? extends Throwable> cause;

        public ExceptionMeterMetric(final MetricRegistry registry,
                                    final Class<?> handlerClass,
                                    final Method definitionMethod,
                                    final ExceptionMetered exceptionMetered) {
            final String name = chooseName(exceptionMetered.name(),
                    exceptionMetered.absolute(), handlerClass, definitionMethod, ExceptionMetered.DEFAULT_NAME_SUFFIX);
            this.meter = registry.meter(name);
            this.cause = exceptionMetered.cause();
        }
    }

    private static class TimerRequestEventListener implements RequestEventListener {
        private final ConcurrentMap<String, Timer> timers;
        private Timer.Context context = null;

        public TimerRequestEventListener(final ConcurrentMap<String, Timer> timers) {
            this.timers = timers;
        }

        @Override
        public void onEvent(RequestEvent event) {
            if (event.getType() == RequestEvent.Type.RESOURCE_METHOD_START) {
                final ResourceMethod method = event.getUriInfo().getMatchedResourceMethod();
                final Invocable methodInvocable = method.getInvocable();
                final Method definitionMethod = methodInvocable.getDefinitionMethod();
                final Timed annotation = definitionMethod.getAnnotation(Timed.class);

                if (annotation != null) {
                    final Class<?> handlerClass = methodInvocable.getHandler().getHandlerClass();
                    final Timer timer = this.timers.get(chooseName(annotation.name(), annotation.absolute(), handlerClass, definitionMethod));
                    this.context = timer.time();
                }
            } else if (event.getType() == RequestEvent.Type.RESOURCE_METHOD_FINISHED) {
                if (this.context != null) {
                    this.context.close();
                }
            }
        }
    }

    private static class MeterRequestEventListener implements RequestEventListener {
        private final ConcurrentMap<String, Meter> meters;

        public MeterRequestEventListener(final ConcurrentMap<String, Meter> meters) {
            this.meters = meters;
        }

        @Override
        public void onEvent(RequestEvent event) {
            if (event.getType() == RequestEvent.Type.RESOURCE_METHOD_START) {
                final ResourceMethod method = event.getUriInfo().getMatchedResourceMethod();
                final Invocable methodInvocable = method.getInvocable();
                final Method definitionMethod = methodInvocable.getDefinitionMethod();
                final Metered annotation = definitionMethod.getAnnotation(Metered.class);

                if (annotation != null) {
                    final Class<?> handlerClass = methodInvocable.getHandler().getHandlerClass();
                    final Meter meter = this.meters.get(chooseName(annotation.name(), annotation.absolute(), handlerClass, definitionMethod));
                    meter.mark();
                }
            }
        }
    }

    private static class ExceptionMeterRequestEventListener implements RequestEventListener {
        private final ConcurrentMap<String, ExceptionMeterMetric> exceptionMeters;

        public ExceptionMeterRequestEventListener(final ConcurrentMap<String, ExceptionMeterMetric> exceptionMeters) {
            this.exceptionMeters = exceptionMeters;
        }

        @Override
        public void onEvent(RequestEvent event) {
            if (event.getType() == RequestEvent.Type.ON_EXCEPTION) {
                final ResourceMethod method = event.getUriInfo().getMatchedResourceMethod();

                if (method != null) {
                    final Invocable methodInvocable = method.getInvocable();
                    final Method definitionMethod = methodInvocable.getDefinitionMethod();
                    final ExceptionMetered annotation = definitionMethod.getAnnotation(ExceptionMetered.class);

                    if (annotation != null) {
                        final Class<?> handlerClass = methodInvocable.getHandler().getHandlerClass();
                        final ExceptionMeterMetric metric = this.exceptionMeters.get(chooseName(annotation.name(), annotation.absolute(), handlerClass, definitionMethod));

                        if (metric.cause.isAssignableFrom(event.getException().getClass()) ||
                                (event.getException().getCause() != null &&
                                        metric.cause.isAssignableFrom(event.getException().getCause().getClass()))) {
                            metric.meter.mark();
                        }
                    }
                }
            }
        }
    }

    private static class ChainedRequestEventListener implements RequestEventListener {
        private final RequestEventListener[] listeners;

        private ChainedRequestEventListener(final RequestEventListener... listeners) {
            this.listeners = listeners;
        }

        @Override
        public void onEvent(final RequestEvent event) {
            for (RequestEventListener listener : listeners) {
                listener.onEvent(event);
            }
        }
    }

    @Override
    public void onEvent(ApplicationEvent event) {
        if (event.getType() == ApplicationEvent.Type.INITIALIZATION_APP_FINISHED) {
            registerMetricsForModel(event.getResourceModel());
        }
    }

    @Override
    public ResourceModel processResourceModel(ResourceModel resourceModel, Configuration configuration) {
        return resourceModel;
    }

    @Override
    public ResourceModel processSubResource(ResourceModel subResourceModel, Configuration configuration) {
        registerMetricsForModel(subResourceModel);
        return subResourceModel;
    }

    private void registerMetricsForModel(ResourceModel resourceModel) {
        for (final Resource resource : resourceModel.getResources()) {
            for (final ResourceMethod method : resource.getAllMethods()) {
                registerTimedAnnotations(method);
                registerMeteredAnnotations(method);
                registerExceptionMeteredAnnotations(method);
            }

            for (final Resource childResource : resource.getChildResources()) {
                for (final ResourceMethod method : childResource.getAllMethods()) {
                    registerTimedAnnotations(method);
                    registerMeteredAnnotations(method);
                    registerExceptionMeteredAnnotations(method);
                }
            }
        }

    }

    @Override
    public RequestEventListener onRequest(final RequestEvent event) {
        final RequestEventListener listener = new ChainedRequestEventListener(
                new TimerRequestEventListener(timers),
                new MeterRequestEventListener(meters),
                new ExceptionMeterRequestEventListener(exceptionMeters));

        return listener;
    }

    private void registerTimedAnnotations(final ResourceMethod method) {
        final Invocable methodInvocable = method.getInvocable();
        final Method definitionMethod = methodInvocable.getDefinitionMethod();
        final Timed annotation = definitionMethod.getAnnotation(Timed.class);

        if (annotation != null) {
            final Class<?> handlerClass = methodInvocable.getHandler().getHandlerClass();
            String methodName = chooseName(annotation.name(), annotation.absolute(), handlerClass, definitionMethod);
            timers.putIfAbsent(methodName, this.metrics.timer(methodName));
        }
    }

    private void registerMeteredAnnotations(final ResourceMethod method) {
        final Invocable methodInvocable = method.getInvocable();
        final Method definitionMethod = methodInvocable.getDefinitionMethod();
        final Metered annotation = definitionMethod.getAnnotation(Metered.class);

        if (annotation != null) {
            final Class<?> handlerClass = methodInvocable.getHandler().getHandlerClass();
            String methodName = chooseName(annotation.name(), annotation.absolute(), handlerClass, definitionMethod);
            meters.putIfAbsent(methodName, this.metrics.meter(methodName));
        }
    }

    private void registerExceptionMeteredAnnotations(final ResourceMethod method) {
        final Invocable methodInvocable = method.getInvocable();
        final Method definitionMethod = methodInvocable.getDefinitionMethod();
        final ExceptionMetered annotation = definitionMethod.getAnnotation(ExceptionMetered.class);

        if (annotation != null) {
            final Class<?> handlerClass = methodInvocable.getHandler().getHandlerClass();
            String methodName = chooseName(annotation.name(), annotation.absolute(), handlerClass, definitionMethod);
            exceptionMeters.putIfAbsent(methodName, new ExceptionMeterMetric(metrics, handlerClass, definitionMethod, annotation));
        }
    }

    protected static String chooseName(final String explicitName, final boolean absolute, final Class<?> handlerClass,
                                     final Method definitionMethod, final String... suffixes) {
        if (explicitName != null && !explicitName.isEmpty()) {
            if (absolute) {
                return explicitName;
            }
            return name(handlerClass, explicitName);
        }

        return name(name(handlerClass, definitionMethod.getName()), suffixes);
    }
}
