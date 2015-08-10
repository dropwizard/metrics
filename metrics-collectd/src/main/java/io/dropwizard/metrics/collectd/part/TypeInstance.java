package io.dropwizard.metrics.collectd.part;

import java.nio.charset.Charset;

public class TypeInstance extends StringPart {
	public TypeInstance(final String value, final Charset charset) {
		super(PartType.TYPE_INSTANCE, value, charset);
	}
}
