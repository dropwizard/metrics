package com.yammer.metrics.ehcache;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.constructs.CacheDecoratorFactory;

import java.util.Properties;

public class InstrumentedEhcacheFactory extends CacheDecoratorFactory {

    @Override
    public Ehcache createDecoratedEhcache(Ehcache cache, Properties properties) {
        return InstrumentedEhcache.instrument(cache);
    }

    @Override
    public Ehcache createDefaultDecoratedEhcache(Ehcache cache, Properties properties) {
        return InstrumentedEhcache.instrument(cache);
    }

}
