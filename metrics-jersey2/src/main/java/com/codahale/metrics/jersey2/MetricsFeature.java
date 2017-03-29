package com.codahale.metrics.jersey2;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;

import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

/**
 * A {@link Feature} which registers a {@link InstrumentedResourceMethodApplicationListener}
 * for recording request events.
 */
public class MetricsFeature implements Feature {

    private final MetricRegistry registry;
    private final InstrumentedResourceMethodApplicationListener.ClockProvider clockProvider;

    public MetricsFeature(MetricRegistry registry) {
        this(registry, null);
    }

    public MetricsFeature(MetricRegistry registry, InstrumentedResourceMethodApplicationListener.ClockProvider clockProvider) {
        this.registry = registry;
        this.clockProvider = clockProvider;
    }

    public MetricsFeature(String registryName) {
        this(SharedMetricRegistries.getOrCreate(registryName));
    }

    /**
     * A call-back method called when the feature is to be enabled in a given
     * runtime configuration scope.
     * <p/>
     * The responsibility of the feature is to properly update the supplied runtime configuration context
     * and return {@code true} if the feature was successfully enabled or {@code false} otherwise.
     * <p>
     * Note that under some circumstances the feature may decide not to enable itself, which
     * is indicated by returning {@code false}. In such case the configuration context does
     * not add the feature to the collection of enabled features and a subsequent call to
     * {@link javax.ws.rs.core.Configuration#isEnabled(javax.ws.rs.core.Feature)} or
     * {@link javax.ws.rs.core.Configuration#isEnabled(Class)} method
     * would return {@code false}.
     * </p>
     *
     * @param context configurable context in which the feature should be enabled.
     * @return {@code true} if the feature was successfully enabled, {@code false}
     * otherwise.
     */
    @Override
    public boolean configure(FeatureContext context) {
        context.register(new InstrumentedResourceMethodApplicationListener(registry, clockProvider));
        return true;
    }
}
