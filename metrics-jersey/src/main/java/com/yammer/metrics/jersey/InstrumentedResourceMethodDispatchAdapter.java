package com.yammer.metrics.jersey;

import com.sun.jersey.spi.container.ResourceMethodDispatchAdapter;
import com.sun.jersey.spi.container.ResourceMethodDispatchProvider;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.MetricsRegistry;

import javax.ws.rs.ext.Provider;

/**
 * A provider that wraps a {@link ResourceMethodDispatchProvider} in an
 * {@link InstrumentedResourceMethodDispatchProvider}
 */
@Provider
public class InstrumentedResourceMethodDispatchAdapter implements ResourceMethodDispatchAdapter {
    private MetricsRegistry registry;

    /**
     * Construct a resource method dispatch adapter using the default
     * metrics registry
     *
     */
    public InstrumentedResourceMethodDispatchAdapter() {
        this(Metrics.defaultRegistry());
    }

    /**
     * Construct a resource method dispatch adapter using the given
     * metrics registry
     * <p />
     * When using this constructor, the {@link InstrumentedResourceMethodDispatchAdapter}
     * should be added to a Jersey {@code ResourceConfig} as a singleton
     *
     * @param registry a {@link MetricsRegistry}
     */
    public InstrumentedResourceMethodDispatchAdapter( MetricsRegistry registry ) {
        this.registry = registry;
    }


    @Override
    public ResourceMethodDispatchProvider adapt(ResourceMethodDispatchProvider provider) {
        return new InstrumentedResourceMethodDispatchProvider(provider, registry);
    }
}
