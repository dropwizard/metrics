package com.yammer.metrics.guava;

import com.google.common.cache.Cache;
import com.google.common.cache.ForwardingCache;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.core.Timer;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

/**
 * An instrumented {@link Cache} instance.
 */
public class InstrumentedCache<K,V> extends ForwardingCache<K,V> {
    /**
     * Instruments the given {@link Cache} instance with a set of gauges for
     * Cache's built-in statistics:
     * <p/>
     * <table>
     * <tr>
     * <td>{@code request-count}</td>
     * <td>The the number of times lookup methods have returned either a
     * cached or uncached value.</td>
     * </tr>
     * <tr>
     * <td>{@code hit-count}</td>
     * <td>The number of times lookup methods have returned a cached value.</td>
     * </tr>
     * <tr>
     * <td>{@code hit-rate}</td>
     * <td>The ratio of cache requests which were hits.</td>
     * </tr>
     * <tr>
     * <td>{@code miss-count}</td>
     * <td>The number of times lookup methods have returned an uncached (newly
     * loaded) value, or null.</td>
     * </tr>
     * <tr>
     * <td>{@code miss-rate}</td>
     * <td>The ratio of cache requests which were misses.</td>
     * </tr>
     * <tr>
     * <td>{@code load-count}</td>
     * <td>The total number of times that lookup methods attempted to load new
     * values.</td>
     * </tr>
     * <tr>
     * <td>{@code load-success-count}</td>
     * <td>The number of times lookup methods have successfully loaded a new
     * value.</td>
     * </tr>
     * <tr>
     * <td>{@code load-exception-count}</td>
     * <td>The number of times lookup methods threw an exception while loading
     * a new value.</td>
     * </tr>
     * <tr>
     * <td>{@code load-exception-rate}</td>
     * <td>The ratio of cache loading attempts which threw exceptions.</td>
     * </tr>
     * <tr>
     * <td>{@code total-load-time}</td>
     * <td>The total number of nanoseconds the cache has spent loading new
     * values.</td>
     * </tr>
     * <tr>
     * <td>{@code average-load-penalty}</td>
     * <td>The average time spent loading new values.</td>
     * </tr>
     * <tr>
     * <td>{@code eviction-count}</td>
     * <td>The number of times an entry has been evicted.</td>
     * </tr>
     * <tr>
     * <td>{@code entries}</td>
     * <td>The approximate number of entries in the cache.</td>
     * </tr>
     * </table>
     *
     * @param cacheName the name of the cache
     * @param cache a {@link Cache} instance
     * @see com.google.common.cache.CacheStats
     */
    public static void instrumentCache(String cacheName, final Cache<?,?> cache) {
        Metrics.newGauge(cache.getClass(), "request-count", cacheName, new Gauge<Long>() {
            @Override
            public Long value() {
                return cache.stats().requestCount();
            }
        });

        Metrics.newGauge(cache.getClass(), "hit-count", cacheName, new Gauge<Long>() {
            @Override
            public Long value() {
                return cache.stats().hitCount();
            }
        });

        Metrics.newGauge(cache.getClass(), "hit-rate", cacheName, new Gauge<Double>() {
            @Override
            public Double value() {
                return cache.stats().hitRate();
            }
        });

        Metrics.newGauge(cache.getClass(), "miss-count", cacheName, new Gauge<Long>() {
            @Override
            public Long value() {
                return cache.stats().missCount();
            }
        });

        Metrics.newGauge(cache.getClass(), "miss-rate", cacheName, new Gauge<Double>() {
            @Override
            public Double value() {
                return cache.stats().missRate();
            }
        });

        Metrics.newGauge(cache.getClass(), "load-count", cacheName, new Gauge<Long>() {
            @Override
            public Long value() {
                return cache.stats().loadCount();
            }
        });

        Metrics.newGauge(cache.getClass(), "load-success-count", cacheName, new Gauge<Long>() {
            @Override
            public Long value() {
                return cache.stats().loadSuccessCount();
            }
        });

        Metrics.newGauge(cache.getClass(), "load-exception-count", cacheName, new Gauge<Long>() {
            @Override
            public Long value() {
                return cache.stats().loadExceptionCount();
            }
        });

        Metrics.newGauge(cache.getClass(), "load-exception-rate", cacheName, new Gauge<Double>() {
            @Override
            public Double value() {
                return cache.stats().loadExceptionRate();
            }
        });

        Metrics.newGauge(cache.getClass(), "total-load-time", cacheName, new Gauge<Long>() {
            @Override
            public Long value() {
                return cache.stats().totalLoadTime();
            }
        });

        Metrics.newGauge(cache.getClass(), "average-load-penalty", cacheName, new Gauge<Double>() {
            @Override
            public Double value() {
                return cache.stats().averageLoadPenalty();
            }
        });

        Metrics.newGauge(cache.getClass(), "eviction-count", cacheName, new Gauge<Long>() {
            @Override
            public Long value() {
                return cache.stats().evictionCount();
            }
        });

        Metrics.newGauge(cache.getClass(), "entries", cacheName, new Gauge<Long>() {
            @Override
            public Long value() {
                return cache.size();
            }
        });
    }

    /**
     * Instruments the given {@link Cache} instance with a get timer
     * and a set of gauges for Cache's built-in statistics.
     *
     * @param cacheName the name of the cache
     * @param cache a {@link Cache} instance
     * @return an instrumented decorator for {@code cache}
     * @see #instrumentCache
     */
    public static <K,V> Cache<K,V> instrument(String cacheName, final Cache<K,V> cache) {
        instrumentCache(cacheName, cache);
        return new InstrumentedCache(cacheName, cache);
    }

    private final Cache<K,V> underlyingCache;
    private final Timer getTimer;

    private InstrumentedCache(String cacheName, Cache<K,V> cache) {
        this.underlyingCache = cache;
        this.getTimer = Metrics.newTimer(cache.getClass(), "get", cacheName, TimeUnit.MICROSECONDS, TimeUnit.SECONDS);
    }

    protected Cache<K,V> delegate() {
        return underlyingCache;
    }

    @Override
    public V getIfPresent(K key) {
        final long start = System.nanoTime();
        try {
            return underlyingCache.getIfPresent(key);
        } finally {
            getTimer.update(System.nanoTime() - start, TimeUnit.NANOSECONDS);
        }
    }
}
