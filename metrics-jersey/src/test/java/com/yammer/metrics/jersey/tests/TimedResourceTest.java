package com.yammer.metrics.jersey.tests;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Test;

import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.LowLevelAppDescriptor;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.jersey.InstrumentedResourceMethodDispatchAdapter;
import com.yammer.metrics.jersey.tests.resources.TimedResource;

/**
 * Tests importing {@link InstrumentedResourceMethodDispatchAdapter} as a
 * singleton in a Jersey {@link com.sun.jersey.api.core.ResourceConfig}
 */
public class TimedResourceTest extends JerseyTest {
	static {
		Logger.getLogger("com.sun.jersey").setLevel(Level.OFF);
	}

	private MetricsRegistry registry;

	@Override
	protected AppDescriptor configure() {
        return new LowLevelAppDescriptor.Builder(
                InstrumentedResourceMethodDispatchAdapter.class,
                TimedResource.class
        ).build();
	}

	@Test
	public void timedFirstIsTimed() {
		assertThat(resource().path("timed-first").get(String.class), is("yay-first"));

		final Timer timer = Metrics.newTimer(TimedResource.class, "timedFirst");
		assertThat(timer.count(), is(1L));
	}

	@Test
	public void timedSecondIsTimed() {
		assertThat(resource().path("timed-second").get(String.class), is("yay-second"));

		final Timer timer = Metrics.newTimer(TimedResource.class, "timedSecond");
		assertThat(timer.count(), is(1L));
	}

}
