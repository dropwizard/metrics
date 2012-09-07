package com.yammer.metrics.jersey.tests;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Test;

import com.sun.jersey.api.container.MappableContainerException;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.LowLevelAppDescriptor;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Meter;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.jersey.InstrumentedResourceMethodDispatchAdapter;
import com.yammer.metrics.jersey.tests.resources.ExceptionMeteredResource;

/**
 * Tests importing {@link InstrumentedResourceMethodDispatchAdapter} as a
 * singleton in a Jersey {@link com.sun.jersey.api.core.ResourceConfig}
 */
public class ExceptionMeteredResourceTest extends JerseyTest {
	static {
		Logger.getLogger("com.sun.jersey").setLevel(Level.OFF);
	}

	private MetricsRegistry registry;

	@Override
	protected AppDescriptor configure() {
        return new LowLevelAppDescriptor.Builder(
                InstrumentedResourceMethodDispatchAdapter.class,
                ExceptionMeteredResource.class
        ).build();		
	}

	@Test
	public void exceptionMeteredFirstIsExceptionMetered() {
		final Meter meter = Metrics.newMeter(ExceptionMeteredResource.class, "exceptionMeteredFirstExceptions", "blah", TimeUnit.SECONDS);

		assertThat(resource().path("exception-metered-first").get(String.class), is("fuh"));

		assertThat(meter.count(), is(0L));

		try {
			resource().path("exception-metered-first").queryParam("splode", "true").get(String.class);
			fail("should have thrown a MappableContainerException, but didn't");
		} catch (MappableContainerException e) {
			assertThat(e.getCause(), is(instanceOf(IOException.class)));
		}

		assertThat(meter.count(), is(1L));
	}

	@Test
	public void exceptionMeteredSecondIsExceptionMetered() {
		final Meter meter = Metrics.newMeter(ExceptionMeteredResource.class, "exceptionMeteredSecondExceptions", "blah", TimeUnit.SECONDS);

		assertThat(resource().path("exception-metered-second").get(String.class), is("fuh"));

		assertThat(meter.count(), is(0L));

		try {
			resource().path("exception-metered-second").queryParam("splode", "true").get(String.class);
			fail("should have thrown a MappableContainerException, but didn't");
		} catch (MappableContainerException e) {
			assertThat(e.getCause(), is(instanceOf(IOException.class)));
		}

		assertThat(meter.count(), is(1L));
	}

}
