package io.dropwizard.metrics5.jersey3;

import io.dropwizard.metrics5.MetricRegistry;
import io.dropwizard.metrics5.Timer;
import io.dropwizard.metrics5.jersey3.resources.InstrumentedResourceTimedPerClass;
import io.dropwizard.metrics5.jersey3.resources.InstrumentedSubResourceTimedPerClass;
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
class SingletonMetricsTimedPerClassJerseyTest extends JerseyTest {
    static {
        Logger.getLogger("org.glassfish.jersey").setLevel(Level.OFF);
    }

    private MetricRegistry registry;

    @Override
    protected Application configure() {
        this.registry = new MetricRegistry();

        ResourceConfig config = new ResourceConfig();

        config = config.register(new MetricsFeature(this.registry));
        config = config.register(InstrumentedResourceTimedPerClass.class);

        return config;
    }

    @Test
    void timedPerClassMethodsAreTimed() {
        assertThat(target("timedPerClass")
        .request()
        .get(String.class))
        .isEqualTo("yay");

        final Timer timer = registry.timer(name(InstrumentedResourceTimedPerClass.class, "timedPerClass"));

        assertThat(timer.getCount()).isEqualTo(1);
    }

    @Test
    void subresourcesFromLocatorsRegisterMetrics() {
        assertThat(target("subresource/timedPerClass")
        .request()
        .get(String.class))
        .isEqualTo("yay");

        final Timer timer = registry.timer(name(InstrumentedSubResourceTimedPerClass.class, "timedPerClass"));
        assertThat(timer.getCount()).isEqualTo(1);

    }


}
