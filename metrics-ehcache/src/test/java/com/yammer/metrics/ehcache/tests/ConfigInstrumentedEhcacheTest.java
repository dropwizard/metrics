package com.yammer.metrics.ehcache.tests;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Timer;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class ConfigInstrumentedEhcacheTest {

    private static final CacheManager MANAGER = CacheManager.create();

    private Ehcache cache;

    @Before
    public void setUp() throws Exception {
        cache = MANAGER.getEhcache("test-config");
        if (cache == null) fail("Cache is not set correctly");
    }

    @Test
    public void measuresGets() throws Exception {
        cache.get("woo");

        final Timer gets = Metrics.newTimer(Cache.class,
                                            "get",
                                            "test-config",
                                            TimeUnit.MILLISECONDS,
                                            TimeUnit.SECONDS);

        assertThat(gets.count(), is(1L));

    }

    @Test
    public void measuresPuts() throws Exception {

        cache.put(new Element("woo", "whee"));

        final Timer puts = Metrics.newTimer(Cache.class,
                                            "put",
                                            "test-config",
                                            TimeUnit.MILLISECONDS,
                                            TimeUnit.SECONDS);

        assertThat(puts.count(), is(1L));

    }

}
