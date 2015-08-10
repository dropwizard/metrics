package io.dropwizard.metrics.collectd.part;

import java.nio.charset.Charset;

public class PluginInstanceTest extends StringPartTest<PluginInstance> {

	@Override
	protected PluginInstance createPart(final String initialMetric, final Charset charset) {
		return new PluginInstance(initialMetric, charset);
	}

	@Override
	protected PartType getExpectedPartType() {
		return PartType.PLUGIN_INSTANCE;
	}

}
