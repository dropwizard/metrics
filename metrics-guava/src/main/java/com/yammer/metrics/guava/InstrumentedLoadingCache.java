package com.yammer.metrics.guava;

import com.google.common.cache.ForwardingLoadingCache;
import com.google.common.cache.LoadingCache;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.core.Timer;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * An instrumented {@link LoadingCache} instance.
 */
public class InstrumentedLoadingCache<K,V> extends ForwardingLoadingCache<K,V> {
    /**
     * Instruments the given {@link LoadingCache} instance with a get timer
     * and a set of gauges for LoadingCache's built-in statistics.
     *
     * @param cacheName the name of the cache
     * @param cache a {@link LoadingCache} instance
     * @return an instrumented decorator for {@code cache}
     * @see InstrumentedCache#instrumentCache
     * @see com.google.common.cache.CacheStats
     */
    public static <K,V> LoadingCache<K,V> instrument(String cacheName, final LoadingCache<K,V> cache) {
        InstrumentedCache.instrumentCache(cacheName, cache);
        return new InstrumentedLoadingCache(cacheName, cache);
    }

    private final LoadingCache<K,V> underlyingCache;
    private final Timer getTimer;

    private InstrumentedLoadingCache(String cacheName, LoadingCache<K,V> cache) {
        this.underlyingCache = cache;
        this.getTimer = Metrics.newTimer(cache.getClass(), "get", cacheName, TimeUnit.MICROSECONDS, TimeUnit.SECONDS);
    }

    protected LoadingCache<K,V> delegate() {
        return underlyingCache;
    }

    @Override
    public V get(K key) throws ExecutionException {
        final long start = System.nanoTime();
        try {
            return underlyingCache.get(key);
        } finally {
            getTimer.update(System.nanoTime() - start, TimeUnit.NANOSECONDS);
        }
    }

    @Override
    public V getUnchecked(K key) {
        final long start = System.nanoTime();
        try {
            return underlyingCache.getUnchecked(key);
        } finally {
            getTimer.update(System.nanoTime() - start, TimeUnit.NANOSECONDS);
        }
    }
}
