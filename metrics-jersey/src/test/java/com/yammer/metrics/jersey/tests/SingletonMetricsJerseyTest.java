package com.yammer.metrics.jersey.tests;

import com.sun.jersey.api.container.MappableContainerException;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.LowLevelAppDescriptor;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Meter;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.jersey.InstrumentedResourceMethodDispatchAdapter;
import com.yammer.metrics.jersey.tests.resources.InstrumentedResource;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Tests importing {@link InstrumentedResourceMethodDispatchAdapter} as a singleton
 * in a Jersey {@link com.sun.jersey.api.core.ResourceConfig}
 */
public class SingletonMetricsJerseyTest extends JerseyTest {
    static {
        Logger.getLogger("com.sun.jersey").setLevel(Level.OFF);
    }

    private MetricsRegistry registry;

    @Override
    protected AppDescriptor configure() {
        this.registry = new MetricsRegistry();

        final DefaultResourceConfig config = new DefaultResourceConfig();
        config.getSingletons().add(new InstrumentedResourceMethodDispatchAdapter(registry));
        config.getClasses().add(InstrumentedResource.class);

        return new LowLevelAppDescriptor.Builder(config).build();
    }

    @Test
    public void registryIsNotDefault() {
        final Timer timer1 = registry.newTimer(InstrumentedResource.class, "timed");
        final Timer timer2 = registry.newTimer(InstrumentedResource.class, "timed");
        final Timer timer3 = Metrics.defaultRegistry().newTimer(InstrumentedResource.class, "timed");

        assertThat(timer1, sameInstance(timer2));
        assertThat(timer1, not(sameInstance(timer3)));
    }

    @Test
    public void timedMethodsAreTimed() {
        assertThat(resource().path("timed").get(String.class),
                   is("yay"));

        final Timer timer = registry.newTimer(InstrumentedResource.class, "timed");
        assertThat(timer.count(),
                   is(1L));
    }

    @Test
    public void meteredMethodsAreMetered() {
        assertThat(resource().path("metered").get(String.class),
                   is("woo"));

        final Meter meter = registry.newMeter(InstrumentedResource.class, "metered", "blah", TimeUnit.SECONDS);
        assertThat(meter.count(),
                   is(1L));
    }

    @Test
    public void exceptionMeteredMethodsAreExceptionMetered() {
        final Meter meter = registry.newMeter(InstrumentedResource.class, "exceptionMeteredExceptions", "blah", TimeUnit.SECONDS);
        
        assertThat(resource().path("exception-metered").get(String.class),
                   is("fuh"));

        assertThat(meter.count(),
                   is(0L));
        
        try {
            resource().path("exception-metered").queryParam("splode", "true").get(String.class);
            fail("should have thrown a MappableContainerException, but didn't");
        } catch (MappableContainerException e) {
            assertThat(e.getCause(),
                       is(instanceOf(IOException.class)));
        }

        assertThat(meter.count(),
                   is(1L));
    }
}
