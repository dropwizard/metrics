package io.dropwizard.metrics.collectd.part;

import java.nio.charset.Charset;

public class PluginInstance extends StringPart {
	public PluginInstance(final String value, final Charset charset) {
		super(PartType.PLUGIN_INSTANCE, value, charset);
	}
}
