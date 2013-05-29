package com.codahale.metrics.jersey;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import com.sun.jersey.spi.container.ResourceMethodDispatchAdapter;
import com.sun.jersey.spi.container.ResourceMethodDispatchProvider;

import javax.ws.rs.ext.Provider;

/**
 * A provider that wraps a {@link ResourceMethodDispatchProvider} in an
 * {@link InstrumentedResourceMethodDispatchProvider}
 */
@Provider
public class InstrumentedResourceMethodDispatchAdapter implements ResourceMethodDispatchAdapter {
    private final MetricRegistry registry;

    /**
     * Construct a resource method dispatch adapter using the given metrics registry name.
     *
     * @param registryName the name of a shared metric registry
     */
    public InstrumentedResourceMethodDispatchAdapter(String registryName) {
        this(SharedMetricRegistries.getOrCreate(registryName));
    }

    /**
     * Construct a resource method dispatch adapter using the given metrics registry.
     * <p/>
     * When using this constructor, the {@link InstrumentedResourceMethodDispatchAdapter}
     * should be added to a Jersey {@code ResourceConfig} as a singleton.
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
