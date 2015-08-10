package io.dropwizard.metrics.collectd.part;

import java.nio.charset.Charset;

public class Host extends StringPart {
	public Host(final String value, final Charset charset) {
		super(PartType.HOST, value, charset);
	}
}
