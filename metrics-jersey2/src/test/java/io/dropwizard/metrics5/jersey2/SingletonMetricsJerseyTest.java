package io.dropwizard.metrics5.jersey2;

import io.dropwizard.metrics5.Meter;
import io.dropwizard.metrics5.MetricRegistry;
import io.dropwizard.metrics5.Timer;
import io.dropwizard.metrics5.jersey2.resources.InstrumentedResource;
import io.dropwizard.metrics5.jersey2.resources.InstrumentedSubResource;
import org.glassfish.jersey.client.ClientResponse;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;

/**
 * Tests registering {@link InstrumentedResourceMethodApplicationListener} as a singleton
 * in a Jersey {@link org.glassfish.jersey.server.ResourceConfig}
 */
public class SingletonMetricsJerseyTest extends JerseyTest {
    static {
        Logger.getLogger("org.glassfish.jersey").setLevel(Level.OFF);
    }

    private MetricRegistry registry;

    @Override
    protected Application configure() {
        this.registry = new MetricRegistry();

        ResourceConfig config = new ResourceConfig();
        config = config.register(new MetricsFeature(this.registry));
        config = config.register(InstrumentedResource.class);

        return config;
    }

    @Test
    public void timedMethodsAreTimed() {
        assertThat(target("timed")
                .request()
                .get(String.class))
                .isEqualTo("yay");

        final Timer timer = registry.timer(MetricRegistry.name(InstrumentedResource.class, "timed"));

        assertThat(timer.getCount()).isEqualTo(1);
    }

    @Test
    public void meteredMethodsAreMetered() {
        assertThat(target("metered")
                .request()
                .get(String.class))
                .isEqualTo("woo");

        final Meter meter = registry.meter(MetricRegistry.name(InstrumentedResource.class, "metered"));
        assertThat(meter.getCount()).isEqualTo(1);
    }

    @Test
    public void exceptionMeteredMethodsAreExceptionMetered() {
        final Meter meter = registry.meter(MetricRegistry.name(InstrumentedResource.class,
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
    public void responseMeteredMethodsAreMetered() {
        final Meter meter2xx = registry.meter(MetricRegistry.name(InstrumentedResource.class,
                "response2xxMetered",
                "2xx-responses"));
        final Meter meter4xx = registry.meter(MetricRegistry.name(InstrumentedResource.class,
                "response4xxMetered",
                "4xx-responses"));
        final Meter meter5xx = registry.meter(MetricRegistry.name(InstrumentedResource.class,
                "response5xxMetered",
                "5xx-responses"));

        assertThat(meter2xx.getCount()).isZero();
        assertThat(target("response-2xx-metered")
                .request()
                .get().getStatus())
                .isEqualTo(200);

        assertThat(meter4xx.getCount()).isZero();
        assertThat(target("response-4xx-metered")
                .request()
                .get().getStatus())
                .isEqualTo(400);

        assertThat(meter5xx.getCount()).isZero();
        assertThat(target("response-5xx-metered")
                .request()
                .get().getStatus())
                .isEqualTo(500);

        assertThat(meter2xx.getCount()).isEqualTo(1);
        assertThat(meter4xx.getCount()).isEqualTo(1);
        assertThat(meter5xx.getCount()).isEqualTo(1);
    }

    @Test
    public void testResourceNotFound() {
        final Response response = target().path("not-found").request().get();
        assertThat(response.getStatus()).isEqualTo(404);

        try {
            target().path("not-found").request().get(ClientResponse.class);
            failBecauseExceptionWasNotThrown(NotFoundException.class);
        } catch (NotFoundException e) {
            assertThat(e.getMessage()).isEqualTo("HTTP 404 Not Found");
        }
    }

    @Test
    public void subresourcesFromLocatorsRegisterMetrics() {
        assertThat(target("subresource/timed")
                .request()
                .get(String.class))
                .isEqualTo("yay");

        final Timer timer = registry.timer(MetricRegistry.name(InstrumentedSubResource.class, "timed"));
        assertThat(timer.getCount()).isEqualTo(1);

    }
}
