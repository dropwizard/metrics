package com.yammer.metrics.guava.tests;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.guava.InstrumentedLoadingCache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class InstrumentedLoadingCacheTest {

    private Class<?> cacheClass;
    private LoadingCache<String,String> cache;

    @Before
    public void setUp() throws Exception {
        LoadingCache<String,String> c = CacheBuilder.newBuilder().build(
            new CacheLoader<String,String>() {
                public String load(String key) {
                    return key;
                }
            }
        );
        this.cacheClass = c.getClass();
        this.cache = InstrumentedLoadingCache.instrument("test", c);
    }

    @Test
    public void measuresGets() throws Exception {
        cache.get("woo");

        final Timer gets = Metrics.newTimer(cacheClass,
                                            "get",
                                            "test",
                                            TimeUnit.MILLISECONDS,
                                            TimeUnit.SECONDS);

        assertThat(gets.count(),
                   is(1L));
    }
}
