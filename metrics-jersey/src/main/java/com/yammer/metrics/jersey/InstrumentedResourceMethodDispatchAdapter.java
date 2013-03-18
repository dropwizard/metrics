package com.yammer.metrics.jersey;

import com.sun.jersey.spi.container.ResourceMethodDispatchAdapter;
import com.sun.jersey.spi.container.ResourceMethodDispatchProvider;
import com.yammer.metrics.MetricRegistry;

import javax.ws.rs.ext.Provider;

/**
 * A provider that wraps a {@link ResourceMethodDispatchProvider} in an
 * {@link InstrumentedResourceMethodDispatchProvider}
 */
@Provider
public class InstrumentedResourceMethodDispatchAdapter implements ResourceMethodDispatchAdapter {
    // TODO: 3/10/13 <coda> -- figure out how to coordinate on registry names

    private final MetricRegistry registry;

    /**
     * Construct a resource method dispatch adapter using the given
     * metrics registry
     * <p />
     * When using this constructor, the {@link InstrumentedResourceMethodDispatchAdapter}
     * should be added to a Jersey {@code ResourceConfig} as a singleton
     *
     * @param registry a {@link MetricRegistry}
     */
    public InstrumentedResourceMethodDispatchAdapter(MetricRegistry registry) {
        this.registry = registry;
    }


    @Override
    public ResourceMethodDispatchProvider adapt(ResourceMethodDispatchProvider provider) {
        return new InstrumentedResourceMethodDispatchProvider(provider, registry);
    }
}
