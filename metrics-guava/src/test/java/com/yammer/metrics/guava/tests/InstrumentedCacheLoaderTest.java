package com.yammer.metrics.guava.tests;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.guava.InstrumentedCacheLoader;
import com.google.common.cache.CacheLoader;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class InstrumentedCacheLoaderTest {

    private Class<?> cacheLoaderClass;
    private CacheLoader<String, String> cacheLoader;

    @Before
    public void setUp() throws Exception {
        CacheLoader<String,String> cl = new CacheLoader<String,String>() {
            public String load(String key) {
                return key;
            }
        };
        this.cacheLoaderClass = cl.getClass();
        this.cacheLoader = InstrumentedCacheLoader.instrument(cl);
    }

    @Test
    public void measuresLoads() throws Exception {
        cacheLoader.load("woo");

        final Timer loads = Metrics.newTimer(cacheLoaderClass,
                                            "load",
                                            TimeUnit.MILLISECONDS,
                                            TimeUnit.SECONDS);

        assertThat(loads.count(),
                   is(1L));
    }
}
