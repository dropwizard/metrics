package com.yammer.metrics.ehcache;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.core.TimerContext;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.Statistics;
import net.sf.ehcache.constructs.EhcacheDecoratorAdapter;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

/**
 * An instrumented {@link Ehcache} instance.
 */
public class InstrumentedEhcache extends EhcacheDecoratorAdapter {
    /**
     * Instruments the given {@link Ehcache} instance with get and put timers
     * and a set of gauges for Ehcache's built-in statistics:
     * <p/>
     * <table>
     * <tr>
     * <td>{@code hits}</td>
     * <td>The number of times a requested item was found in the
     * cache.</td>
     * </tr>
     * <tr>
     * <td>{@code in-memory-hits}</td>
     * <td>Number of times a requested item was found in the memory
     * store.</td>
     * </tr>
     * <tr>
     * <td>{@code off-heap-hits}</td>
     * <td>Number of times a requested item was found in the off-heap
     * store.</td>
     * </tr>
     * <tr>
     * <td>{@code on-disk-hits}</td>
     * <td>Number of times a requested item was found in the disk
     * store.</td>
     * </tr>
     * <tr>
     * <td>{@code misses}</td>
     * <td>Number of times a requested item was not found in the
     * cache.</td>
     * </tr>
     * <tr>
     * <td>{@code in-memory-misses}</td>
     * <td>Number of times a requested item was not found in the memory
     * store.</td>
     * </tr>
     * <tr>
     * <td>{@code off-heap-misses}</td>
     * <td>Number of times a requested item was not found in the
     * off-heap store.</td>
     * </tr>
     * <tr>
     * <td>{@code on-disk-misses}</td>
     * <td>Number of times a requested item was not found in the disk
     * store.</td>
     * </tr>
     * <tr>
     * <td>{@code objects}</td>
     * <td>Number of elements stored in the cache.</td>
     * </tr>
     * <tr>
     * <td>{@code in-memory-objects}</td>
     * <td>Number of objects in the memory store.</td>
     * </tr>
     * <tr>
     * <td>{@code off-heap-objects}</td>
     * <td>Number of objects in the off-heap store.</td>
     * </tr>
     * <tr>
     * <td>{@code on-disk-objects}</td>
     * <td>Number of objects in the disk store.</td>
     * </tr>
     * <tr>
     * <td>{@code mean-get-time}</td>
     * <td>The average get time. Because ehcache support JDK1.4.2, each
     * get time uses {@link System#currentTimeMillis()}, rather than
     * nanoseconds. The accuracy is thus limited.</td>
     * </tr>
     * <tr>
     * <td>{@code mean-search-time}</td>
     * <td>The average execution time (in milliseconds) within the last
     * sample period.</td>
     * </tr>
     * <tr>
     * <td>{@code eviction-count}</td>
     * <td>The number of cache evictions, since the cache was created,
     * or statistics were cleared.</td>
     * </tr>
     * <tr>
     * <td>{@code searches-per-second}</td>
     * <td>The number of search executions that have completed in the
     * last second.</td>
     * </tr>
     * <tr>
     * <td>{@code accuracy}</td>
     * <td>A human readable description of the accuracy setting. One of
     * "None", "Best Effort" or "Guaranteed".</td>
     * </tr>
     * </table>
     *
     * <b>N.B.: This enables Ehcache's sampling statistics with an accuracy
     * level of "none."</b>
     *
     * @param cache an {@link Ehcache} instance
     * @return an instrumented decorator for {@code cache}
     * @see Statistics
     */
    public static Ehcache instrument(final Ehcache cache) {
        cache.setSampledStatisticsEnabled(true);
        cache.setStatisticsAccuracy(Statistics.STATISTICS_ACCURACY_NONE);

        Metrics.newGauge(cache.getClass(), "hits", cache.getName(), new Gauge<Long>() {
            @Override
            public Long value() {
                return cache.getStatistics().getCacheHits();
            }
        });

        Metrics.newGauge(cache.getClass(), "in-memory-hits", cache.getName(), new Gauge<Long>() {
            @Override
            public Long value() {
                return cache.getStatistics().getInMemoryHits();
            }
        });

        Metrics.newGauge(cache.getClass(), "off-heap-hits", cache.getName(), new Gauge<Long>() {
            @Override
            public Long value() {
                return cache.getStatistics().getOffHeapHits();
            }
        });

        Metrics.newGauge(cache.getClass(), "on-disk-hits", cache.getName(), new Gauge<Long>() {
            @Override
            public Long value() {
                return cache.getStatistics().getOnDiskHits();
            }
        });

        Metrics.newGauge(cache.getClass(), "misses", cache.getName(), new Gauge<Long>() {
            @Override
            public Long value() {
                return cache.getStatistics().getCacheMisses();
            }
        });

        Metrics.newGauge(cache.getClass(), "in-memory-misses", cache.getName(), new Gauge<Long>() {
            @Override
            public Long value() {
                return cache.getStatistics().getInMemoryMisses();
            }
        });

        Metrics.newGauge(cache.getClass(), "off-heap-misses", cache.getName(), new Gauge<Long>() {
            @Override
            public Long value() {
                return cache.getStatistics().getOffHeapMisses();
            }
        });

        Metrics.newGauge(cache.getClass(), "on-disk-misses", cache.getName(), new Gauge<Long>() {
            @Override
            public Long value() {
                return cache.getStatistics().getOnDiskMisses();
            }
        });

        Metrics.newGauge(cache.getClass(), "objects", cache.getName(), new Gauge<Long>() {
            @Override
            public Long value() {
                return cache.getStatistics().getObjectCount();
            }
        });

        Metrics.newGauge(cache.getClass(), "in-memory-objects", cache.getName(), new Gauge<Long>() {
            @Override
            public Long value() {
                return cache.getStatistics().getMemoryStoreObjectCount();
            }
        });

        Metrics.newGauge(cache.getClass(), "off-heap-objects", cache.getName(), new Gauge<Long>() {
            @Override
            public Long value() {
                return cache.getStatistics().getOffHeapStoreObjectCount();
            }
        });

        Metrics.newGauge(cache.getClass(), "on-disk-objects", cache.getName(), new Gauge<Long>() {
            @Override
            public Long value() {
                return cache.getStatistics().getDiskStoreObjectCount();
            }
        });

        Metrics.newGauge(cache.getClass(), "mean-get-time", cache.getName(), new Gauge<Float>() {
            @Override
            public Float value() {
                return cache.getStatistics().getAverageGetTime();
            }
        });

        Metrics.newGauge(cache.getClass(), "mean-search-time", cache.getName(), new Gauge<Long>() {
            @Override
            public Long value() {
                return cache.getStatistics().getAverageSearchTime();
            }
        });

        Metrics.newGauge(cache.getClass(), "eviction-count", cache.getName(), new Gauge<Long>() {
            @Override
            public Long value() {
                return cache.getStatistics().getEvictionCount();
            }
        });

        Metrics.newGauge(cache.getClass(), "searches-per-second", cache.getName(), new Gauge<Long>() {
            @Override
            public Long value() {
                return cache.getStatistics().getSearchesPerSecond();
            }
        });

        Metrics.newGauge(cache.getClass(), "writer-queue-size", cache.getName(), new Gauge<Long>() {
            @Override
            public Long value() {
                return cache.getStatistics().getWriterQueueSize();
            }
        });

        Metrics.newGauge(cache.getClass(), "accuracy", cache.getName(), new Gauge<String>() {
            @Override
            public String value() {
                return cache.getStatistics().getStatisticsAccuracyDescription();
            }
        });

        return new InstrumentedEhcache(cache);
    }

    private final Timer getTimer, putTimer;

    private InstrumentedEhcache(Ehcache cache) {
        super(cache);
        this.getTimer = Metrics.newTimer(cache.getClass(), "get", cache.getName(), TimeUnit.MICROSECONDS, TimeUnit.SECONDS);
        this.putTimer = Metrics.newTimer(cache.getClass(), "put", cache.getName(), TimeUnit.MICROSECONDS, TimeUnit.SECONDS);
    }

    @Override
    public Element get(Object key) throws IllegalStateException, CacheException {
        final TimerContext ctx = getTimer.time();
        try {
            return underlyingCache.get(key);
        } finally {
            ctx.stop();
        }
    }

    @Override
    public Element get(Serializable key) throws IllegalStateException, CacheException {
        final TimerContext ctx = getTimer.time();
        try {
            return underlyingCache.get(key);
        } finally {
            ctx.stop();
        }
    }

    @Override
    public void put(Element element) throws IllegalArgumentException, IllegalStateException, CacheException {
        final TimerContext ctx = putTimer.time();
        try {
            underlyingCache.put(element);
        } finally {
            ctx.stop();
        }
    }

    @Override
    public void put(Element element, boolean doNotNotifyCacheReplicators) throws IllegalArgumentException, IllegalStateException, CacheException {
        final TimerContext ctx = putTimer.time();
        try {
            underlyingCache.put(element, doNotNotifyCacheReplicators);
        } finally {
            ctx.stop();
        }
    }

    @Override
    public Element putIfAbsent(Element element) throws NullPointerException {
        final TimerContext ctx = putTimer.time();
        try {
            return underlyingCache.putIfAbsent(element);
        } finally {
            ctx.stop();
        }
    }
}
