package com.yammer.metrics.guava;

import com.google.common.cache.Cache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.yammer.metrics.core.Meter;
import com.yammer.metrics.core.MetricsRegistry;

import java.util.concurrent.TimeUnit;

/**
 * An instrumented {@link RemovalListener} implementation which records the number and rate of cache
 * evictions.
 *
 * @param <K>    the type of keys in the cache
 * @param <V>    the type of values in the cache
 */
public class InstrumentedRemovalListener<K, V> implements RemovalListener<K, V> {
    /**
     * Returns a {@link RemovalListener} which measures cache evictions.
     *
     * @param registry     a {@link MetricsRegistry}
     * @param cacheName    the name of the cache
     * @param rateUnit     the rate unit to measure misses in
     * @param <K>          the type of keys in the cache
     * @param <V>          the type of values in the cache
     * @return an instrumented {@link RemovalListener}
     */
    public static <K, V> RemovalListener<K, V> newListener(MetricsRegistry registry, String cacheName, TimeUnit rateUnit) {
        final Meter evictions = registry.newMeter(Cache.class, "cache-evictions", cacheName, "evictions", rateUnit);
        return new InstrumentedRemovalListener<K, V>(evictions);
    }

    private final Meter meter;

    private InstrumentedRemovalListener(Meter meter) {
        this.meter = meter;
    }

    @Override
    public void onRemoval(RemovalNotification<K, V> notification) {
        meter.mark();
    }
}
