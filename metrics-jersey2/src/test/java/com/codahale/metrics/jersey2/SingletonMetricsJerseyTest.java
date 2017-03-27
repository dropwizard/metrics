package com.codahale.metrics.jersey2;

import com.codahale.metrics.Clock;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.jersey2.resources.InstrumentedResource;
import com.codahale.metrics.jersey2.resources.InstrumentedSubResource;
import com.codahale.metrics.jersey2.resources.TestRequestFilter;

import org.glassfish.jersey.client.ClientResponse;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Before;
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

    private TestClock testClock;

    public class TestClockProvider implements InstrumentedResourceMethodApplicationListener.ClockProvider {
        @Override
        public Clock get() {
            return testClock;
        }
    }

    @Override
    protected Application configure() {
        this.registry = new MetricRegistry();
        testClock = new TestClock();
        ResourceConfig config = new ResourceConfig();
        config = config.register(new MetricsFeature(this.registry, new TestClockProvider()));
        config = config.register(new TestRequestFilter(testClock));
        config = config.register(new InstrumentedResource(testClock));

        return config;
    }

    @Before
    public void resetClock() {
        testClock.tick = 0;
    }

    @Test
    public void timedMethodsAreTimed() {
        assertThat(target("timed")
                .request()
                .get(String.class))
                .isEqualTo("yay");

        final Timer timer = registry.timer(name(InstrumentedResource.class, "timed"));

        assertThat(timer.getCount()).isEqualTo(1);
        assertThat(timer.getSnapshot().getValues()[0]).isEqualTo(1);
    }

    @Test
    public void explicitNamesAreUsed() {
        assertThat(target("named")
            .request()
            .get(String.class))
            .isEqualTo("fancy");

        final Timer timer = registry.timer(name(InstrumentedResource.class, "fancyName"));

        assertThat(timer.getCount()).isEqualTo(1);
        assertThat(timer.getSnapshot().getValues()[0]).isEqualTo(1);
    }

    @Test
    public void absoluteNamesAreNotPrefixed() {
        assertThat(target("absolute")
            .request()
            .get(String.class))
            .isEqualTo("absolute");

        final Timer timer = registry.timer("absolutelyFancy");

        assertThat(timer.getCount()).isEqualTo(1);
        assertThat(timer.getSnapshot().getValues()[0]).isEqualTo(1);
    }

    @Test
    public void requestFiltersOfTimedMethodsAreTimed() {
        assertThat(target("timed")
            .request()
            .get(String.class))
            .isEqualTo("yay");

        final Timer timer = registry.timer(name(InstrumentedResource.class, "timed", "request", "filtering"));

        assertThat(timer.getCount()).isEqualTo(1);
        assertThat(timer.getSnapshot().getValues()[0]).isEqualTo(4);
    }

    @Test
    public void requestTimedMethodsGetTotalTimer() {
        assertThat(target("timed")
            .request()
            .get(String.class))
            .isEqualTo("yay");

        final Timer timer = registry.timer(name(InstrumentedResource.class, "timed", "total"));

        assertThat(timer.getCount()).isEqualTo(1);
        assertThat(timer.getSnapshot().getValues()[0]).isEqualTo(5);
    }

    @Test
    public void implicitlyNamedMethodsDoGetFilterMetrics() {
        assertThat(target("timed")
            .request()
            .get(String.class))
            .isEqualTo("yay");

        assertThat(registry.getMeters()).doesNotContainKey(
            name(InstrumentedResource.class, "timed", "request", "filtering")
        );
    }

    @Test
    public void explicitlyNamedMethodsDontGetFilterMetrics() {
        assertThat(target("named")
            .request()
            .get(String.class))
            .isEqualTo("fancy");

        assertThat(registry.getMeters()).doesNotContainKey(
            name(InstrumentedResource.class, "fancyName", "request", "filtering")
        );
    }

    @Test
    public void responseFiltersOfTimedMethodsAreTimed() {
        assertThat(target("timed")
            .request()
            .get(String.class))
            .isEqualTo("yay");

        final Timer timer = registry.timer(name(InstrumentedResource.class, "timed", "response", "filtering"));

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
