package io.dropwizard.metrics.collectd.value;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import io.dropwizard.metrics.collectd.Value;

public abstract class ValueTest<V extends Value> {

	protected V value;

	@Before
	public void setUp() {
		value = createValue();
	}

	@Test
	public void shouldHaveADataType() {
		assertThat(value.getDataType(), is(getExpectedDataType()));
	}

	protected abstract DataType getExpectedDataType();

	protected abstract V createValue();
}
