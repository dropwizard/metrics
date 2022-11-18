package io.dropwizard.metrics5.ehcache;

import io.dropwizard.metrics5.MetricName;
import io.dropwizard.metrics5.MetricRegistry;
import io.dropwizard.metrics5.Timer;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.constructs.EhcacheDecoratorAdapter;
import net.sf.ehcache.statistics.StatisticsGateway;

import java.io.Serializable;

import static io.dropwizard.metrics5.MetricRegistry.name;

/**
 * An instrumented {@link Ehcache} instance.
 */
public class InstrumentedEhcache extends EhcacheDecoratorAdapter {
    /**
     * Instruments the given {@link Ehcache} instance with get and put timers
     * and a set of gauges for Ehcache's built-in statistics:
     * <p>
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
     * <p>
     * <b>N.B.: This enables Ehcache's sampling statistics with an accuracy
     * level of "none."</b>
     *
     * @param cache    an {@link Ehcache} instance
     * @param registry a {@link MetricRegistry}
     * @return an instrumented decorator for {@code cache}
     * @see StatisticsGateway
     */
    public static Ehcache instrument(MetricRegistry registry, final Ehcache cache) {

        final MetricName prefix = name(cache.getClass(), cache.getName());
        registry.registerGauge(prefix.resolve("hits"),
                () -> cache.getStatistics().cacheHitCount());

        registry.registerGauge(prefix.resolve("in-memory-hits"),
                () -> cache.getStatistics().localHeapHitCount());

        registry.registerGauge(prefix.resolve("off-heap-hits"),
                () -> cache.getStatistics().localOffHeapHitCount());

        registry.registerGauge(prefix.resolve("on-disk-hits"),
                () -> cache.getStatistics().localDiskHitCount());

        registry.registerGauge(prefix.resolve("misses"),
                () -> cache.getStatistics().cacheMissCount());

        registry.registerGauge(prefix.resolve("in-memory-misses"),
                () -> cache.getStatistics().localHeapMissCount());

        registry.registerGauge(prefix.resolve("off-heap-misses"),
                () -> cache.getStatistics().localOffHeapMissCount());

        registry.registerGauge(prefix.resolve("on-disk-misses"),
                () -> cache.getStatistics().localDiskMissCount());

        registry.registerGauge(prefix.resolve("objects"),
                () -> cache.getStatistics().getSize());

        registry.registerGauge(prefix.resolve("in-memory-objects"),
                () -> cache.getStatistics().getLocalHeapSize());

        registry.registerGauge(prefix.resolve("off-heap-objects"),
                () -> cache.getStatistics().getLocalOffHeapSize());

        registry.registerGauge(prefix.resolve("on-disk-objects"),
                () -> cache.getStatistics().getLocalDiskSize());

        registry.registerGauge(prefix.resolve("mean-get-time"),
                () -> cache.getStatistics().cacheGetOperation().latency().average().value());

        registry.registerGauge(prefix.resolve("mean-search-time"),
                () -> cache.getStatistics().cacheSearchOperation().latency().average().value());

        registry.registerGauge(prefix.resolve("eviction-count"),
                () -> cache.getStatistics().cacheEvictionOperation().count().value());

        registry.registerGauge(prefix.resolve("searches-per-second"),
                () -> cache.getStatistics().cacheSearchOperation().rate().value());

        registry.registerGauge(prefix.resolve("writer-queue-size"),
                () -> cache.getStatistics().getWriterQueueLength());

        return new InstrumentedEhcache(registry, cache);
    }

    private final Timer getTimer, putTimer;

    private InstrumentedEhcache(MetricRegistry registry, Ehcache cache) {
        super(cache);
        this.getTimer = registry.timer(MetricRegistry.name(cache.getClass(), cache.getName(), "gets"));
        this.putTimer = registry.timer(MetricRegistry.name(cache.getClass(), cache.getName(), "puts"));
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
