package io.dropwizard.metrics.collectd.part;

import java.nio.charset.Charset;

public class Message extends StringPart {
	public Message(final String value, final Charset charset) {
		super(PartType.MESSAGE, value, charset);
	}
}
