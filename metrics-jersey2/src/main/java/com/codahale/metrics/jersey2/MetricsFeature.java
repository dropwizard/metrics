package com.codahale.metrics.jersey2;

import com.codahale.metrics.Clock;
import com.codahale.metrics.ExponentiallyDecayingReservoir;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Reservoir;
import com.codahale.metrics.SharedMetricRegistries;

import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import java.util.function.Supplier;

/**
 * A {@link Feature} which registers a {@link InstrumentedResourceMethodApplicationListener}
 * for recording request events.
 */
public class MetricsFeature implements Feature {

    private final MetricRegistry registry;
    private final Clock clock;
    private final boolean trackFilters;
    private final Supplier<Reservoir> reservoirSupplier;

    /*
     * @param registry          the metrics registry where the metrics will be stored
     */
    public MetricsFeature(MetricRegistry registry) {
        this(registry, Clock.defaultClock());
    }

    /*
     * @param registry          the metrics registry where the metrics will be stored
     * @param reservoirSupplier Supplier for creating the {@link Reservoir} for {@link Timer timers}.
     */
    public MetricsFeature(MetricRegistry registry, Supplier<Reservoir> reservoirSupplier) {
        this(registry, Clock.defaultClock(), false, reservoirSupplier);
    }

    /*
     * @param registry          the metrics registry where the metrics will be stored
     * @param clock             the {@link Clock} to track time (used mostly in testing) in timers
     */
    public MetricsFeature(MetricRegistry registry, Clock clock) {
        this(registry, clock, false);
    }

    /*
     * @param registry          the metrics registry where the metrics will be stored
     * @param clock             the {@link Clock} to track time (used mostly in testing) in timers
     * @param trackFilters      whether the processing time for request and response filters should be tracked
     */
    public MetricsFeature(MetricRegistry registry, Clock clock, boolean trackFilters) {
        this(registry, clock, trackFilters, ExponentiallyDecayingReservoir::new);
    }

    /*
     * @param registry          the metrics registry where the metrics will be stored
     * @param clock             the {@link Clock} to track time (used mostly in testing) in timers
     * @param trackFilters      whether the processing time for request and response filters should be tracked
     * @param reservoirSupplier Supplier for creating the {@link Reservoir} for {@link Timer timers}.
     */
    public MetricsFeature(MetricRegistry registry, Clock clock, boolean trackFilters, Supplier<Reservoir> reservoirSupplier) {
        this.registry = registry;
        this.clock = clock;
        this.trackFilters = trackFilters;
        this.reservoirSupplier = reservoirSupplier;
    }

    public MetricsFeature(String registryName) {
        this(SharedMetricRegistries.getOrCreate(registryName));
    }

    /**
     * A call-back method called when the feature is to be enabled in a given
     * runtime configuration scope.
     * <p>
     * The responsibility of the feature is to properly update the supplied runtime configuration context
     * and return {@code true} if the feature was successfully enabled or {@code false} otherwise.
     * <p>
     * Note that under some circumstances the feature may decide not to enable itself, which
     * is indicated by returning {@code false}. In such case the configuration context does
     * not add the feature to the collection of enabled features and a subsequent call to
     * {@link javax.ws.rs.core.Configuration#isEnabled(javax.ws.rs.core.Feature)} or
     * {@link javax.ws.rs.core.Configuration#isEnabled(Class)} method
     * would return {@code false}.
     * <p>
     *
     * @param context configurable context in which the feature should be enabled.
     * @return {@code true} if the feature was successfully enabled, {@code false}
     * otherwise.
     */
    @Override
    public boolean configure(FeatureContext context) {
        context.register(new InstrumentedResourceMethodApplicationListener(registry, clock, trackFilters, reservoirSupplier));
        return true;
    }
}
