package com.yammer.metrics.jersey.tests;

import com.sun.jersey.api.container.MappableContainerException;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.LowLevelAppDescriptor;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Meter;
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
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class MetricsJerseyTest extends JerseyTest {
    static {
        Logger.getLogger("com.sun.jersey").setLevel(Level.OFF);
    }

    @Override
    protected AppDescriptor configure() {
        return new LowLevelAppDescriptor.Builder(
                InstrumentedResourceMethodDispatchAdapter.class,
                InstrumentedResource.class
        ).build();
    }

    @Test
    public void timedMethodsAreTimed() {
        assertThat(resource().path("timed").get(String.class),
                   is("yay"));

        final Timer timer = Metrics.newTimer(InstrumentedResource.class, "timed");
        assertThat(timer.count(),
                   is(1L));
    }

    @Test
    public void meteredMethodsAreMetered() {
        assertThat(resource().path("metered").get(String.class),
                   is("woo"));

        final Meter meter = Metrics.newMeter(InstrumentedResource.class, "metered", "blah", TimeUnit.SECONDS);
        assertThat(meter.count(),
                   is(1L));
    }

    @Test
    public void exceptionMeteredMethodsAreExceptionMetered() {
        final Meter meter = Metrics.newMeter(InstrumentedResource.class, "exceptionMeteredExceptions", "blah", TimeUnit.SECONDS);
        
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
