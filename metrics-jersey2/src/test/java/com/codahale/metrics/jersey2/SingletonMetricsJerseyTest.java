package com.codahale.metrics.jersey2;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.jersey2.resources.InstrumentedResource;
import com.codahale.metrics.jersey2.resources.InstrumentedSubResource;
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

import static com.codahale.metrics.MetricRegistry.name;
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

        final Timer timer = registry.timer(name(InstrumentedResource.class, "timed"));

        assertThat(timer.getCount()).isEqualTo(1);
    }

    @Test
    public void meteredMethodsAreMetered() {
        assertThat(target("metered")
                .request()
                .get(String.class))
                .isEqualTo("woo");

        final Meter meter = registry.meter(name(InstrumentedResource.class, "metered"));
        assertThat(meter.getCount()).isEqualTo(1);
    }

    @Test
    public void exceptionMeteredMethodsAreExceptionMetered() {
        final Meter meter = registry.meter(name(InstrumentedResource.class,
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
    public void responseMeteredMethodsAreMeteredWithCoarseLevel() {
        final Meter meter2xx = registry.meter(name(InstrumentedResource.class,
                "responseMeteredCoarse",
                "2xx-responses"));
        final Meter meter200 = registry.meter(name(InstrumentedResource.class,
                "responseMeteredCoarse",
                "200-responses"));

        assertThat(meter2xx.getCount()).isZero();
        assertThat(meter200.getCount()).isZero();
        assertThat(target("response-metered-coarse")
                .request()
                .get().getStatus())
                .isEqualTo(200);

        assertThat(meter2xx.getCount()).isOne();
        assertThat(meter200.getCount()).isZero();
    }

    @Test
    public void responseMeteredMethodsAreMeteredWithDetailedLevel() {
        final Meter meter2xx = registry.meter(name(InstrumentedResource.class,
                "responseMeteredDetailed",
                "2xx-responses"));
        final Meter meter200 = registry.meter(name(InstrumentedResource.class,
                "responseMeteredDetailed",
                "200-responses"));
        final Meter meter201 = registry.meter(name(InstrumentedResource.class,
                "responseMeteredDetailed",
                "201-responses"));

        assertThat(meter2xx.getCount()).isZero();
        assertThat(meter200.getCount()).isZero();
        assertThat(meter201.getCount()).isZero();
        assertThat(target("response-metered-detailed")
                .request()
                .get().getStatus())
                .isEqualTo(200);
        assertThat(target("response-metered-detailed")
                .queryParam("status_code", 201)
                .request()
                .get().getStatus())
                .isEqualTo(201);

        assertThat(meter2xx.getCount()).isZero();
        assertThat(meter200.getCount()).isOne();
        assertThat(meter201.getCount()).isOne();
    }

    @Test
    public void responseMeteredMethodsAreMeteredWithAllLevel() {
        final Meter meter2xx = registry.meter(name(InstrumentedResource.class,
                "responseMeteredAll",
                "2xx-responses"));
        final Meter meter200 = registry.meter(name(InstrumentedResource.class,
                "responseMeteredAll",
                "200-responses"));

        assertThat(meter2xx.getCount()).isZero();
        assertThat(meter200.getCount()).isZero();
        assertThat(target("response-metered-all")
                .request()
                .get().getStatus())
                .isEqualTo(200);

        assertThat(meter2xx.getCount()).isOne();
        assertThat(meter200.getCount()).isOne();
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

        final Timer timer = registry.timer(name(InstrumentedSubResource.class, "timed"));
        assertThat(timer.getCount()).isEqualTo(1);

    }
}
