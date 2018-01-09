package io.dropwizard.metrics5.ehcache;

import io.dropwizard.metrics5.MetricRegistry;
import io.dropwizard.metrics5.SharedMetricRegistries;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assume.assumeThat;

public class InstrumentedCacheDecoratorFactoryTest {
    private static final CacheManager MANAGER = CacheManager.create();

    private MetricRegistry registry;
    private Ehcache cache;

    @Before
    public void setUp() {
        this.cache = MANAGER.getEhcache("test-config");
        assumeThat(cache, is(CoreMatchers.notNullValue()));

        this.registry = SharedMetricRegistries.getOrCreate("cache-metrics");
    }

    @Test
    public void measuresGets() {
        cache.get("woo");

        assertThat(registry.timer(MetricRegistry.name(Cache.class, "test-config", "gets")).getCount())
                .isEqualTo(1);

    }

    @Test
    public void measuresPuts() {
        cache.put(new Element("woo", "whee"));

        assertThat(registry.timer(MetricRegistry.name(Cache.class, "test-config", "puts")).getCount())
                .isEqualTo(1);
    }
}
