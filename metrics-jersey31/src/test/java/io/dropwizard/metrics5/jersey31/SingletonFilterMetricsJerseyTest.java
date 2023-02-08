package io.dropwizard.metrics5.jersey31;

import io.dropwizard.metrics5.MetricRegistry;
import io.dropwizard.metrics5.Timer;
import io.dropwizard.metrics5.jersey31.resources.InstrumentedFilteredResource;
import io.dropwizard.metrics5.jersey31.resources.TestRequestFilter;
import jakarta.ws.rs.core.Application;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.logging.Level;
import java.util.logging.Logger;

import static io.dropwizard.metrics5.MetricRegistry.name;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests registering {@link InstrumentedResourceMethodApplicationListener} as a singleton
 * in a Jersey {@link ResourceConfig} with filter tracking
 */
class SingletonFilterMetricsJerseyTest extends JerseyTest {
    static {
        Logger.getLogger("org.glassfish.jersey").setLevel(Level.OFF);
    }

    private MetricRegistry registry;

    private TestClock testClock;

    @Override
    protected Application configure() {
        registry = new MetricRegistry();
        testClock = new TestClock();
        ResourceConfig config = new ResourceConfig();
        config = config.register(new MetricsFeature(this.registry, testClock, true));
        config = config.register(new TestRequestFilter(testClock));
        config = config.register(new InstrumentedFilteredResource(testClock));
        return config;
    }

    @BeforeEach
    void resetClock() {
        testClock.tick = 0;
    }

    @Test
    void timedMethodsAreTimed() {
        assertThat(target("timed")
        .request()
        .get(String.class))
        .isEqualTo("yay");

        final Timer timer = registry.timer(name(InstrumentedFilteredResource.class, "timed"));

        assertThat(timer.getCount()).isEqualTo(1);
        assertThat(timer.getSnapshot().getValues()[0]).isEqualTo(1);
    }

    @Test
    void explicitNamesAreTimed() {
        assertThat(target("named")
        .request()
        .get(String.class))
        .isEqualTo("fancy");

        final Timer timer = registry.timer(name(InstrumentedFilteredResource.class, "fancyName"));

        assertThat(timer.getCount()).isEqualTo(1);
        assertThat(timer.getSnapshot().getValues()[0]).isEqualTo(1);
    }

    @Test
    void absoluteNamesAreTimed() {
        assertThat(target("absolute")
        .request()
        .get(String.class))
        .isEqualTo("absolute");

        final Timer timer = registry.timer("absolutelyFancy");

        assertThat(timer.getCount()).isEqualTo(1);
        assertThat(timer.getSnapshot().getValues()[0]).isEqualTo(1);
    }

    @Test
    void requestFiltersOfTimedMethodsAreTimed() {
        assertThat(target("timed")
        .request()
        .get(String.class))
        .isEqualTo("yay");

        final Timer timer = registry.timer(name(InstrumentedFilteredResource.class, "timed", "request", "filtering"));

        assertThat(timer.getCount()).isEqualTo(1);
        assertThat(timer.getSnapshot().getValues()[0]).isEqualTo(4);
    }

    @Test
    void responseFiltersOfTimedMethodsAreTimed() {
        assertThat(target("timed")
        .request()
        .get(String.class))
        .isEqualTo("yay");

        final Timer timer = registry.timer(name(InstrumentedFilteredResource.class, "timed", "response", "filtering"));

        assertThat(timer.getCount()).isEqualTo(1);
    }

    @Test
    void totalTimeOfTimedMethodsIsTimed() {
        assertThat(target("timed")
        .request()
        .get(String.class))
        .isEqualTo("yay");

        final Timer timer = registry.timer(name(InstrumentedFilteredResource.class, "timed", "total"));

        assertThat(timer.getCount()).isEqualTo(1);
        assertThat(timer.getSnapshot().getValues()[0]).isEqualTo(5);
    }

    @Test
    void requestFiltersOfNamedMethodsAreTimed() {
        assertThat(target("named")
        .request()
        .get(String.class))
        .isEqualTo("fancy");

        final Timer timer = registry.timer(name(InstrumentedFilteredResource.class, "fancyName", "request", "filtering"));

        assertThat(timer.getCount()).isEqualTo(1);
        assertThat(timer.getSnapshot().getValues()[0]).isEqualTo(4);
    }

    @Test
    void requestFiltersOfAbsoluteMethodsAreTimed() {
        assertThat(target("absolute")
        .request()
        .get(String.class))
        .isEqualTo("absolute");

        final Timer timer = registry.timer(name("absolutelyFancy", "request", "filtering"));
        assertThat(timer.getCount()).isEqualTo(1);
        assertThat(timer.getSnapshot().getValues()[0]).isEqualTo(4);
    }

    @Test
    void subResourcesFromLocatorsRegisterMetrics() {
        assertThat(target("subresource/timed")
        .request()
        .get(String.class))
        .isEqualTo("yay");

        final Timer timer = registry.timer(name(InstrumentedFilteredResource.InstrumentedFilteredSubResource.class,
        "timed"));
        assertThat(timer.getCount()).isEqualTo(1);

    }
}
