package com.codahale.metrics.ehcache;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricName;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.constructs.EhcacheDecoratorAdapter;
import net.sf.ehcache.statistics.StatisticsGateway;

import java.io.Serializable;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * An instrumented {@link Ehcache} instance.
 */
public class InstrumentedEhcache extends EhcacheDecoratorAdapter {
    /**
     * Instruments the given {@link Ehcache} instance with get and put timers
     * and a set of gauges for Ehcache's built-in statistics:
     * <p/>
     * <table>
     * <caption>Ehcache timered metrics</caption>
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
     * @param cache       an {@link Ehcache} instance
     * @param registry    a {@link MetricRegistry}
     * @return an instrumented decorator for {@code cache}
     * @see StatisticsGateway
     */
    public static Ehcache instrument(MetricRegistry registry, final Ehcache cache) {

        final MetricName prefix = name(cache.getClass(), cache.getName());
        registry.register(prefix.resolve("hits"),
                          new Gauge<Long>() {
                              @Override
                              public Long getValue() {
                                  return cache.getStatistics().cacheHitCount();
                              }
                          });

        registry.register(prefix.resolve("in-memory-hits"),
                          new Gauge<Long>() {
                              @Override
                              public Long getValue() {
                                  return cache.getStatistics().localHeapHitCount();
                              }
                          });

        registry.register(prefix.resolve("off-heap-hits"),
                          new Gauge<Long>() {
                              @Override
                              public Long getValue() {
                                  return cache.getStatistics().localOffHeapHitCount();
                              }
                          });

        registry.register(prefix.resolve("on-disk-hits"),
                          new Gauge<Long>() {
                              @Override
                              public Long getValue() {
                                  return cache.getStatistics().localDiskHitCount();
                              }
                          });

        registry.register(prefix.resolve("misses"),
                          new Gauge<Long>() {
                              @Override
                              public Long getValue() {
                                  return cache.getStatistics().cacheMissCount();
                              }
                          });

        registry.register(prefix.resolve("in-memory-misses"),
                          new Gauge<Long>() {
                              @Override
                              public Long getValue() {
                                  return cache.getStatistics().localHeapMissCount();
                              }
                          });

        registry.register(prefix.resolve("off-heap-misses"),
                          new Gauge<Long>() {
                              @Override
                              public Long getValue() {
                                  return cache.getStatistics().localOffHeapMissCount();
                              }
                          });

        registry.register(prefix.resolve("on-disk-misses"),
                          new Gauge<Long>() {
                              @Override
                              public Long getValue() {
                                  return cache.getStatistics().localDiskMissCount();
                              }
                          });

        registry.register(prefix.resolve("objects"),
                          new Gauge<Long>() {
                              @Override
                              public Long getValue() {
                                  return cache.getStatistics().getSize();
                              }
                          });

        registry.register(prefix.resolve("in-memory-objects"),
                          new Gauge<Long>() {
                              @Override
                              public Long getValue() {
                                  return cache.getStatistics().getLocalHeapSize();
                              }
                          });

        registry.register(prefix.resolve("off-heap-objects"),
                          new Gauge<Long>() {
                              @Override
                              public Long getValue() {
                                  return cache.getStatistics().getLocalOffHeapSize();
                              }
                          });

        registry.register(prefix.resolve("on-disk-objects"),
                          new Gauge<Long>() {
                              @Override
                              public Long getValue() {
                                  return cache.getStatistics().getLocalDiskSize();
                              }
                          });

        registry.register(prefix.resolve("mean-get-time"),
                          new Gauge<Double>() {
                              @Override
                              public Double getValue() {
                                  return cache.getStatistics().cacheGetOperation().latency().average().value();
                              }
                          });

        registry.register(prefix.resolve("mean-search-time"),
                          new Gauge<Double>() {
                              @Override
                              public Double getValue() {
                                  return cache.getStatistics().cacheSearchOperation().latency().average().value();
                              }
                          });

        registry.register(prefix.resolve("eviction-count"),
                          new Gauge<Long>() {
                              @Override
                              public Long getValue() {
                                  return cache.getStatistics().cacheEvictionOperation().count().value();
                              }
                          });

        registry.register(prefix.resolve("searches-per-second"),
                          new Gauge<Double>() {
                              @Override
                              public Double getValue() {
                                  return cache.getStatistics().cacheSearchOperation().rate().value();
                              }
                          });

        registry.register(prefix.resolve("writer-queue-size"),
                          new Gauge<Long>() {
                              @Override
                              public Long getValue() {
                                  return cache.getStatistics().getWriterQueueLength();
                              }
                          });

        return new InstrumentedEhcache(registry, cache);
    }

    private final Timer getTimer, putTimer;

    private InstrumentedEhcache(MetricRegistry registry, Ehcache cache) {
        super(cache);
        this.getTimer = registry.timer(name(cache.getClass(), cache.getName(), "gets"));
        this.putTimer = registry.timer(name(cache.getClass(), cache.getName(), "puts"));
    }

    @Override
    public Element get(Object key) throws IllegalStateException, CacheException {
        final Timer.Context ctx = getTimer.time();
        try {
            return underlyingCache.get(key);
        } finally {
            ctx.stop();
        }
    }

    @Override
    public Element get(Serializable key) throws IllegalStateException, CacheException {
        final Timer.Context ctx = getTimer.time();
        try {
            return underlyingCache.get(key);
        } finally {
            ctx.stop();
        }
    }

    @Override
    public void put(Element element) throws IllegalArgumentException, IllegalStateException, CacheException {
        final Timer.Context ctx = putTimer.time();
        try {
            underlyingCache.put(element);
        } finally {
            ctx.stop();
        }
    }

    @Override
    public void put(Element element, boolean doNotNotifyCacheReplicators) throws IllegalArgumentException, IllegalStateException, CacheException {
        final Timer.Context ctx = putTimer.time();
        try {
            underlyingCache.put(element, doNotNotifyCacheReplicators);
        } finally {
            ctx.stop();
        }
    }

    @Override
    public Element putIfAbsent(Element element) throws NullPointerException {
        final Timer.Context ctx = putTimer.time();
        try {
            return underlyingCache.putIfAbsent(element);
        } finally {
            ctx.stop();
        }
    }
}
