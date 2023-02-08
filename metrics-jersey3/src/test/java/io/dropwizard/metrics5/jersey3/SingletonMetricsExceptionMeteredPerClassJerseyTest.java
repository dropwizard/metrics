package io.dropwizard.metrics5.jersey3;

import io.dropwizard.metrics5.Meter;
import io.dropwizard.metrics5.MetricRegistry;
import io.dropwizard.metrics5.jersey3.resources.InstrumentedResourceExceptionMeteredPerClass;
import io.dropwizard.metrics5.jersey3.resources.InstrumentedSubResourceExceptionMeteredPerClass;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.core.Application;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static io.dropwizard.metrics5.MetricRegistry.name;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;

/**
 * Tests registering {@link InstrumentedResourceMethodApplicationListener} as a singleton
 * in a Jersey {@link ResourceConfig}
 */
class SingletonMetricsExceptionMeteredPerClassJerseyTest extends JerseyTest {
    static {
        Logger.getLogger("org.glassfish.jersey").setLevel(Level.OFF);
    }

    private MetricRegistry registry;

    @Override
    protected Application configure() {
        this.registry = new MetricRegistry();

        ResourceConfig config = new ResourceConfig();

        config = config.register(new MetricsFeature(this.registry));
        config = config.register(InstrumentedResourceExceptionMeteredPerClass.class);

        return config;
    }

    @Test
    void exceptionMeteredMethodsAreExceptionMetered() {
        final Meter meter = registry.meter(name(InstrumentedResourceExceptionMeteredPerClass.class,
        "exceptionMetered",
        "exceptions"));

        assertThat(target("exception-metered")
        .request()
        .get(String.class))
        .isEqualTo("fuh");

        assertThat(meter.getCount()).isZero();

        try {
            target("exception-metered")
            .queryParam("splode", true)
            .request()
            .get(String.class);

            failBecauseExceptionWasNotThrown(ProcessingException.class);
        } catch (ProcessingException e) {
            assertThat(e.getCause()).isInstanceOf(IOException.class);
        }

        assertThat(meter.getCount()).isEqualTo(1);
    }

    @Test
    void subresourcesFromLocatorsRegisterMetrics() {
        final Meter meter = registry.meter(name(InstrumentedSubResourceExceptionMeteredPerClass.class,
        "exceptionMetered",
        "exceptions"));

        assertThat(target("subresource/exception-metered")
        .request()
        .get(String.class))
        .isEqualTo("fuh");

        assertThat(meter.getCount()).isZero();

        try {
            target("subresource/exception-metered")
            .queryParam("splode", true)
            .request()
            .get(String.class);

            failBecauseExceptionWasNotThrown(ProcessingException.class);
        } catch (ProcessingException e) {
            assertThat(e.getCause()).isInstanceOf(IOException.class);
        }

        assertThat(meter.getCount()).isEqualTo(1);
    }

}
