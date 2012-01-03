package com.yammer.metrics.guava.tests;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.yammer.metrics.core.Meter;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.guava.InstrumentedCacheLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class InstrumentedCacheLoaderTest {
    private MetricsRegistry registry;
    private LoadingCache<String, String> cache;

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        this.registry = new MetricsRegistry();

        final CacheLoader<String, String> loader = mock(CacheLoader.class);
        when(loader.load(anyString())).thenReturn("woo");

        this.cache = CacheBuilder.newBuilder().build(
                InstrumentedCacheLoader.instrument(registry, loader, "cache", TimeUnit.SECONDS)
        );
    }

    @After
    public void tearDown() throws Exception {
        registry.shutdown();
    }

    @Test
    public void marksCacheMissesViaAMeter() throws Exception {
        final Meter meter = registry.newMeter(Cache.class,
                                              "cache-misses",
                                              "cache",
                                              "misses",
                                              TimeUnit.SECONDS);

        cache.get("one");
        cache.get("one");
        cache.get("two");
        cache.get("two");

        assertThat(meter.count(),
                   is(2L));
    }
}
