package com.codahale.metrics.jersey2;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.jersey2.resources.InstrumentedResourceTimedPerClass;
import com.codahale.metrics.jersey2.resources.InstrumentedSubResourceTimedPerClass;
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
public class SingletonMetricsTimedPerClassJerseyTest extends JerseyTest {
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
    public void timedPerClassMethodsAreTimed() {
        assertThat(target("timedPerClass")
                .request()
                .get(String.class))
                .isEqualTo("yay");

        final Timer timer = registry.timer(name(InstrumentedResourceTimedPerClass.class, "timedPerClass"));

        assertThat(timer.getCount()).isEqualTo(1);
    }

    @Test
    public void subresourcesFromLocatorsRegisterMetrics() {
        assertThat(target("subresource/timedPerClass")
                .request()
                .get(String.class))
                .isEqualTo("yay");

        final Timer timer = registry.timer(name(InstrumentedSubResourceTimedPerClass.class, "timedPerClass"));
        assertThat(timer.getCount()).isEqualTo(1);

    }


}
