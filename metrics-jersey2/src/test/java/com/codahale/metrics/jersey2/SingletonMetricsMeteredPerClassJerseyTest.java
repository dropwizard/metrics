package com.codahale.metrics.jersey2;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jersey2.resources.InstrumentedResourceMeteredPerClass;
import com.codahale.metrics.jersey2.resources.InstrumentedSubResourceMeteredPerClass;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

import javax.ws.rs.core.Application;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.codahale.metrics.MetricRegistry.name;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests registering {@link InstrumentedResourceMethodApplicationListener} as a singleton
 * in a Jersey {@link ResourceConfig}
 */
public class SingletonMetricsMeteredPerClassJerseyTest extends JerseyTest {
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
    public void meteredPerClassMethodsAreMetered() {
        assertThat(target("meteredPerClass")
                .request()
                .get(String.class))
                .isEqualTo("yay");

        final Meter meter = registry.meter(name(InstrumentedResourceMeteredPerClass.class, "meteredPerClass"));

        assertThat(meter.getCount()).isEqualTo(1);
    }

    @Test
    public void subresourcesFromLocatorsRegisterMetrics() {
        assertThat(target("subresource/meteredPerClass")
                .request()
                .get(String.class))
                .isEqualTo("yay");

        final Meter meter = registry.meter(name(InstrumentedSubResourceMeteredPerClass.class, "meteredPerClass"));
        assertThat(meter.getCount()).isEqualTo(1);

    }


}
