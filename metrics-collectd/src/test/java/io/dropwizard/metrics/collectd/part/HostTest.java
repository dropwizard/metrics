package io.dropwizard.metrics.collectd.part;

import java.nio.charset.Charset;

public class HostTest extends StringPartTest<Host> {

	@Override
	protected Host createPart(final String initialMetric, final Charset charset) {
		return new Host(initialMetric, charset);
	}

	@Override
	protected PartType getExpectedPartType() {
		return PartType.HOST;
	}

}
