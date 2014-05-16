package com.codahale.metrics.jersey2;

import static com.codahale.metrics.MetricRegistry.name;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.ext.Provider;

import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.model.ResourceMethod;
import org.glassfish.jersey.server.monitoring.ApplicationEvent;
import org.glassfish.jersey.server.monitoring.ApplicationEventListener;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;

/**
 * An application event listener that listens for Jersey application initialization to
 * be finished, then creates a map of resource method that have metrics annotations.
 *
 * Finally, it listens for method start events, and returns a {@link RequestEventListener}
 * that updates the relevant metric for suitably annotated methods when it gets the
 * request events indicating that the method is about to be invoked, or just got done
 * being invoked.
 */

@Provider
public class InstrumentedResourceMethodApplicationListener implements
        ApplicationEventListener {
    
    private MetricRegistry metrics;

    /**
     * Construct an application event listener using the given metrics registry.
     *
     * <p/>
     * When using this constructor, the {@link InstrumentedResourceMethodApplicationListener}
     * should be added to a Jersey {@code ResourceConfig} as a singleton.
     *
     * @param metrics a {@link MetricRegistry}
     */
    public InstrumentedResourceMethodApplicationListener (MetricRegistry metrics) {
        this.metrics = metrics;
    }

    /**
     * A private class to maintain the metric for a method annotated with the 
     * {@link ExceptionMetered} annotation, which needs to maintain both a meter
     * and a cause for which the meter should be updated.
     *
     */
    private static class ExceptionMeterMetric {
        public final Meter meter;
        public final Class<? extends Throwable> cause;
        
        public ExceptionMeterMetric (MetricRegistry registry,
                                     ResourceMethod method,
                                     ExceptionMetered exceptionMetered) {
            final String name = chooseName(exceptionMetered.name(), 
                    exceptionMetered.absolute(), method, 
                    ExceptionMetered.DEFAULT_NAME_SUFFIX);
            this.meter = registry.meter(name);
            this.cause = exceptionMetered.cause();
        }
    }
    
    private static class TimerRequestEventListener implements RequestEventListener
    {
        private final Map<Method,Timer> timerMap;
        private final RequestEventListener other;
        private Timer.Context context = null;
        
        public TimerRequestEventListener (Map<Method,Timer> timerMap,
                                          RequestEventListener other)
        {
            this.timerMap = timerMap;
            this.other = other;
        }

        @Override
        public void onEvent(RequestEvent event) {
            if (this.other != null)
                this.other.onEvent(event);
            if (event.getType() == RequestEvent.Type.RESOURCE_METHOD_START)
            {
                Timer timer = this.timerMap.get(event.getUriInfo()
                        .getMatchedResourceMethod().getInvocable().getDefinitionMethod());
                if (timer != null)
                    this.context = timer.time();
            }
            else if (event.getType() == RequestEvent.Type.RESOURCE_METHOD_FINISHED)
            {
                if (this.context != null)
                    this.context.close();
            }
        }
    }
    
    private static class MeterRequestEventListener implements RequestEventListener
    {
        private final Map<Method,Meter> meterMap;
        private final RequestEventListener other;
        
        public MeterRequestEventListener (Map<Method,Meter> meterMap,
                                          RequestEventListener other)
        {
            this.meterMap = meterMap;
            this.other = other;
        }

        @Override
        public void onEvent(RequestEvent event) {
            if (this.other != null)
                this.other.onEvent(event);
            if (event.getType() == RequestEvent.Type.RESOURCE_METHOD_START)
            {
                Meter meter = this.meterMap.get(event.getUriInfo()
                        .getMatchedResourceMethod().getInvocable().getDefinitionMethod());
                if (meter != null)
                    meter.mark();
            }
        }        
    }
    
    private static class ExceptionMeterRequestEventListener implements RequestEventListener
    {
        private final Map<Method,ExceptionMeterMetric> exceptionMeterMap;
        private final RequestEventListener other;
        
        public ExceptionMeterRequestEventListener (Map<Method,ExceptionMeterMetric> exceptionMeterMap,
                                                   RequestEventListener other)
        {
            this.exceptionMeterMap = exceptionMeterMap;
            this.other = other;
        }

        @Override
        public void onEvent(RequestEvent event) {
            if (this.other != null)
                this.other.onEvent(event);
            
            if (event.getType() == RequestEvent.Type.ON_EXCEPTION)
            {
                ResourceMethod method = event.getUriInfo().getMatchedResourceMethod();
                
                ExceptionMeterMetric metric = (method != null ?
                            this.exceptionMeterMap.get(method.getInvocable().getDefinitionMethod()) :
                            null);
                if (metric != null)
                {
                    if (metric.cause.isAssignableFrom(event.getException().getClass()) ||
                            (event.getException().getCause() != null && 
                            metric.cause.isAssignableFrom(event.getException().getCause().getClass()))) {
                        metric.meter.mark();
                    }
                }
            }
        }
        
    }


    private Map<Method,Timer> timerMap = new HashMap<Method,Timer>();
    private Map<Method,Meter> meterMap = new HashMap<Method,Meter>();
    private Map<Method,ExceptionMeterMetric> exceptionMeterMap = 
            new HashMap<Method,ExceptionMeterMetric>();

    @Override
    public void onEvent(ApplicationEvent event) {
        if (event.getType() == ApplicationEvent.Type.INITIALIZATION_APP_FINISHED)
        {
            for (Resource resource : event.getResourceModel().getResources())
            {
                for (ResourceMethod method : resource.getAllMethods())
                {
                    registerMetricsAnnotations (method);
                }
                
                for (Resource childResource : resource.getChildResources())
                {
                    for (ResourceMethod method : childResource.getAllMethods())
                    {
                        registerMetricsAnnotations (method);
                    }
                }
            }
        }
    }

    @Override
    public RequestEventListener onRequest(RequestEvent event) {
        RequestEventListener listener = new TimerRequestEventListener (timerMap, null);
        listener = new MeterRequestEventListener (meterMap, listener);
        listener = new ExceptionMeterRequestEventListener (exceptionMeterMap, listener);

        return listener;
    }
    
    private void registerMetricsAnnotations (ResourceMethod method)
    {
        Metered meteredAnnotation = method.getInvocable().getDefinitionMethod().getAnnotation(Metered.class);
        Timed timedAnnotation = method.getInvocable().getDefinitionMethod().getAnnotation(Timed.class);
        ExceptionMetered exceptionMeteredAnnotation = method.getInvocable().getDefinitionMethod().getAnnotation(ExceptionMetered.class);
        
        if (meteredAnnotation != null)
        {
            meterMap.put(method.getInvocable().getDefinitionMethod(), 
                    meterMetric(this.metrics, method, meteredAnnotation));
        }
        
        if (timedAnnotation != null)
        {
            timerMap.put(method.getInvocable().getDefinitionMethod(),
                    timerMetric(this.metrics, method, timedAnnotation));
        }
        
        if (exceptionMeteredAnnotation != null)
        {
            exceptionMeterMap.put(method.getInvocable().getDefinitionMethod(), 
                    new ExceptionMeterMetric(this.metrics, method, exceptionMeteredAnnotation));
        }
    }
    
    private static Timer timerMetric (MetricRegistry registry,
                                      ResourceMethod method,
                                      Timed timed) {
        final String name = chooseName(timed.name(), timed.absolute(), method);
        return registry.timer(name);
    }
    
    private static Meter meterMetric (MetricRegistry registry,
                                      ResourceMethod method,
                                      Metered metered)
    {
        final String name = chooseName(metered.name(), metered.absolute(), method);
        return registry.meter(name);
    }
    
    protected static String chooseName(String explicitName, boolean absolute, ResourceMethod method, String... suffixes) {
        if (explicitName != null && !explicitName.isEmpty()) {
            if (absolute) {
                return explicitName;
            }
            return name(method.getInvocable().getDefinitionMethod().getDeclaringClass(), explicitName);
        }

        return name(name(method.getInvocable().getDefinitionMethod().getDeclaringClass(),
                         method.getInvocable().getDefinitionMethod().getName()),
                    suffixes);
    }
}
