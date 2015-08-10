package io.dropwizard.metrics.collectd.part;

import java.nio.charset.Charset;

public class Plugin extends StringPart {
	public Plugin(final String value, final Charset charset) {
		super(PartType.PLUGIN, value, charset);
	}
}
