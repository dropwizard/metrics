package com.yammer.metrics.guava.tests;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.guava.InstrumentedCache;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class InstrumentedCacheTest {

    private Class<?> cacheClass;
    private Cache<String,String> cache;

    @Before
    public void setUp() throws Exception {
        Cache<String,String> c = CacheBuilder.newBuilder().build();
        this.cacheClass = c.getClass();
        this.cache = InstrumentedCache.instrument("test", c);
    }

    @Test
    public void measuresGets() throws Exception {
        cache.getIfPresent("woo");

        final Timer gets = Metrics.newTimer(cacheClass,
                                            "get",
                                            "test",
                                            TimeUnit.MILLISECONDS,
                                            TimeUnit.SECONDS);

        assertThat(gets.count(),
                   is(1L));
    }
}
