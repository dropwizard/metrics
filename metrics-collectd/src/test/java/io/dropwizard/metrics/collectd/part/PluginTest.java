package io.dropwizard.metrics.collectd.part;

import java.nio.charset.Charset;

public class PluginTest extends StringPartTest<Plugin> {

	@Override
	protected Plugin createPart(final String initialMetric, final Charset charset) {
		return new Plugin(initialMetric, charset);
	}

	@Override
	protected PartType getExpectedPartType() {
		return PartType.PLUGIN;
	}

}
