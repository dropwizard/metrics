package com.yammer.metrics.reporting.tempodb;

import static com.yammer.dropwizard.testing.JsonHelpers.jsonFixture;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.TimeUnit;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.yammer.metrics.core.Clock;
import com.yammer.metrics.core.MetricsRegistry;

public class TempoDbReporterTest {

	private MetricsRegistry registry;
	private TempoDbReporter reporter;

	@Test
	public void multipleMetrics() throws Exception {
		this.registry.newCounter(TempoDbReporterTest.class, "counter");
		this.registry.newTimer(TempoDbReporterTest.class, "timer");
		assertFixture("combined");
	}

	@Test
	public void timer() throws Exception {
		this.registry.newTimer(TempoDbReporterTest.class, "timer");
		assertFixture("timer");
	}

	@Test
	public void counter() throws Exception {
		this.registry.newCounter(TempoDbReporterTest.class, "counter");
		assertFixture("counter");
	}

	@Test
	public void histogram() throws Exception {
		this.registry.newHistogram(TempoDbReporterTest.class, "histogram");
		assertFixture("histogram");
	}

	@Test
	public void meter() throws Exception {
		this.registry.newMeter(TempoDbReporterTest.class, "meter", "foo", TimeUnit.SECONDS);
		assertFixture("meter");
	}

	private void assertFixture(String fixture) throws Exception {
		final ByteArrayOutputStream result = new ByteArrayOutputStream();
		this.reporter.report(result);
		final ObjectMapper mapper = new ObjectMapper();
		final JsonNode actual = mapper.readTree(new String(result.toByteArray()));
		final JsonNode expected = mapper.readTree(jsonFixture(fixture + ".json"));
		assertEquals(expected, actual);
	}

	@Before
	public void setup() {
		final Clock clock = mock(Clock.class);
		when(clock.getTime()).thenReturn(302897812L);
		this.registry = new MetricsRegistry(clock);
		this.reporter = new TempoDbReporter(
				this.registry,
				TempoDbReporter.class.getSimpleName(),
				"api key",
				"api secret",
				TempoDbReporter.DEFAULT_ENDPOINT,
				clock);
	}

	@After
	public void y() {
		this.registry.shutdown();
	}
}