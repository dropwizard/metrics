package io.dropwizard.metrics5.jersey3;

import io.dropwizard.metrics5.Meter;
import io.dropwizard.metrics5.MetricRegistry;
import io.dropwizard.metrics5.jersey3.resources.InstrumentedResourceMeteredPerClass;
import io.dropwizard.metrics5.jersey3.resources.InstrumentedSubResourceMeteredPerClass;
import jakarta.ws.rs.core.Application;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.Test;

import java.util.logging.Level;
import java.util.logging.Logger;

import static io.dropwizard.metrics5.MetricRegistry.name;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests registering {@link InstrumentedResourceMethodApplicationListener} as a singleton
 * in a Jersey {@link ResourceConfig}
 */
class SingletonMetricsMeteredPerClassJerseyTest extends JerseyTest {
    static {
        Logger.getLogger("org.glassfish.jersey").setLevel(Level.OFF);
    }

    private MetricRegistry registry;

    @Override
    protected Application configure() {
        this.registry = new MetricRegistry();

        ResourceConfig config = new ResourceConfig();

        config = config.register(new MetricsFeature(this.registry));
        config = config.register(InstrumentedResourceMeteredPerClass.class);

        return config;
    }

    @Test
    void meteredPerClassMethodsAreMetered() {
        assertThat(target("meteredPerClass")
        .request()
        .get(String.class))
        .isEqualTo("yay");

        final Meter meter = registry.meter(name(InstrumentedResourceMeteredPerClass.class, "meteredPerClass"));

        assertThat(meter.getCount()).isEqualTo(1);
    }

    @Test
    void subresourcesFromLocatorsRegisterMetrics() {
        assertThat(target("subresource/meteredPerClass")
        .request()
        .get(String.class))
        .isEqualTo("yay");

        final Meter meter = registry.meter(name(InstrumentedSubResourceMeteredPerClass.class, "meteredPerClass"));
        assertThat(meter.getCount()).isEqualTo(1);

    }


}
