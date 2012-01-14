package com.yammer.metrics.ehcache.tests;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.ehcache.InstrumentedEhcache;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class InstrumentedEhcacheTest {
    private static final CacheManager MANAGER = CacheManager.create();

    private Ehcache cache;

    @Before
    public void setUp() throws Exception {
        final Cache c = new Cache(new CacheConfiguration("test", 100));
        MANAGER.addCache(c);
        this.cache = InstrumentedEhcache.instrument(c);
    }

    @Test
    public void measuresGetsAndPuts() throws Exception {
        cache.get("woo");

        cache.put(new Element("woo", "whee"));

        final Timer gets = Metrics.newTimer(Cache.class,
                                            "get",
                                            "test",
                                            TimeUnit.MILLISECONDS,
                                            TimeUnit.SECONDS);

        assertThat(gets.count(),
                   is(1L));

        final Timer puts = Metrics.newTimer(Cache.class,
                                            "put",
                                            "test",
                                            TimeUnit.MILLISECONDS,
                                            TimeUnit.SECONDS);

        assertThat(puts.count(),
                   is(1L));
    }
}
