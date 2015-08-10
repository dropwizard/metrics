package io.dropwizard.metrics.collectd.part;

import java.nio.charset.Charset;

public class TypeInstanceTest extends StringPartTest<TypeInstance> {

	@Override
	protected TypeInstance createPart(final String initialMetric, final Charset charset) {
		return new TypeInstance(initialMetric, charset);
	}

	@Override
	protected PartType getExpectedPartType() {
		return PartType.TYPE_INSTANCE;
	}

}
