package com.yammer.metrics.guava;

import com.google.common.cache.CacheLoader;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Timer;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

/**
 * An instrumented {@link CacheLoader} instance.
 */
public class InstrumentedCacheLoader<K,V> extends CacheLoader<K,V> {
    /**
     * Instruments the given {@link CacheLoader} instance with a load timer.
     *
     * @param cacheLoader a {@link CacheLoader} instance
     * @return an instrumented decorator for {@code cacheLoader}
     */
    public static <K,V> CacheLoader<K,V> instrument(final CacheLoader<K,V> cacheLoader) {
        return new InstrumentedCacheLoader(cacheLoader);
    }

    private final CacheLoader<K,V> underlyingCacheLoader;
    private final Timer loadTimer;

    private InstrumentedCacheLoader(CacheLoader<K,V> cacheLoader) {
        this.underlyingCacheLoader = cacheLoader;
        this.loadTimer = Metrics.newTimer(cacheLoader.getClass(), "load", TimeUnit.MICROSECONDS, TimeUnit.SECONDS);
    }

    @Override
    public V load(K key) throws Exception {
        final long start = System.nanoTime();
        try {
            return underlyingCacheLoader.load(key);
        } finally {
            loadTimer.update(System.nanoTime() - start, TimeUnit.NANOSECONDS);
        }
    }
}
