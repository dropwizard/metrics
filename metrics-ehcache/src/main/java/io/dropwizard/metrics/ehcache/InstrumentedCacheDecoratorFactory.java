package io.dropwizard.metrics.ehcache;

import io.dropwizard.metrics.MetricRegistry;
import io.dropwizard.metrics.SharedMetricRegistries;

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
