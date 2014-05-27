package com.codahale.metrics.jersey2;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;
import org.glassfish.jersey.server.model.ResourceMethod;
import org.glassfish.jersey.server.monitoring.ApplicationEvent;
import org.glassfish.jersey.server.monitoring.ApplicationEventListener;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;

import javax.ws.rs.ext.Provider;
import java.lang.reflect.Method;

import static com.codahale.metrics.MetricRegistry.name;

@Provider
public class MetricsApplicationEventListener implements ApplicationEventListener {
    private final MetricRegistry registry;

    public MetricsApplicationEventListener(MetricRegistry registry) {
        this.registry = registry;
    }

    /**
     * Process the application {@code event}. This method is called when new event occurs.
     *
     * @param event Application event.
     */
    @Override
    public void onEvent(ApplicationEvent event) {
        // NOP
    }

    /**
     * Process a new request and return a {@link org.glassfish.jersey.server.monitoring.RequestEventListener request event listener} if
     * listening to {@link org.glassfish.jersey.server.monitoring.RequestEvent request events} is required. The method is called once for
     * each new incoming request. If listening to the request is required then request event must be returned
     * from the method. Such a request event listener will receive all request events that one request. If listening
     * to request event for the request is not required then {@code null} must be returned
     * from the method (do not return empty mock listener in these
     * cases as it will have negative performance impact).
     *
     * @param requestEvent Event of type {@link org.glassfish.jersey.server.monitoring.RequestEvent.Type#START}.
     * @return Request event listener that will monitor the events of the request
     *         connected with {@code requestEvent}; null otherwise.
     */
    @Override
    public RequestEventListener onRequest(RequestEvent requestEvent) {
        switch (requestEvent.getType()) {
            case START:
                return new MetricsRequestEventListener(registry);
            default:
                return null;
        }
    }

    private static class MetricsRequestEventListener implements RequestEventListener {
        private final MetricRegistry registry;

        private volatile Timer.Context timerContext;

        private MetricsRequestEventListener(final MetricRegistry registry) {
            this.registry = registry;
        }

        @Override
        public void onEvent(RequestEvent event) {
            switch (event.getType()) {
                case REQUEST_MATCHED: {
                    final ResourceMethod resourceMethod = event.getUriInfo().getMatchedResourceMethod();

                    if (null != resourceMethod) {
                        final Method method = resourceMethod.getInvocable().getHandlingMethod();
                        final Timed timedAnnotation = method.getAnnotation(Timed.class);
                        final Metered meteredAnnotation = method.getAnnotation(Metered.class);
                        final Class<?> resourceClass = method.getDeclaringClass();

                        if (timedAnnotation != null) {
                            final String name = chooseName(timedAnnotation.name(),
                                    timedAnnotation.absolute(),
                                    resourceClass.getName(),
                                    method.getName());
                            timerContext = registry.timer(name).time();
                        }

                        if (meteredAnnotation != null) {
                            final String name = chooseName(meteredAnnotation.name(),
                                    meteredAnnotation.absolute(),
                                    resourceClass.getName(),
                                    method.getName());
                            registry.meter(name).mark();
                        }
                    }
                }
                break;
                case ON_EXCEPTION: {
                    final ResourceMethod resourceMethod = event.getUriInfo().getMatchedResourceMethod();

                    if (null != resourceMethod) {
                        final Method method = resourceMethod.getInvocable().getHandlingMethod();
                        final ExceptionMetered annotation = method.getAnnotation(ExceptionMetered.class);
                        final Class<?> resourceClass = method.getDeclaringClass();

                        if (annotation != null) {
                            final Class<? extends Throwable> exceptionClass = annotation.cause();
                            final String name = chooseName(annotation.name(),
                                    annotation.absolute(),
                                    resourceClass.getName(),
                                    method.getName(),
                                    ExceptionMetered.DEFAULT_NAME_SUFFIX);

                            final Throwable e = event.getException();
                            if (exceptionClass.isAssignableFrom(e.getClass()) ||
                                    (e.getCause() != null && exceptionClass.isAssignableFrom(e.getCause().getClass()))) {
                                registry.meter(name).mark();
                            }
                        }
                    }
                }
                break;
                case FINISHED:
                    final ResourceMethod resourceMethod = event.getUriInfo().getMatchedResourceMethod();

                    if (null != resourceMethod) {
                        final Method method = resourceMethod.getInvocable().getHandlingMethod();

                        if (method.isAnnotationPresent(Timed.class)) {
                            timerContext.stop();
                        }
                    }
                    break;
                default:
                    break;
            }
        }

        private String chooseName(final String explicitName, final boolean absolute,
                                  final String resourceClass, final String resourceMethod,
                                  final String... suffixes) {
            if (explicitName != null && !explicitName.isEmpty()) {
                if (absolute) {
                    return explicitName;
                }
                return name(resourceClass, explicitName);
            }
            return name(name(resourceClass, resourceMethod), suffixes);
        }
    }
}
