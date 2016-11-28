package io.dropwizard.metrics.ehcache;

import static io.dropwizard.metrics.MetricRegistry.name;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;

import io.dropwizard.metrics.MetricRegistry;
import io.dropwizard.metrics.SharedMetricRegistries;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assume.assumeThat;

public class InstrumentedCacheDecoratorFactoryTest {
    private static final CacheManager MANAGER = CacheManager.create();

    private MetricRegistry registry;
    private Ehcache cache;

    @Before
    public void setUp() throws Exception {
        this.cache = MANAGER.getEhcache("test-config");
        assumeThat(cache, is(CoreMatchers.notNullValue()));

        this.registry = SharedMetricRegistries.getOrCreate("cache-metrics");
    }

    @Test
    public void measuresGets() throws Exception {
        cache.get("woo");

        assertThat(registry.timer(name(Cache.class, "test-config", "gets")).getCount())
                .isEqualTo(1);

    }

    @Test
    public void measuresPuts() throws Exception {
        cache.put(new Element("woo", "whee"));

        assertThat(registry.timer(name(Cache.class, "test-config", "puts")).getCount())
                .isEqualTo(1);
    }
}
