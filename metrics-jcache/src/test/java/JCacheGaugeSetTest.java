import io.dropwizard.metrics5.MetricName;
import io.dropwizard.metrics5.MetricRegistry;
import io.dropwizard.metrics5.jcache.JCacheGaugeSet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.spi.CachingProvider;

import static org.assertj.core.api.Assertions.assertThat;

public class JCacheGaugeSetTest {

    private MetricRegistry registry;
    private Cache<Object, Object> myCache;
    private Cache<Object, Object> myOtherCache;
    private CacheManager cacheManager;

    @Before
    public void setUp() throws Exception {

        CachingProvider provider = Caching.getCachingProvider();
        cacheManager = provider.getCacheManager(
                getClass().getResource("ehcache.xml").toURI(),
                getClass().getClassLoader());

        myCache = cacheManager.getCache("myCache");
        myOtherCache = cacheManager.getCache("myOtherCache");

        registry = new MetricRegistry();
        registry.register("jcache.statistics", new JCacheGaugeSet());
    }

    @Test
    public void measuresGauges() throws Exception {

        myOtherCache.get("woo");
        assertThat(registry.getGauges().get(MetricName.build("jcache.statistics.myOtherCache.cache-misses"))
                .getValue())
                .isEqualTo(1L);

        myCache.get("woo");
        MetricName myCache = MetricName.build("jcache.statistics.myCache");
        assertThat(registry.getGauges().get(myCache.resolve("cache-misses")).getValue())
                .isEqualTo(1L);
        assertThat(registry.getGauges().get(myCache.resolve("cache-hits")).getValue())
                .isEqualTo(0L);
        assertThat(registry.getGauges().get(myCache.resolve("cache-gets")).getValue())
                .isEqualTo(1L);

        this.myCache.put("woo", "whee");
        this.myCache.get("woo");
        assertThat(registry.getGauges().get(myCache.resolve("cache-puts")).getValue())
                .isEqualTo(1L);
        assertThat(registry.getGauges().get(myCache.resolve("cache-hits")).getValue())
                .isEqualTo(1L);
        assertThat(registry.getGauges().get(myCache.resolve("cache-hit-percentage")).getValue())
                .isEqualTo(50.0f);
        assertThat(registry.getGauges().get(myCache.resolve("cache-miss-percentage")).getValue())
                .isEqualTo(50.0f);
        assertThat(registry.getGauges().get(myCache.resolve("cache-gets")).getValue())
                .isEqualTo(2L);

        // cache size being 1, eviction occurs after this line
        this.myCache.put("woo2", "whoza");
        assertThat(registry.getGauges().get(myCache.resolve("cache-evictions")).getValue())
                .isEqualTo(1L);

        this.myCache.remove("woo2");
        assertThat((Float) registry.getGauges().get(myCache.resolve("average-get-time")).getValue())
                .isGreaterThan(0.0f);
        assertThat((Float) registry.getGauges().get(myCache.resolve("average-put-time")).getValue())
                .isGreaterThan(0.0f);
        assertThat((Float) registry.getGauges().get(myCache.resolve("average-remove-time")).getValue())
                .isGreaterThan(0.0f);

    }

    @After
    public void tearDown() throws Exception {
        cacheManager.destroyCache("myCache");
        cacheManager.destroyCache("myOtherCache");
        cacheManager.close();
    }
}
