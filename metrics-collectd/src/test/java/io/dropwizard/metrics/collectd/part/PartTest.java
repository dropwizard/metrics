package io.dropwizard.metrics.collectd.part;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import io.dropwizard.metrics.collectd.Part;

public abstract class PartTest<P extends Part> {

	protected P part;

	@Before
	public void setUp() {
		part = createPart();
	}

	@Test
	public void shouldHaveAPartType() {
		assertThat(part.getType(), is(getExpectedPartType()));
	}

	protected abstract P createPart();

	protected abstract PartType getExpectedPartType();
}
