package io.dropwizard.metrics5.ehcache;

import io.dropwizard.metrics5.MetricRegistry;
import io.dropwizard.metrics5.Timer;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

class InstrumentedEhcacheTest {
    private static final CacheManager MANAGER = CacheManager.create();

    private final MetricRegistry registry = new MetricRegistry();
    private Ehcache cache;

    @BeforeEach
    void setUp() {
        final Cache c = new Cache(new CacheConfiguration("test", 100));
        MANAGER.addCache(c);
        this.cache = InstrumentedEhcache.instrument(registry, c);
        assertThat(registry.getGauges().entrySet().stream()
                .map(e -> entry(e.getKey().getKey(), (Number) e.getValue().getValue())))
                .containsOnly(
                        entry("net.sf.ehcache.Cache.test.eviction-count", 0L),
                        entry("net.sf.ehcache.Cache.test.hits", 0L),
                        entry("net.sf.ehcache.Cache.test.in-memory-hits", 0L),
                        entry("net.sf.ehcache.Cache.test.in-memory-misses", 0L),
                        entry("net.sf.ehcache.Cache.test.in-memory-objects", 0L),
                        entry("net.sf.ehcache.Cache.test.mean-get-time", Double.NaN),
                        entry("net.sf.ehcache.Cache.test.mean-search-time", Double.NaN),
                        entry("net.sf.ehcache.Cache.test.misses", 0L),
                        entry("net.sf.ehcache.Cache.test.objects", 0L),
                        entry("net.sf.ehcache.Cache.test.off-heap-hits", 0L),
                        entry("net.sf.ehcache.Cache.test.off-heap-misses", 0L),
                        entry("net.sf.ehcache.Cache.test.off-heap-objects", 0L),
                        entry("net.sf.ehcache.Cache.test.on-disk-hits", 0L),
                        entry("net.sf.ehcache.Cache.test.on-disk-misses", 0L),
                        entry("net.sf.ehcache.Cache.test.on-disk-objects", 0L),
                        entry("net.sf.ehcache.Cache.test.searches-per-second", 0.0),
                        entry("net.sf.ehcache.Cache.test.writer-queue-size", 0L)
                );
    }

    @Test
    void measuresGetsAndPuts() {
        cache.get("woo");

        cache.put(new Element("woo", "whee"));

        final Timer gets = registry.timer(MetricRegistry.name(Cache.class, "test", "gets"));

        assertThat(gets.getCount())
                .isEqualTo(1);

        final Timer puts = registry.timer(MetricRegistry.name(Cache.class, "test", "puts"));

        assertThat(puts.getCount())
                .isEqualTo(1);
    }
}
