package io.dropwizard.metrics5.ehcache;

import io.dropwizard.metrics5.MetricRegistry;
import io.dropwizard.metrics5.Timer;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class InstrumentedEhcacheTest {
    private static final CacheManager MANAGER = CacheManager.create();

    private final MetricRegistry registry = new MetricRegistry();
    private Ehcache cache;

    @Before
    public void setUp() {
        final Cache c = new Cache(new CacheConfiguration("test", 100));
        MANAGER.addCache(c);
        this.cache = InstrumentedEhcache.instrument(registry, c);
    }

    @Test
    public void measuresGetsAndPuts() {
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
