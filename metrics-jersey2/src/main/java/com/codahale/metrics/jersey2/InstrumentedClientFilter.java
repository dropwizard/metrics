package com.codahale.metrics.jersey2;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import java.io.IOException;

public class InstrumentedClientFilter implements ClientRequestFilter, ClientResponseFilter {

    private final MetricRegistry registry;
    private final Jersey2MetricNameStrategy metricNameStrategy;
    private final String name;

    private static final String CONTEXT_PROPERTY_NAME = "InstrumentedClientFilter.timerContext";

    public InstrumentedClientFilter(MetricRegistry registry, Jersey2MetricNameStrategy metricNameStrategy) {
        this(registry, metricNameStrategy, null);
    }

    public InstrumentedClientFilter(MetricRegistry registry, Jersey2MetricNameStrategy metricNameStrategy, String name) {
        this.registry = registry;
        this.metricNameStrategy = metricNameStrategy;
        this.name = name;
    }

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        final Timer.Context timerContext = timer(requestContext).time();
        requestContext.setProperty(CONTEXT_PROPERTY_NAME, timerContext);
    }

    @Override
    public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {
        Object timer = requestContext.getProperty(CONTEXT_PROPERTY_NAME);
        if (timer != null) {
            final Timer.Context timerContext = (Timer.Context)timer;
            timerContext.stop();
            requestContext.removeProperty(CONTEXT_PROPERTY_NAME);
        }
    }

    private Timer timer(ClientRequestContext context) {
        return registry.timer(metricNameStrategy.getNameFor(name, context));
    }

}
