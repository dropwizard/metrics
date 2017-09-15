package com.codahale.metrics.jersey2;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.jersey2.resources.InstrumentedFilteredResource;
import com.codahale.metrics.jersey2.resources.TestRequestFilter;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Application;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.codahale.metrics.MetricRegistry.name;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests registering {@link InstrumentedResourceMethodApplicationListener} as a singleton
 * in a Jersey {@link ResourceConfig} with filter tracking
 */
public class SingletonFilterMetricsJerseyTest extends JerseyTest {
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

        final Timer timer = registry.timer(name(InstrumentedFilteredResource.class, "timed"));

        assertThat(timer.getCount()).isEqualTo(1);
        assertThat(timer.getSnapshot().getValues()[0]).isEqualTo(1);
    }

    @Test
    public void explicitNamesAreTimed() {
        assertThat(target("named")
                .request()
                .get(String.class))
                .isEqualTo("fancy");

        final Timer timer = registry.timer(name(InstrumentedFilteredResource.class, "fancyName"));

        assertThat(timer.getCount()).isEqualTo(1);
        assertThat(timer.getSnapshot().getValues()[0]).isEqualTo(1);
    }

    @Test
    public void absoluteNamesAreTimed() {
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

        final Timer timer = registry.timer(name(InstrumentedFilteredResource.class, "timed", "request", "filtering"));

        assertThat(timer.getCount()).isEqualTo(1);
        assertThat(timer.getSnapshot().getValues()[0]).isEqualTo(4);
    }

    @Test
    public void responseFiltersOfTimedMethodsAreTimed() {
        assertThat(target("timed")
                .request()
                .get(String.class))
                .isEqualTo("yay");

        final Timer timer = registry.timer(name(InstrumentedFilteredResource.class, "timed", "response", "filtering"));

        assertThat(timer.getCount()).isEqualTo(1);
    }

    @Test
    public void totalTimeOfTimedMethodsIsTimed() {
        assertThat(target("timed")
                .request()
                .get(String.class))
                .isEqualTo("yay");

        final Timer timer = registry.timer(name(InstrumentedFilteredResource.class, "timed", "total"));

        assertThat(timer.getCount()).isEqualTo(1);
        assertThat(timer.getSnapshot().getValues()[0]).isEqualTo(5);
    }

    @Test
    public void requestFiltersOfNamedMethodsAreTimed() {
        assertThat(target("named")
                .request()
                .get(String.class))
                .isEqualTo("fancy");

        final Timer timer = registry.timer(name(InstrumentedFilteredResource.class, "fancyName", "request", "filtering"));

        assertThat(timer.getCount()).isEqualTo(1);
        assertThat(timer.getSnapshot().getValues()[0]).isEqualTo(4);
    }

    @Test
    public void requestFiltersOfAbsoluteMethodsAreTimed() {
        assertThat(target("absolute")
                .request()
                .get(String.class))
                .isEqualTo("absolute");

        final Timer timer = registry.timer(name("absolutelyFancy", "request", "filtering"));
        assertThat(timer.getCount()).isEqualTo(1);
        assertThat(timer.getSnapshot().getValues()[0]).isEqualTo(4);
    }

    @Test
    public void subResourcesFromLocatorsRegisterMetrics() {
        assertThat(target("subresource/timed")
                .request()
                .get(String.class))
                .isEqualTo("yay");

        final Timer timer = registry.timer(name(InstrumentedFilteredResource.InstrumentedFilteredSubResource.class,
                "timed"));
        assertThat(timer.getCount()).isEqualTo(1);

    }
}
