package io.dropwizard.metrics.collectd.part;

import java.nio.charset.Charset;

public class TypeTest extends StringPartTest<Type> {

	@Override
	protected Type createPart(final String initialMetric, final Charset charset) {
		return new Type(initialMetric, charset);
	}

	@Override
	protected PartType getExpectedPartType() {
		return PartType.TYPE;
	}

}
