package io.dropwizard.metrics.collectd.part;

import java.nio.charset.Charset;

public class MessageTest extends StringPartTest<Message> {
	@Override
	protected Message createPart(final String initialMetric, final Charset charset) {
		return new Message(initialMetric, charset);
	}

	@Override
	protected PartType getExpectedPartType() {
		return PartType.MESSAGE;
	}
}
