package io.dropwizard.metrics5.ehcache;

import io.dropwizard.metrics5.MetricRegistry;
import io.dropwizard.metrics5.SharedMetricRegistries;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.constructs.CacheDecoratorFactory;

import java.util.Properties;

public class InstrumentedCacheDecoratorFactory extends CacheDecoratorFactory {
    @Override
    public Ehcache createDecoratedEhcache(Ehcache cache, Properties properties) {
        final String name = properties.getProperty("metric-registry-name");
        final MetricRegistry registry = SharedMetricRegistries.getOrCreate(name);
        return InstrumentedEhcache.instrument(registry, cache);
    }

    @Override
    public Ehcache createDefaultDecoratedEhcache(Ehcache cache, Properties properties) {
        final String name = properties.getProperty("metric-registry-name");
        final MetricRegistry registry = SharedMetricRegistries.getOrCreate(name);
        return InstrumentedEhcache.instrument(registry, cache);
    }
}
