package com.codahale.metrics.jersey2;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.UniformReservoir;
import com.codahale.metrics.jersey2.resources.InstrumentedResourceTimedPerClass;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

import javax.ws.rs.core.Application;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.codahale.metrics.MetricRegistry.name;
import static org.assertj.core.api.Assertions.assertThat;

public class CustomReservoirImplementationTest extends JerseyTest {
    static {
        Logger.getLogger("org.glassfish.jersey").setLevel(Level.OFF);
    }

    private MetricRegistry registry;

    @Override
    protected Application configure() {
        this.registry = new MetricRegistry();

        return new ResourceConfig()
                .register(new MetricsFeature(this.registry, UniformReservoir::new))
                .register(InstrumentedResourceTimedPerClass.class);
    }

    @Test
    public void timerHistogramIsUsingCustomReservoirImplementation() {
        assertThat(target("timedPerClass").request().get(String.class)).isEqualTo("yay");

        final Timer timer = registry.timer(name(InstrumentedResourceTimedPerClass.class, "timedPerClass"));
        assertThat(timer)
                .extracting("histogram")
                .extracting("reservoir")
                .isInstanceOf(UniformReservoir.class);
    }
}
