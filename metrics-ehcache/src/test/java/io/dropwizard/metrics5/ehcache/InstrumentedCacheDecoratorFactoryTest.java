package io.dropwizard.metrics5.ehcache;

import io.dropwizard.metrics5.MetricRegistry;
import io.dropwizard.metrics5.SharedMetricRegistries;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;

class InstrumentedCacheDecoratorFactoryTest {
    private static final CacheManager MANAGER = CacheManager.create();

    private MetricRegistry registry;
    private Ehcache cache;

    @BeforeEach
    void setUp() {
        this.cache = MANAGER.getEhcache("test-config");
        assumeThat(cache).isNotNull();

        this.registry = SharedMetricRegistries.getOrCreate("cache-metrics");
    }

    @Test
    void measuresGets() {
        cache.get("woo");

        assertThat(registry.timer(MetricRegistry.name(Cache.class, "test-config", "gets")).getCount())
                .isEqualTo(1);

    }

    @Test
    void measuresPuts() {
        cache.put(new Element("woo", "whee"));

        assertThat(registry.timer(MetricRegistry.name(Cache.class, "test-config", "puts")).getCount())
                .isEqualTo(1);
    }
}
