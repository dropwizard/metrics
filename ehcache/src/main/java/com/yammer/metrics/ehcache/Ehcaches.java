package com.yammer.metrics.ehcache;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.GaugeMetric;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Statistics;

/**
 * A class for monitoring {@link Ehcache} instances.
 */
public class Ehcaches {
    private Ehcaches() { /* unused */ }

    /**
     * The suggested accuracy level for
     * {@link net.sf.ehcache.config.CacheConfiguration}s
     */
    public static final int SUGGESTED_ACCURACY_LEVEL = Statistics.STATISTICS_ACCURACY_NONE;

    /**
     * Creates gauges for the provided {@link Ehcache} instance:
     *
     * <table>
     *     <tr>
     *         <td>{@code hits}</td>
     *         <td>The number of times a requested item was found in the
     *         cache.</td>
     *     </tr>
     *     <tr>
     *         <td>{@code in-memory-hits}</td>
     *         <td>Number of times a requested item was found in the memory
     *         store.</td>
     *     </tr>
     *     <tr>
     *         <td>{@code off-heap-hits}</td>
     *         <td>Number of times a requested item was found in the off-heap
     *         store.</td>
     *     </tr>
     *     <tr>
     *         <td>{@code on-disk-hits}</td>
     *         <td>Number of times a requested item was found in the disk
     *         store.</td>
     *     </tr>
     *     <tr>
     *         <td>{@code misses}</td>
     *         <td>Number of times a requested item was not found in the
     *         cache<./td>
     *     </tr>
     *     <tr>
     *         <td>{@code in-memory-misses}</td>
     *         <td>Number of times a requested item was not found in the memory
     *         store.</td>
     *     </tr>
     *     <tr>
     *         <td>{@code off-heap-misses}</td>
     *         <td>Number of times a requested item was not found in the
     *         off-heap store.</td>
     *     </tr>
     *     <tr>
     *         <td>{@code on-disk-misses}</td>
     *         <td>Number of times a requested item was not found in the disk
     *         store.</td>
     *     </tr>
     *     <tr>
     *         <td>{@code objects}</td>
     *         <td>Number of elements stored in the cache.</td>
     *     </tr>
     *     <tr>
     *         <td>{@code in-memory-objects}</td>
     *         <td>Number of objects in the memory store.</td>
     *     </tr>
     *     <tr>
     *         <td>{@code off-heap-objects}</td>
     *         <td>Number of objects in the off-heap store.</td>
     *     </tr>
     *     <tr>
     *         <td>{@code on-disk-objects}</td>
     *         <td>Number of objects in the disk store.</td>
     *     </tr>
     *     <tr>
     *         <td>{@code mean-get-time}</td>
     *         <td>The average get time. Because ehcache support JDK1.4.2, each
     *         get time uses {@link System#currentTimeMillis()}, rather than
     *         nanoseconds. The accuracy is thus limited.</td>
     *     </tr>
     *     <tr>
     *         <td>{@code mean-search-time}</td>
     *         <td>The average execution time (in milliseconds) within the last
     *         sample period.</td>
     *     </tr>
     *     <tr>
     *         <td>{@code eviction-count}</td>
     *         <td>The number of cache evictions, since the cache was created,
     *         or statistics were cleared.</td>
     *     </tr>
     *     <tr>
     *         <td>{@code searches-per-second}</td>
     *         <td>The number of search executions that have completed in the
     *         last second.</td>
     *     </tr>
     *     <tr>
     *         <td>{@code accuracy}</td>
     *         <td>A human readable description of the accuracy setting. One of
     *         "None", "Best Effort" or "Guaranteed".</td>
     *     </tr>
     * </table>
     *
     * @param cache an {@link Ehcache} instance
     * @see Statistics
     */
    public static void monitor(final Ehcache cache) {
        Metrics.newGauge(Ehcache.class, "hits", cache.getName(), new GaugeMetric<Long>() {
            @Override
            public Long value() {
                return cache.getStatistics().getCacheHits();
            }
        });

        Metrics.newGauge(Ehcache.class, "in-memory-hits", cache.getName(), new GaugeMetric<Long>() {
            @Override
            public Long value() {
                return cache.getStatistics().getInMemoryHits();
            }
        });

        Metrics.newGauge(Ehcache.class, "off-heap-hits", cache.getName(), new GaugeMetric<Long>() {
            @Override
            public Long value() {
                return cache.getStatistics().getOffHeapHits();
            }
        });

        Metrics.newGauge(Ehcache.class, "on-disk-hits", cache.getName(), new GaugeMetric<Long>() {
            @Override
            public Long value() {
                return cache.getStatistics().getOnDiskHits();
            }
        });

        Metrics.newGauge(Ehcache.class, "misses", cache.getName(), new GaugeMetric<Long>() {
            @Override
            public Long value() {
                return cache.getStatistics().getCacheMisses();
            }
        });

        Metrics.newGauge(Ehcache.class, "in-memory-misses", cache.getName(), new GaugeMetric<Long>() {
            @Override
            public Long value() {
                return cache.getStatistics().getInMemoryMisses();
            }
        });

        Metrics.newGauge(Ehcache.class, "off-heap-misses", cache.getName(), new GaugeMetric<Long>() {
            @Override
            public Long value() {
                return cache.getStatistics().getOffHeapMisses();
            }
        });

        Metrics.newGauge(Ehcache.class, "on-disk-misses", cache.getName(), new GaugeMetric<Long>() {
            @Override
            public Long value() {
                return cache.getStatistics().getOnDiskMisses();
            }
        });

        Metrics.newGauge(Ehcache.class, "objects", cache.getName(), new GaugeMetric<Long>() {
            @Override
            public Long value() {
                return cache.getStatistics().getObjectCount();
            }
        });

        Metrics.newGauge(Ehcache.class, "in-memory-objects", cache.getName(), new GaugeMetric<Long>() {
            @Override
            public Long value() {
                return cache.getStatistics().getMemoryStoreObjectCount();
            }
        });

        Metrics.newGauge(Ehcache.class, "off-heap-objects", cache.getName(), new GaugeMetric<Long>() {
            @Override
            public Long value() {
                return cache.getStatistics().getOffHeapStoreObjectCount();
            }
        });

        Metrics.newGauge(Ehcache.class, "on-disk-objects", cache.getName(), new GaugeMetric<Long>() {
            @Override
            public Long value() {
                return cache.getStatistics().getDiskStoreObjectCount();
            }
        });

        Metrics.newGauge(Ehcache.class, "mean-get-time", cache.getName(), new GaugeMetric<Float>() {
            @Override
            public Float value() {
                return cache.getStatistics().getAverageGetTime();
            }
        });

        Metrics.newGauge(Ehcache.class, "mean-search-time", cache.getName(), new GaugeMetric<Long>() {
            @Override
            public Long value() {
                return cache.getStatistics().getAverageSearchTime();
            }
        });

        Metrics.newGauge(Ehcache.class, "eviction-count", cache.getName(), new GaugeMetric<Long>() {
            @Override
            public Long value() {
                return cache.getStatistics().getEvictionCount();
            }
        });

        Metrics.newGauge(Ehcache.class, "searches-per-second", cache.getName(), new GaugeMetric<Long>() {
            @Override
            public Long value() {
                return cache.getStatistics().getSearchesPerSecond();
            }
        });

        Metrics.newGauge(Ehcache.class, "writer-queue-size", cache.getName(), new GaugeMetric<Long>() {
            @Override
            public Long value() {
                return cache.getStatistics().getWriterQueueSize();
            }
        });

        Metrics.newGauge(Ehcache.class, "accuracy", cache.getName(), new GaugeMetric<String>() {
            @Override
            public String value() {
                return cache.getStatistics().getStatisticsAccuracyDescription();
            }
        });
    }
}
