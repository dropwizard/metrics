package com.codahale.metrics.jersey2;

import com.codahale.metrics.Clock;
import com.codahale.metrics.ExponentiallyDecayingReservoir;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;
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
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

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

    private static final String[] REQUEST_FILTERING = {"request", "filtering"};
    private static final String[] RESPONSE_FILTERING = {"response", "filtering"};
    private static final String TOTAL = "total";

    private final MetricRegistry metrics;
    private ConcurrentMap<EventTypeAndMethod, Timer> timers = new ConcurrentHashMap<>();
    private ConcurrentMap<Method, Meter> meters = new ConcurrentHashMap<>();
    private ConcurrentMap<Method, ExceptionMeterMetric> exceptionMeters = new ConcurrentHashMap<>();

    private final Clock clock;
    private final boolean trackFilters;

    /**
     * Construct an application event listener using the given metrics registry.
     * <p/>
     * When using this constructor, the {@link InstrumentedResourceMethodApplicationListener}
     * should be added to a Jersey {@code ResourceConfig} as a singleton.
     *
     * @param metrics a {@link MetricRegistry}
     */
    public InstrumentedResourceMethodApplicationListener(final MetricRegistry metrics) {
        this(metrics, Clock.defaultClock(), false);
    }

    /**
     * Constructs a custom application listener.
     *
     * @param metrics      the metrics registry where the metrics will be stored
     * @param clock        the {@link Clock} to track time (used mostly in testing) in timers
     * @param trackFilters whether the processing time for request and response filters should be tracked
     */
    public InstrumentedResourceMethodApplicationListener(final MetricRegistry metrics, final Clock clock,
                                                         final boolean trackFilters) {
        this.metrics = metrics;
        this.clock = clock;
        this.trackFilters = trackFilters;
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
                                    final ResourceMethod method,
                                    final ExceptionMetered exceptionMetered) {
            final String name = chooseName(exceptionMetered.name(),
                    exceptionMetered.absolute(), method, ExceptionMetered.DEFAULT_NAME_SUFFIX);
            this.meter = registry.meter(name);
            this.cause = exceptionMetered.cause();
        }
    }

    private static class TimerRequestEventListener implements RequestEventListener {

        private final ConcurrentMap<EventTypeAndMethod, Timer> timers;
        private final Clock clock;
        private final long start;
        private Timer.Context resourceMethodStartContext;
        private Timer.Context requestMatchedContext;
        private Timer.Context responseFiltersStartContext;

        public TimerRequestEventListener(final ConcurrentMap<EventTypeAndMethod, Timer> timers, final Clock clock) {
            this.timers = timers;
            this.clock = clock;
            start = clock.getTick();
        }

        @Override
        public void onEvent(RequestEvent event) {
            switch (event.getType()) {
                case RESOURCE_METHOD_START:
                    resourceMethodStartContext = context(event);
                    break;
                case REQUEST_MATCHED:
                    requestMatchedContext = context(event);
                    break;
                case RESP_FILTERS_START:
                    responseFiltersStartContext = context(event);
                    break;
                case RESOURCE_METHOD_FINISHED:
                    if (resourceMethodStartContext != null) {
                        resourceMethodStartContext.close();
                    }
                    break;
                case REQUEST_FILTERED:
                    if (requestMatchedContext != null) {
                        requestMatchedContext.close();
                    }
                    break;
                case RESP_FILTERS_FINISHED:
                    if (responseFiltersStartContext != null) {
                        responseFiltersStartContext.close();
                    }
                    break;
                case FINISHED:
                    if (requestMatchedContext != null && responseFiltersStartContext != null) {
                        final Timer timer = timer(event);
                        if (timer != null) {
                            timer.update(clock.getTick() - start, TimeUnit.NANOSECONDS);
                        }
                    }
                    break;
                default:
                    break;
            }
        }

        private Timer timer(RequestEvent event) {
            final ResourceMethod resourceMethod = event.getUriInfo().getMatchedResourceMethod();
            if (resourceMethod == null) {
                return null;
            }
            return timers.get(new EventTypeAndMethod(event.getType(), resourceMethod.getInvocable().getDefinitionMethod()));
        }

        private Timer.Context context(RequestEvent event) {
            final Timer timer = timer(event);
            return timer != null ? timer.time() : null;
        }
    }

    private static class MeterRequestEventListener implements RequestEventListener {
        private final ConcurrentMap<Method, Meter> meters;

        public MeterRequestEventListener(final ConcurrentMap<Method, Meter> meters) {
            this.meters = meters;
        }

        @Override
        public void onEvent(RequestEvent event) {
            if (event.getType() == RequestEvent.Type.RESOURCE_METHOD_START) {
                final Meter meter = this.meters.get(event.getUriInfo().getMatchedResourceMethod().getInvocable().getDefinitionMethod());
                if (meter != null) {
                    meter.mark();
                }
            }
        }
    }

    private static class ExceptionMeterRequestEventListener implements RequestEventListener {
        private final ConcurrentMap<Method, ExceptionMeterMetric> exceptionMeters;

        public ExceptionMeterRequestEventListener(final ConcurrentMap<Method, ExceptionMeterMetric> exceptionMeters) {
            this.exceptionMeters = exceptionMeters;
        }

        @Override
        public void onEvent(RequestEvent event) {
            if (event.getType() == RequestEvent.Type.ON_EXCEPTION) {
                final ResourceMethod method = event.getUriInfo().getMatchedResourceMethod();
                final ExceptionMeterMetric metric = (method != null) ?
                        this.exceptionMeters.get(method.getInvocable().getDefinitionMethod()) : null;

                if (metric != null) {
                    if (metric.cause.isAssignableFrom(event.getException().getClass()) ||
                            (event.getException().getCause() != null &&
                                    metric.cause.isAssignableFrom(event.getException().getCause().getClass()))) {
                        metric.meter.mark();
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

            final Timed classLevelTimed = getClassLevelAnnotation(resource, Timed.class);
            final Metered classLevelMetered = getClassLevelAnnotation(resource, Metered.class);
            final ExceptionMetered classLevelExceptionMetered = getClassLevelAnnotation(resource, ExceptionMetered.class);

            for (final ResourceMethod method : resource.getAllMethods()) {
                registerTimedAnnotations(method, classLevelTimed);
                registerMeteredAnnotations(method, classLevelMetered);
                registerExceptionMeteredAnnotations(method, classLevelExceptionMetered);
            }

            for (final Resource childResource : resource.getChildResources()) {

                final Timed classLevelTimedChild = getClassLevelAnnotation(childResource, Timed.class);
                final Metered classLevelMeteredChild = getClassLevelAnnotation(childResource, Metered.class);
                final ExceptionMetered classLevelExceptionMeteredChild = getClassLevelAnnotation(childResource, ExceptionMetered.class);

                for (final ResourceMethod method : childResource.getAllMethods()) {
                    registerTimedAnnotations(method, classLevelTimedChild);
                    registerMeteredAnnotations(method, classLevelMeteredChild);
                    registerExceptionMeteredAnnotations(method, classLevelExceptionMeteredChild);
                }
            }
        }

    }

    @Override
    public RequestEventListener onRequest(final RequestEvent event) {
        final RequestEventListener listener = new ChainedRequestEventListener(
                new TimerRequestEventListener(timers, clock),
                new MeterRequestEventListener(meters),
                new ExceptionMeterRequestEventListener(exceptionMeters));

        return listener;
    }

    private <T extends Annotation> T getClassLevelAnnotation(final Resource resource, final Class<T> annotationClazz) {
        T annotation = null;

        for (final Class<?> clazz : resource.getHandlerClasses()) {
            annotation = clazz.getAnnotation(annotationClazz);

            if (annotation != null) {
                break;
            }
        }
        return annotation;
    }

    private void registerTimedAnnotations(final ResourceMethod method, final Timed classLevelTimed) {
        final Method definitionMethod = method.getInvocable().getDefinitionMethod();
        if (classLevelTimed != null) {
            registerTimers(method, definitionMethod, classLevelTimed);
            return;
        }

        final Timed annotation = definitionMethod.getAnnotation(Timed.class);
        if (annotation != null) {
            registerTimers(method, definitionMethod, annotation);
        }
    }

    private void registerTimers(ResourceMethod method, Method definitionMethod, Timed annotation) {
        timers.putIfAbsent(EventTypeAndMethod.requestMethodStart(definitionMethod), timerMetric(metrics, method, annotation));
        if (trackFilters) {
            timers.putIfAbsent(EventTypeAndMethod.requestMatched(definitionMethod), timerMetric(metrics, method, annotation, REQUEST_FILTERING));
            timers.putIfAbsent(EventTypeAndMethod.respFiltersStart(definitionMethod), timerMetric(metrics, method, annotation, RESPONSE_FILTERING));
            timers.putIfAbsent(EventTypeAndMethod.finished(definitionMethod), timerMetric(metrics, method, annotation, TOTAL));
        }
    }

    private void registerMeteredAnnotations(final ResourceMethod method, final Metered classLevelMetered) {
        final Method definitionMethod = method.getInvocable().getDefinitionMethod();

        if (classLevelMetered != null) {
            meters.putIfAbsent(definitionMethod, meterMetric(metrics, method, classLevelMetered));
            return;
        }
        final Metered annotation = definitionMethod.getAnnotation(Metered.class);

        if (annotation != null) {
            meters.putIfAbsent(definitionMethod, meterMetric(metrics, method, annotation));
        }
    }

    private void registerExceptionMeteredAnnotations(final ResourceMethod method, final ExceptionMetered classLevelExceptionMetered) {
        final Method definitionMethod = method.getInvocable().getDefinitionMethod();

        if (classLevelExceptionMetered != null) {
            exceptionMeters.putIfAbsent(definitionMethod, new ExceptionMeterMetric(metrics, method, classLevelExceptionMetered));
            return;
        }
        final ExceptionMetered annotation = definitionMethod.getAnnotation(ExceptionMetered.class);

        if (annotation != null) {
            exceptionMeters.putIfAbsent(definitionMethod, new ExceptionMeterMetric(metrics, method, annotation));
        }
    }

    private Timer timerMetric(final MetricRegistry registry,
                              final ResourceMethod method,
                              final Timed timed,
                              final String... suffixes) {
        final String name = chooseName(timed.name(), timed.absolute(), method, suffixes);
        return registry.timer(name, new MetricRegistry.MetricSupplier<Timer>() {
            @Override
            public Timer newMetric() {
                return new Timer(new ExponentiallyDecayingReservoir(), clock);
            }
        });
    }

    private static Meter meterMetric(final MetricRegistry registry,
                                     final ResourceMethod method,
                                     final Metered metered) {
        final String name = chooseName(metered.name(), metered.absolute(), method);
        return registry.meter(name);
    }

    protected static String chooseName(final String explicitName, final boolean absolute, final ResourceMethod method,
                                       final String... suffixes) {
        final Method definitionMethod = method.getInvocable().getDefinitionMethod();
        final String metricName;
        if (explicitName != null && !explicitName.isEmpty()) {
            metricName = absolute ? explicitName : name(definitionMethod.getDeclaringClass(), explicitName);
        } else {
            metricName = name(definitionMethod.getDeclaringClass(), definitionMethod.getName());
        }
        return name(metricName, suffixes);
    }

    private static class EventTypeAndMethod {

        private final RequestEvent.Type type;
        private final Method method;

        private EventTypeAndMethod(RequestEvent.Type type, Method method) {
            this.type = type;
            this.method = method;
        }

        static EventTypeAndMethod requestMethodStart(Method method) {
            return new EventTypeAndMethod(RequestEvent.Type.RESOURCE_METHOD_START, method);
        }

        static EventTypeAndMethod requestMatched(Method method) {
            return new EventTypeAndMethod(RequestEvent.Type.REQUEST_MATCHED, method);
        }

        static EventTypeAndMethod respFiltersStart(Method method) {
            return new EventTypeAndMethod(RequestEvent.Type.RESP_FILTERS_START, method);
        }

        static EventTypeAndMethod finished(Method method) {
            return new EventTypeAndMethod(RequestEvent.Type.FINISHED, method);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            EventTypeAndMethod that = (EventTypeAndMethod) o;

            if (type != that.type) {
                return false;
            }
            return method.equals(that.method);
        }

        @Override
        public int hashCode() {
            int result = type.hashCode();
            result = 31 * result + method.hashCode();
            return result;
        }
    }
}
