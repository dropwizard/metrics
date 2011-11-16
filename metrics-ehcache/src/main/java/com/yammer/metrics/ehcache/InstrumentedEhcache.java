package com.yammer.metrics.ehcache;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.GaugeMetric;
import com.yammer.metrics.core.TimerMetric;
import net.sf.ehcache.*;
import net.sf.ehcache.bootstrap.BootstrapCacheLoader;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.event.RegisteredEventListeners;
import net.sf.ehcache.exceptionhandler.CacheExceptionHandler;
import net.sf.ehcache.extension.CacheExtension;
import net.sf.ehcache.loader.CacheLoader;
import net.sf.ehcache.search.Attribute;
import net.sf.ehcache.search.Query;
import net.sf.ehcache.statistics.CacheUsageListener;
import net.sf.ehcache.statistics.LiveCacheStatistics;
import net.sf.ehcache.statistics.sampled.SampledCacheStatistics;
import net.sf.ehcache.terracotta.TerracottaNotRunningException;
import net.sf.ehcache.transaction.manager.TransactionManagerLookup;
import net.sf.ehcache.writer.CacheWriter;
import net.sf.ehcache.writer.CacheWriterManager;

import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * An instrumented {@link Ehcache} instance.
 */
@SuppressWarnings({"deprecation", "CloneDoesntCallSuperClone"})
public class InstrumentedEhcache implements Ehcache {
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

        Metrics.newGauge(cache.getClass(), "hits", cache.getName(), new GaugeMetric<Long>() {
            @Override
            public Long value() {
                return cache.getStatistics().getCacheHits();
            }
        });

        Metrics.newGauge(cache.getClass(), "in-memory-hits", cache.getName(), new GaugeMetric<Long>() {
            @Override
            public Long value() {
                return cache.getStatistics().getInMemoryHits();
            }
        });

        Metrics.newGauge(cache.getClass(), "off-heap-hits", cache.getName(), new GaugeMetric<Long>() {
            @Override
            public Long value() {
                return cache.getStatistics().getOffHeapHits();
            }
        });

        Metrics.newGauge(cache.getClass(), "on-disk-hits", cache.getName(), new GaugeMetric<Long>() {
            @Override
            public Long value() {
                return cache.getStatistics().getOnDiskHits();
            }
        });

        Metrics.newGauge(cache.getClass(), "misses", cache.getName(), new GaugeMetric<Long>() {
            @Override
            public Long value() {
                return cache.getStatistics().getCacheMisses();
            }
        });

        Metrics.newGauge(cache.getClass(), "in-memory-misses", cache.getName(), new GaugeMetric<Long>() {
            @Override
            public Long value() {
                return cache.getStatistics().getInMemoryMisses();
            }
        });

        Metrics.newGauge(cache.getClass(), "off-heap-misses", cache.getName(), new GaugeMetric<Long>() {
            @Override
            public Long value() {
                return cache.getStatistics().getOffHeapMisses();
            }
        });

        Metrics.newGauge(cache.getClass(), "on-disk-misses", cache.getName(), new GaugeMetric<Long>() {
            @Override
            public Long value() {
                return cache.getStatistics().getOnDiskMisses();
            }
        });

        Metrics.newGauge(cache.getClass(), "objects", cache.getName(), new GaugeMetric<Long>() {
            @Override
            public Long value() {
                return cache.getStatistics().getObjectCount();
            }
        });

        Metrics.newGauge(cache.getClass(), "in-memory-objects", cache.getName(), new GaugeMetric<Long>() {
            @Override
            public Long value() {
                return cache.getStatistics().getMemoryStoreObjectCount();
            }
        });

        Metrics.newGauge(cache.getClass(), "off-heap-objects", cache.getName(), new GaugeMetric<Long>() {
            @Override
            public Long value() {
                return cache.getStatistics().getOffHeapStoreObjectCount();
            }
        });

        Metrics.newGauge(cache.getClass(), "on-disk-objects", cache.getName(), new GaugeMetric<Long>() {
            @Override
            public Long value() {
                return cache.getStatistics().getDiskStoreObjectCount();
            }
        });

        Metrics.newGauge(cache.getClass(), "mean-get-time", cache.getName(), new GaugeMetric<Float>() {
            @Override
            public Float value() {
                return cache.getStatistics().getAverageGetTime();
            }
        });

        Metrics.newGauge(cache.getClass(), "mean-search-time", cache.getName(), new GaugeMetric<Long>() {
            @Override
            public Long value() {
                return cache.getStatistics().getAverageSearchTime();
            }
        });

        Metrics.newGauge(cache.getClass(), "eviction-count", cache.getName(), new GaugeMetric<Long>() {
            @Override
            public Long value() {
                return cache.getStatistics().getEvictionCount();
            }
        });

        Metrics.newGauge(cache.getClass(), "searches-per-second", cache.getName(), new GaugeMetric<Long>() {
            @Override
            public Long value() {
                return cache.getStatistics().getSearchesPerSecond();
            }
        });

        Metrics.newGauge(cache.getClass(), "writer-queue-size", cache.getName(), new GaugeMetric<Long>() {
            @Override
            public Long value() {
                return cache.getStatistics().getWriterQueueSize();
            }
        });

        Metrics.newGauge(cache.getClass(), "accuracy", cache.getName(), new GaugeMetric<String>() {
            @Override
            public String value() {
                return cache.getStatistics().getStatisticsAccuracyDescription();
            }
        });

        return new InstrumentedEhcache(cache);
    }

    private final TimerMetric getTimer, putTimer;
    private final Ehcache cache;

    private InstrumentedEhcache(Ehcache cache) {
        this.cache = cache;
        this.getTimer = Metrics.newTimer(cache.getClass(), "get", cache.getName(), TimeUnit.MICROSECONDS, TimeUnit.SECONDS);
        this.putTimer = Metrics.newTimer(cache.getClass(), "put", cache.getName(), TimeUnit.MICROSECONDS, TimeUnit.SECONDS);
    }

    @Override
    public void unregisterCacheLoader(CacheLoader cacheLoader) {
        cache.unregisterCacheLoader(cacheLoader);
    }

    @Override
    public void acquireReadLockOnKey(Object key) {
        cache.acquireReadLockOnKey(key);
    }

    @Override
    public void acquireWriteLockOnKey(Object key) {
        cache.acquireWriteLockOnKey(key);
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        cache.addPropertyChangeListener(listener);
    }

    @Override
    public void bootstrap() {
        cache.bootstrap();
    }

    @Override
    public long calculateInMemorySize() throws IllegalStateException, CacheException {
        return cache.calculateInMemorySize();
    }

    @Override
    public long calculateOffHeapSize() throws IllegalStateException, CacheException {
        return cache.calculateOffHeapSize();
    }

    @Override
    public void clearStatistics() {
        cache.clearStatistics();
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return InstrumentedEhcache.instrument((Ehcache) cache.clone());
    }

    @Override
    public Query createQuery() {
        return cache.createQuery();
    }

    @Override
    public void disableDynamicFeatures() {
        cache.disableDynamicFeatures();
    }

    @Override
    public void dispose() throws IllegalStateException {
        cache.dispose();
    }

    @Override
    public void evictExpiredElements() {
        cache.evictExpiredElements();
    }

    @Override
    public void flush() throws IllegalStateException, CacheException {
        cache.flush();
    }

    @Override
    public Element get(Object key) throws IllegalStateException, CacheException {
        final long start = System.nanoTime();
        try {
            return cache.get(key);
        } finally {
            getTimer.update(System.nanoTime() - start, TimeUnit.NANOSECONDS);
        }
    }

    @Override
    public Element get(Serializable key) throws IllegalStateException, CacheException {
        final long start = System.nanoTime();
        try {
            return cache.get(key);
        } finally {
            getTimer.update(System.nanoTime() - start, TimeUnit.NANOSECONDS);
        }
    }

    @Override
    public Map getAllWithLoader(Collection keys, Object loaderArgument) throws CacheException {
        return cache.getAllWithLoader(keys, loaderArgument);
    }

    @Override
    public float getAverageGetTime() {
        return cache.getAverageGetTime();
    }

    @Override
    public long getAverageSearchTime() {
        return cache.getAverageSearchTime();
    }

    @Override
    public BootstrapCacheLoader getBootstrapCacheLoader() {
        return cache.getBootstrapCacheLoader();
    }

    @Override
    public CacheConfiguration getCacheConfiguration() {
        return cache.getCacheConfiguration();
    }

    @Override
    public RegisteredEventListeners getCacheEventNotificationService() {
        return cache.getCacheEventNotificationService();
    }

    @Override
    public CacheExceptionHandler getCacheExceptionHandler() {
        return cache.getCacheExceptionHandler();
    }

    @Override
    public CacheManager getCacheManager() {
        return cache.getCacheManager();
    }

    @Override
    public int getDiskStoreSize() throws IllegalStateException {
        return cache.getDiskStoreSize();
    }

    @Override
    public String getGuid() {
        return cache.getGuid();
    }

    @Override
    public Object getInternalContext() {
        return cache.getInternalContext();
    }

    @Override
    public List getKeys() throws IllegalStateException, CacheException {
        return cache.getKeys();
    }

    @Override
    public List getKeysNoDuplicateCheck() throws IllegalStateException {
        return cache.getKeysNoDuplicateCheck();
    }

    @Override
    public List getKeysWithExpiryCheck() throws IllegalStateException, CacheException {
        return cache.getKeysWithExpiryCheck();
    }

    @Override
    public LiveCacheStatistics getLiveCacheStatistics() throws IllegalStateException {
        return cache.getLiveCacheStatistics();
    }

    @Override
    public long getMemoryStoreSize() throws IllegalStateException {
        return cache.getMemoryStoreSize();
    }

    @Override
    public String getName() {
        return cache.getName();
    }

    @Override
    public long getOffHeapStoreSize() throws IllegalStateException {
        return cache.getOffHeapStoreSize();
    }

    @Override
    public Element getQuiet(Object key) throws IllegalStateException, CacheException {
        return cache.getQuiet(key);
    }

    @Override
    public Element getQuiet(Serializable key) throws IllegalStateException, CacheException {
        return cache.getQuiet(key);
    }

    @Override
    public List<CacheExtension> getRegisteredCacheExtensions() {
        return cache.getRegisteredCacheExtensions();
    }

    @Override
    public List<CacheLoader> getRegisteredCacheLoaders() {
        return cache.getRegisteredCacheLoaders();
    }

    @Override
    public CacheWriter getRegisteredCacheWriter() {
        return cache.getRegisteredCacheWriter();
    }

    @Override
    public SampledCacheStatistics getSampledCacheStatistics() {
        return cache.getSampledCacheStatistics();
    }

    @Override
    public <T> Attribute<T> getSearchAttribute(String attributeName) throws CacheException {
        return cache.getSearchAttribute(attributeName);
    }

    @Override
    public long getSearchesPerSecond() {
        return cache.getSearchesPerSecond();
    }

    @Override
    public int getSize() throws IllegalStateException, CacheException {
        return cache.getSize();
    }

    @Override
    public int getSizeBasedOnAccuracy(int statisticsAccuracy) throws IllegalArgumentException, IllegalStateException, CacheException {
        return cache.getSizeBasedOnAccuracy(statisticsAccuracy);
    }

    @Override
    public Statistics getStatistics() throws IllegalStateException {
        return cache.getStatistics();
    }

    @Override
    public int getStatisticsAccuracy() {
        return cache.getStatisticsAccuracy();
    }

    @Override
    public Status getStatus() {
        return cache.getStatus();
    }

    @Override
    public Element getWithLoader(Object key, CacheLoader loader, Object loaderArgument) throws CacheException {
        return cache.getWithLoader(key, loader, loaderArgument);
    }

    @Override
    public CacheWriterManager getWriterManager() {
        return cache.getWriterManager();
    }

    @Override
    public void initialise() {
        cache.initialise();
    }

    @Override
    public boolean isClusterBulkLoadEnabled() throws UnsupportedOperationException, TerracottaNotRunningException {
        return cache.isClusterBulkLoadEnabled();
    }

    @Override
    @Deprecated
    public boolean isClusterCoherent() throws TerracottaNotRunningException {
        return cache.isClusterCoherent();
    }

    @Override
    public boolean isDisabled() {
        return cache.isDisabled();
    }

    @Override
    public boolean isElementInMemory(Object key) {
        return cache.isElementInMemory(key);
    }

    @Override
    public boolean isElementInMemory(Serializable key) {
        return cache.isElementInMemory(key);
    }

    @Override
    public boolean isElementOnDisk(Object key) {
        return cache.isElementOnDisk(key);
    }

    @Override
    public boolean isElementOnDisk(Serializable key) {
        return cache.isElementOnDisk(key);
    }

    @Override
    public boolean isExpired(Element element) throws IllegalStateException, NullPointerException {
        return cache.isExpired(element);
    }

    @Override
    public boolean isKeyInCache(Object key) {
        return cache.isKeyInCache(key);
    }

    @Override
    public boolean isNodeBulkLoadEnabled() throws UnsupportedOperationException, TerracottaNotRunningException {
        return cache.isNodeBulkLoadEnabled();
    }

    @Override
    @Deprecated
    public boolean isNodeCoherent() throws TerracottaNotRunningException {
        return cache.isNodeCoherent();
    }

    @Override
    public boolean isReadLockedByCurrentThread(Object key) {
        return cache.isReadLockedByCurrentThread(key);
    }

    @Override
    public boolean isSampledStatisticsEnabled() {
        return cache.isSampledStatisticsEnabled();
    }

    @Override
    public boolean isSearchable() {
        return cache.isSearchable();
    }

    @Override
    public boolean isStatisticsEnabled() {
        return cache.isStatisticsEnabled();
    }

    @Override
    public boolean isValueInCache(Object value) {
        return cache.isValueInCache(value);
    }

    @Override
    public boolean isWriteLockedByCurrentThread(Object key) {
        return cache.isWriteLockedByCurrentThread(key);
    }

    @Override
    public void load(Object key) throws CacheException {
        cache.load(key);
    }

    @Override
    public void loadAll(Collection keys, Object argument) throws CacheException {
        cache.loadAll(keys, argument);
    }

    @Override
    public void unpinAll() {
        cache.unpinAll();
    }

    @Override
    public boolean isPinned(Object o) {
        return cache.isPinned(o);
    }

    @Override
    public void setPinned(Object o, boolean b) {
        cache.setPinned(o, b);
    }

    @Override
    public void putAll(Collection<Element> elements) throws IllegalArgumentException, IllegalStateException, CacheException {
        cache.putAll(elements);
    }

    @Override
    public Map<Object, Element> getAll(Collection<?> objects) throws IllegalStateException, CacheException, NullPointerException {
        return cache.getAll(objects);
    }

    @Override
    public void removeAll(Collection<?> objects) throws IllegalStateException, NullPointerException {
        cache.removeAll(objects);
    }

    @Override
    public void removeAll(Collection<?> objects, boolean b) throws IllegalStateException, NullPointerException {
        cache.removeAll(objects,  b);
    }

    @Override
    public long calculateOnDiskSize() throws IllegalStateException, CacheException {
        return cache.calculateOnDiskSize();
    }

    @Override
    public boolean hasAbortedSizeOf() {
        return cache.hasAbortedSizeOf();
    }

    @Override
    public void put(Element element) throws IllegalArgumentException, IllegalStateException, CacheException {
        final long start = System.nanoTime();
        try {
            cache.put(element);
        } finally {
            putTimer.update(System.nanoTime() - start, TimeUnit.NANOSECONDS);
        }
    }

    @Override
    public void put(Element element, boolean doNotNotifyCacheReplicators) throws IllegalArgumentException, IllegalStateException, CacheException {
        final long start = System.nanoTime();
        try {
            cache.put(element, doNotNotifyCacheReplicators);
        } finally {
            putTimer.update(System.nanoTime() - start, TimeUnit.NANOSECONDS);
        }
    }

    @Override
    public Element putIfAbsent(Element element) throws NullPointerException {
        final long start = System.nanoTime();
        try {
            return cache.putIfAbsent(element);
        } finally {
            putTimer.update(System.nanoTime() - start, TimeUnit.NANOSECONDS);
        }
    }

    @Override
    public void putQuiet(Element element) throws IllegalArgumentException, IllegalStateException, CacheException {
        cache.putQuiet(element);
    }

    @Override
    public void putWithWriter(Element element) throws IllegalArgumentException, IllegalStateException, CacheException {
        cache.putWithWriter(element);
    }

    @Override
    public void registerCacheExtension(CacheExtension cacheExtension) {
        cache.registerCacheExtension(cacheExtension);
    }

    @Override
    public void registerCacheLoader(CacheLoader cacheLoader) {
        cache.registerCacheLoader(cacheLoader);
    }

    @Override
    public void registerCacheUsageListener(CacheUsageListener cacheUsageListener) throws IllegalStateException {
        cache.registerCacheUsageListener(cacheUsageListener);
    }

    @Override
    public void registerCacheWriter(CacheWriter cacheWriter) {
        cache.registerCacheWriter(cacheWriter);
    }

    @Override
    public void releaseReadLockOnKey(Object key) {
        cache.releaseReadLockOnKey(key);
    }

    @Override
    public void releaseWriteLockOnKey(Object key) {
        cache.releaseWriteLockOnKey(key);
    }

    @Override
    public boolean remove(Object key) throws IllegalStateException {
        return cache.remove(key);
    }

    @Override
    public boolean remove(Object key, boolean doNotNotifyCacheReplicators) throws IllegalStateException {
        return cache.remove(key, doNotNotifyCacheReplicators);
    }

    @Override
    public boolean remove(Serializable key) throws IllegalStateException {
        return cache.remove(key);
    }

    @Override
    public boolean remove(Serializable key, boolean doNotNotifyCacheReplicators) throws IllegalStateException {
        return cache.remove(key, doNotNotifyCacheReplicators);
    }

    @Override
    public void removeAll() throws IllegalStateException, CacheException {
        cache.removeAll();
    }

    @Override
    public void removeAll(boolean doNotNotifyCacheReplicators) throws IllegalStateException, CacheException {
        cache.removeAll(doNotNotifyCacheReplicators);
    }

    @Override
    public void removeCacheUsageListener(CacheUsageListener cacheUsageListener) throws IllegalStateException {
        cache.removeCacheUsageListener(cacheUsageListener);
    }

    @Override
    public boolean removeElement(Element element) throws NullPointerException {
        return cache.removeElement(element);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        cache.removePropertyChangeListener(listener);
    }

    @Override
    public boolean removeQuiet(Object key) throws IllegalStateException {
        return cache.removeQuiet(key);
    }

    @Override
    public boolean removeQuiet(Serializable key) throws IllegalStateException {
        return cache.removeQuiet(key);
    }

    @Override
    public boolean removeWithWriter(Object key) throws IllegalStateException, CacheException {
        return cache.removeWithWriter(key);
    }

    @Override
    public Element replace(Element element) throws NullPointerException {
        return cache.replace(element);
    }

    @Override
    public boolean replace(Element old, Element element) throws NullPointerException, IllegalArgumentException {
        return cache.replace(old, element);
    }

    @Override
    public void setBootstrapCacheLoader(BootstrapCacheLoader bootstrapCacheLoader) throws CacheException {
        cache.setBootstrapCacheLoader(bootstrapCacheLoader);
    }

    @Override
    public void setCacheExceptionHandler(CacheExceptionHandler cacheExceptionHandler) {
        cache.setCacheExceptionHandler(cacheExceptionHandler);
    }

    @Override
    public void setCacheManager(CacheManager cacheManager) {
        cache.setCacheManager(cacheManager);
    }

    @Override
    public void setDisabled(boolean disabled) {
        cache.setDisabled(disabled);
    }

    @Override
    public void setDiskStorePath(String diskStorePath) throws CacheException {
        cache.setDiskStorePath(diskStorePath);
    }

    @Override
    public void setName(String name) {
        cache.setName(name);
    }

    @Override
    public void setNodeBulkLoadEnabled(boolean enabledBulkLoad) throws UnsupportedOperationException, TerracottaNotRunningException {
        cache.setNodeBulkLoadEnabled(enabledBulkLoad);
    }

    @Override
    @Deprecated
    public void setNodeCoherent(boolean coherent) throws UnsupportedOperationException, TerracottaNotRunningException {
        cache.setNodeCoherent(coherent);
    }

    @Override
    public void setSampledStatisticsEnabled(boolean enableStatistics) {
        cache.setSampledStatisticsEnabled(enableStatistics);
    }

    @Override
    public void setStatisticsAccuracy(int statisticsAccuracy) {
        cache.setStatisticsAccuracy(statisticsAccuracy);
    }

    @Override
    public void setStatisticsEnabled(boolean enableStatistics) {
        cache.setStatisticsEnabled(enableStatistics);
    }

    @Override
    public void setTransactionManagerLookup(TransactionManagerLookup transactionManagerLookup) {
        cache.setTransactionManagerLookup(transactionManagerLookup);
    }

    @Override
    public String toString() {
        return cache.toString();
    }

    @Override
    public boolean tryReadLockOnKey(Object key, long timeout) throws InterruptedException {
        return cache.tryReadLockOnKey(key, timeout);
    }

    @Override
    public boolean tryWriteLockOnKey(Object key, long timeout) throws InterruptedException {
        return cache.tryWriteLockOnKey(key, timeout);
    }

    @Override
    public void unregisterCacheExtension(CacheExtension cacheExtension) {
        cache.unregisterCacheExtension(cacheExtension);
    }

    @Override
    public void unregisterCacheWriter() {
        cache.unregisterCacheWriter();
    }

    @Override
    public void waitUntilClusterBulkLoadComplete() throws UnsupportedOperationException, TerracottaNotRunningException {
        cache.waitUntilClusterBulkLoadComplete();
    }

    @Override
    @Deprecated
    public void waitUntilClusterCoherent() throws UnsupportedOperationException, TerracottaNotRunningException {
        cache.waitUntilClusterCoherent();
    }
}
