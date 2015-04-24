package com.codahale.metrics.jersey2;

import com.codahale.metrics.Meter;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.jersey2.resources.InstrumentedResource;
import org.glassfish.jersey.client.ClientResponse;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Map;
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
    public void timedMethodsInInterfaceAreTimed() {
        assertThat(target("timed-in-interface")
                .request()
                .get(String.class))
                .isEqualTo("yay-interface");

        Metric metric = findMetric("timedInInterface");
        assertThat(metric).isInstanceOf(Timer.class);
        assertThat(((Timer) metric).getCount()).isEqualTo(1);
    }

    @Test
    public void timedMethodsInImplementationAreTimed() {
        assertThat(target("timed-in-implementation")
                .request()
                .get(String.class))
                .isEqualTo("yay-implementation");

        Metric metric = findMetric("timedInImplementation");
        assertThat(metric).isInstanceOf(Timer.class);
        assertThat(((Timer) metric).getCount()).isEqualTo(1);
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
    public void meteredMethodsInInterfaceAreMetered() {
        assertThat(target("metered-in-interface")
                .request()
                .get(String.class))
                .isEqualTo("woo-interface");

        Metric metric = findMetric("meteredInInterface");
        assertThat(metric).isInstanceOf(Meter.class);
        assertThat(((Meter) metric).getCount()).isEqualTo(1);
    }

    @Test
    public void meteredMethodsInImplementationAreMetered() {
        assertThat(target("metered-in-implementation")
                .request()
                .get(String.class))
                .isEqualTo("woo-implementation");

        Metric metric = findMetric("meteredInImplementation");
        assertThat(metric).isInstanceOf(Meter.class);
        assertThat(((Meter) metric).getCount()).isEqualTo(1);
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
    public void exceptionMeteredMethodsInInterfaceAreExceptionMetered() {
        assertThat(target("exception-metered-in-interface")
                .request()
                .get(String.class))
                .isEqualTo("fuh-interface");

        Metric metric = findMetric("exceptionMeteredInInterface.exceptions");
        assertThat(metric).isInstanceOf(Meter.class);
        assertThat(((Meter) metric).getCount()).isEqualTo(0);

        try {
            target("exception-metered-in-interface")
                    .queryParam("splode", true)
                    .request()
                    .get(String.class);
            failBecauseExceptionWasNotThrown(ProcessingException.class);
        } catch (ProcessingException e) {
            assertThat(e.getCause()).isInstanceOf(IOException.class);
        }
        assertThat(((Meter) metric).getCount()).isEqualTo(1);
    }

    @Test
    public void exceptionMeteredMethodsInImplementationAreExceptionMetered() {
        assertThat(target("exception-metered-in-implementation")
                .request()
                .get(String.class))
                .isEqualTo("fuh-implementation");

        Metric metric = findMetric("exceptionMeteredInImplementation.exceptions");
        assertThat(metric).isInstanceOf(Meter.class);
        assertThat(((Meter) metric).getCount()).isEqualTo(0);

        try {
            target("exception-metered-in-implementation")
                    .queryParam("splode", true)
                    .request()
                    .get(String.class);
            failBecauseExceptionWasNotThrown(ProcessingException.class);
        } catch (ProcessingException e) {
            assertThat(e.getCause()).isInstanceOf(IOException.class);
        }
        assertThat(((Meter) metric).getCount()).isEqualTo(1);
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

    private Metric findMetric(final String name) {
        for (Map.Entry<String, Metric> entry : registry.getMetrics().entrySet()) {
            if (entry.getKey().endsWith(name)) {
                return entry.getValue();
            }
        }
        Assert.fail("No metric with this name found.");
        throw new IllegalArgumentException();
    }
}
