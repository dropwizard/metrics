package com.yammer.metrics.guava;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheLoader;
import com.yammer.metrics.core.Meter;
import com.yammer.metrics.core.MetricsRegistry;

import java.util.concurrent.TimeUnit;

/**
 * An instrumented {@link CacheLoader} implementation which records the number and rate of cache
 * misses.
 *
 * @param <K>    the type of keys in the cache
 * @param <V>    the type of values in the cache
 */
public class InstrumentedCacheLoader<K, V> extends CacheLoader<K, V> {
    /**
     * Instruments an existing {@link CacheLoader}.
     *
     * @param registry     a {@link MetricsRegistry}
     * @param loader       an existing {@link CacheLoader}
     * @param cacheName    the name of the cache
     * @param rateUnit     the rate unit to measure misses in
     * @param <K>          the type of keys in the cache
     * @param <V>          the type of values in the cache
     * @return an instrumented decorator for {@code loader}
     */
    public static <K, V> CacheLoader<K, V> instrument(MetricsRegistry registry,
                                                      CacheLoader<K, V> loader,
                                                      String cacheName,
                                                      TimeUnit rateUnit) {
        final Meter cacheMisses = registry.newMeter(Cache.class, "cache-misses", cacheName, "misses", rateUnit);
        return new InstrumentedCacheLoader<K, V>(loader, cacheMisses);
    }

    private final CacheLoader<K, V> loader;
    private final Meter cacheMisses;

    private InstrumentedCacheLoader(CacheLoader<K, V> loader, Meter cacheMisses) {
        this.loader = loader;
        this.cacheMisses = cacheMisses;
    }

    @Override
    public V load(K key) throws Exception {
        cacheMisses.mark();
        return loader.load(key);
    }
}
