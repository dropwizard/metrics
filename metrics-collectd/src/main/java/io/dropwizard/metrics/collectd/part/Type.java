package io.dropwizard.metrics.collectd.part;

import java.nio.charset.Charset;

public class Type extends StringPart {
	public Type(final String value, final Charset charset) {
		super(PartType.TYPE, value, charset);
	}
}
