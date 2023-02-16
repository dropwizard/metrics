package com.codahale.metrics.jersey2;

import com.codahale.metrics.Clock;
import com.codahale.metrics.ExponentiallyDecayingReservoir;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Reservoir;
import com.codahale.metrics.Timer;
import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.ResponseMetered;
import com.codahale.metrics.annotation.ResponseMeteredLevel;
import com.codahale.metrics.annotation.Timed;
import org.glassfish.jersey.server.ContainerResponse;
import org.glassfish.jersey.server.model.*;
import org.glassfish.jersey.server.monitoring.ApplicationEvent;
import org.glassfish.jersey.server.monitoring.ApplicationEventListener;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;

import javax.ws.rs.core.Configuration;
import javax.ws.rs.ext.Provider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.EnumSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static com.codahale.metrics.MetricRegistry.name;
import static com.codahale.metrics.annotation.ResponseMeteredLevel.COARSE;
import static com.codahale.metrics.annotation.ResponseMeteredLevel.DETAILED;
import static com.codahale.metrics.annotation.ResponseMeteredLevel.ALL;

/**
 * An application event listener that listens for Jersey application initialization to
 * be finished, then creates a map of resource method that have metrics annotations.
 * <p>
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
    private final ConcurrentMap<EventTypeAndMethod, Timer> timers = new ConcurrentHashMap<>();
    private final ConcurrentMap<Method, Meter> meters = new ConcurrentHashMap<>();
    private final ConcurrentMap<Method, ExceptionMeterMetric> exceptionMeters = new ConcurrentHashMap<>();
    private final ConcurrentMap<Method, ResponseMeterMetric> responseMeters = new ConcurrentHashMap<>();

    private final Clock clock;
    private final boolean trackFilters;
    private final Supplier<Reservoir> reservoirSupplier;

    /**
     * Construct an application event listener using the given metrics registry.
     * <p>
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
        this(metrics, clock, trackFilters, ExponentiallyDecayingReservoir::new);
    }

    /**
     * Constructs a custom application listener.
     *
     * @param metrics           the metrics registry where the metrics will be stored
     * @param clock             the {@link Clock} to track time (used mostly in testing) in timers
     * @param trackFilters      whether the processing time for request and response filters should be tracked
     * @param reservoirSupplier Supplier for creating the {@link Reservoir} for {@link Timer timers}.
     */
    public InstrumentedResourceMethodApplicationListener(final MetricRegistry metrics, final Clock clock,
                                                         final boolean trackFilters,
                                                         final Supplier<Reservoir> reservoirSupplier) {
        this.metrics = metrics;
        this.clock = clock;
        this.trackFilters = trackFilters;
        this.reservoirSupplier = reservoirSupplier;
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
                    exceptionMetered.absolute(), method, ExceptionMetered.class, ExceptionMetered.DEFAULT_NAME_SUFFIX);
            this.meter = registry.meter(name);
            this.cause = exceptionMetered.cause();
        }
    }

    /**
     * A private class to maintain the metrics for a method annotated with the
     * {@link ResponseMetered} annotation, which needs to maintain meters for
     * different response codes
     */
    private static class ResponseMeterMetric {
        private static final Set<ResponseMeteredLevel> COARSE_METER_LEVELS = EnumSet.of(COARSE, ALL);
        private static final Set<ResponseMeteredLevel> DETAILED_METER_LEVELS = EnumSet.of(DETAILED, ALL);
        private final List<Meter> meters;
        private final Map<Integer, Meter> responseCodeMeters;
        private final MetricRegistry metricRegistry;
        private final String metricName;
        private final ResponseMeteredLevel level;

        public ResponseMeterMetric(final MetricRegistry registry,
                                   final ResourceMethod method,
                                   final ResponseMetered responseMetered) {
            this.metricName = chooseName(responseMetered.name(), responseMetered.absolute(), method, ResponseMetered.class);
            this.level = responseMetered.level();
            this.meters = COARSE_METER_LEVELS.contains(level) ?
                    Collections.unmodifiableList(Arrays.asList(
                    registry.meter(name(metricName, "1xx-responses")), // 1xx
                    registry.meter(name(metricName, "2xx-responses")), // 2xx
                    registry.meter(name(metricName, "3xx-responses")), // 3xx
                    registry.meter(name(metricName, "4xx-responses")), // 4xx
                    registry.meter(name(metricName, "5xx-responses"))  // 5xx
            )) : Collections.emptyList();
            this.responseCodeMeters = DETAILED_METER_LEVELS.contains(level) ? new ConcurrentHashMap<>() : Collections.emptyMap();
            this.metricRegistry = registry;
        }

        public void mark(int statusCode) {
            if (DETAILED_METER_LEVELS.contains(level)) {
                getResponseCodeMeter(statusCode).mark();
            }

            if (COARSE_METER_LEVELS.contains(level)) {
                final int responseStatus = statusCode / 100;
                if (responseStatus >= 1 && responseStatus <= 5) {
                    meters.get(responseStatus - 1).mark();
                }
            }
        }

        private Meter getResponseCodeMeter(int statusCode) {
            return responseCodeMeters
                    .computeIfAbsent(statusCode, sc -> metricRegistry
                            .meter(name(metricName, String.format("%d-responses", sc))));
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
            final MethodAnnotation methodAnnotation = getMethodAnnotation(event, Timed.class);

            if (methodAnnotation == null) {
                return null;
            }

            return timers.get(new EventTypeAndMethod(
                event.getType(),
                methodAnnotation.getMethod()
            ));
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
                final MethodAnnotation methodAnnotation = getMethodAnnotation(event, Metered.class);

                if (null != methodAnnotation) {
                    final Meter meter = this.meters.get(
                        methodAnnotation.getMethod()
                    );

                    if (meter != null) {
                        meter.mark();
                    }
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
                final ResourceMethod   method           = event.getUriInfo().getMatchedResourceMethod();
                final MethodAnnotation methodAnnotation = getMethodAnnotation(method, ExceptionMetered.class);

                if (null != methodAnnotation) {
                    final ExceptionMeterMetric metric =
                        (method != null)
                        ? this.exceptionMeters.get(
                            methodAnnotation.getMethod()
                        )
                        : null;

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
    }

    private static class ResponseMeterRequestEventListener implements RequestEventListener {
        private final ConcurrentMap<Method, ResponseMeterMetric> responseMeters;

        public ResponseMeterRequestEventListener(final ConcurrentMap<Method, ResponseMeterMetric> responseMeters) {
            this.responseMeters = responseMeters;
        }

        @Override
        public void onEvent(RequestEvent event) {
            if (event.getType() == RequestEvent.Type.FINISHED) {
                final ResourceMethod   method           = event.getUriInfo().getMatchedResourceMethod();
                final MethodAnnotation methodAnnotation = getMethodAnnotation(method, ResponseMetered.class);

                if (null != methodAnnotation) {
                    final ResponseMeterMetric metric =
                          (method != null)
                        ? this.responseMeters.get(
                            methodAnnotation.getMethod()
                        )
                        : null;

                    if (metric != null) {
                        ContainerResponse containerResponse = event.getContainerResponse();
                        if (containerResponse == null && event.getException() != null) {
                            metric.mark(500);
                        } else {
                            metric.mark(containerResponse.getStatus());
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

    private static class MethodAnnotation<A extends Annotation> {

        private Method method;
        private A      annotation;

        public MethodAnnotation(Method method, A annotation) {
            this.method     = method;
            this.annotation = annotation;
        }

        public Method getMethod() {
            return method;
        }

        public A getAnnotation() {
            return annotation;
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
            final ResponseMetered classLevelResponseMetered = getClassLevelAnnotation(resource, ResponseMetered.class);

            for (final ResourceMethod method : resource.getAllMethods()) {
                registerTimedAnnotations(method, classLevelTimed);
                registerMeteredAnnotations(method, classLevelMetered);
                registerExceptionMeteredAnnotations(method, classLevelExceptionMetered);
                registerResponseMeteredAnnotations(method, classLevelResponseMetered);
            }

            for (final Resource childResource : resource.getChildResources()) {

                final Timed classLevelTimedChild = getClassLevelAnnotation(childResource, Timed.class);
                final Metered classLevelMeteredChild = getClassLevelAnnotation(childResource, Metered.class);
                final ExceptionMetered classLevelExceptionMeteredChild = getClassLevelAnnotation(childResource, ExceptionMetered.class);
                final ResponseMetered classLevelResponseMeteredChild = getClassLevelAnnotation(childResource, ResponseMetered.class);

                for (final ResourceMethod method : childResource.getAllMethods()) {
                    registerTimedAnnotations(method, classLevelTimedChild);
                    registerMeteredAnnotations(method, classLevelMeteredChild);
                    registerExceptionMeteredAnnotations(method, classLevelExceptionMeteredChild);
                    registerResponseMeteredAnnotations(method, classLevelResponseMeteredChild);
                }
            }
        }
    }

    @Override
    public RequestEventListener onRequest(final RequestEvent event) {
        final RequestEventListener listener = new ChainedRequestEventListener(
                new TimerRequestEventListener(timers, clock),
                new MeterRequestEventListener(meters),
                new ExceptionMeterRequestEventListener(exceptionMeters),
                new ResponseMeterRequestEventListener(responseMeters));

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

    private static <T extends Annotation> MethodAnnotation<T> getMethodAnnotation(Invocable invocable, Class<T> annotationClass) {
        Method method     = invocable.getHandlingMethod();
        T      annotation = method.getAnnotation(annotationClass);

        if (null == annotation) {
            method     = invocable.getDefinitionMethod();
            annotation = method.getAnnotation(annotationClass);
        }

        if (null == annotation) {
            return null;
        }

        return new MethodAnnotation(method, annotation);
    }

    private static <T extends Annotation> MethodAnnotation<T> getMethodAnnotation(ResourceMethod resourceMethod, Class<T> annotationClass) {
        return getMethodAnnotation(
            resourceMethod.getInvocable(),
            annotationClass
        );
    }

    private static <T extends Annotation> MethodAnnotation<T> getMethodAnnotation(RequestEvent requestEvent, Class<T> annotationClass) {
        return getMethodAnnotation(
            requestEvent.getUriInfo().getMatchedResourceMethod(),
            annotationClass
        );
    }

    private void registerTimedAnnotations(final ResourceMethod method, final Timed classLevelTimed) {
        final MethodAnnotation<Timed> methodAnnotation = getMethodAnnotation(method, Timed.class);

        if (null == methodAnnotation) {
            return;
        }

        if (classLevelTimed != null) {
            registerTimers(
                method,
                methodAnnotation.getMethod(),
                classLevelTimed
            );

            return;
        }

        registerTimers(
            method,
            methodAnnotation.getMethod(),
            methodAnnotation.getAnnotation()
        );
    }

    private void registerTimers(ResourceMethod method, Method handlingMethod, Timed annotation) {
        timers.putIfAbsent(EventTypeAndMethod.requestMethodStart(handlingMethod), timerMetric(metrics, method, annotation));
        if (trackFilters) {
            timers.putIfAbsent(EventTypeAndMethod.requestMatched(handlingMethod), timerMetric(metrics, method, annotation, REQUEST_FILTERING));
            timers.putIfAbsent(EventTypeAndMethod.respFiltersStart(handlingMethod), timerMetric(metrics, method, annotation, RESPONSE_FILTERING));
            timers.putIfAbsent(EventTypeAndMethod.finished(handlingMethod), timerMetric(metrics, method, annotation, TOTAL));
        }
    }

    private void registerMeteredAnnotations(final ResourceMethod method, final Metered classLevelMetered) {
        final MethodAnnotation<Metered> methodAnnotation = getMethodAnnotation(method, Metered.class);

        if (null == methodAnnotation) {
            return;
        }

        if (classLevelMetered != null) {
            meters.putIfAbsent(
                methodAnnotation.getMethod(),
                meterMetric(metrics, method, classLevelMetered)
            );

            return;
        }

        meters.putIfAbsent(
            methodAnnotation.getMethod(),
            meterMetric(
                metrics,
                method,
                methodAnnotation.getAnnotation()
            )
        );
    }

    private void registerExceptionMeteredAnnotations(final ResourceMethod method, final ExceptionMetered classLevelExceptionMetered) {
        final MethodAnnotation<ExceptionMetered> methodAnnotation = getMethodAnnotation(method, ExceptionMetered.class);

        if (null == methodAnnotation) {
            return;
        }

        if (classLevelExceptionMetered != null) {
            exceptionMeters.putIfAbsent(
                methodAnnotation.getMethod(),
                new ExceptionMeterMetric(metrics, method, classLevelExceptionMetered)
            );

            return;
        }

        exceptionMeters.putIfAbsent(
            methodAnnotation.getMethod(),
            new ExceptionMeterMetric(
                metrics,
                method,
                methodAnnotation.getAnnotation()
            )
        );
    }

    private void registerResponseMeteredAnnotations(final ResourceMethod method, final ResponseMetered classLevelResponseMetered) {
        final MethodAnnotation<ResponseMetered> methodAnnotation = getMethodAnnotation(method, ResponseMetered.class);

        if (null == methodAnnotation) {
            return;
        }

        if (classLevelResponseMetered != null) {
            responseMeters.putIfAbsent(
                methodAnnotation.getMethod(),
                new ResponseMeterMetric(metrics, method, classLevelResponseMetered)
            );

            return;
        }

        responseMeters.putIfAbsent(
            methodAnnotation.getMethod(),
            new ResponseMeterMetric(
                metrics,
                method,
                methodAnnotation.getAnnotation()
            )
        );
    }

    private Timer timerMetric(final MetricRegistry registry,
                              final ResourceMethod method,
                              final Timed timed,
                              final String... suffixes) {
        final String name = chooseName(timed.name(), timed.absolute(), method, Timed.class, suffixes);
        return registry.timer(name, () -> new Timer(reservoirSupplier.get(), clock));
    }

    private Meter meterMetric(final MetricRegistry registry,
                              final ResourceMethod method,
                              final Metered metered) {
        final String name = chooseName(metered.name(), metered.absolute(), method, Metered.class);
        return registry.meter(name, () -> new Meter(clock));
    }

    protected static <T extends Annotation> String chooseName(
        final String explicitName,
        final boolean absolute,
        final ResourceMethod method,
        final Class<T> annotationClass,
        final String... suffixes
    ) {
        final MethodAnnotation methodAnnotation = getMethodAnnotation(method, annotationClass);

        if (null == methodAnnotation) {
            return null;
        }

        Method gotMethod = methodAnnotation.getMethod();

        final String metricName;
        if (explicitName != null && !explicitName.isEmpty()) {
            metricName = absolute ? explicitName : name(
                gotMethod.getDeclaringClass(),
                explicitName
            );
        } else {
            metricName = name(
                gotMethod.getDeclaringClass(),
                gotMethod.getName()
            );
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
