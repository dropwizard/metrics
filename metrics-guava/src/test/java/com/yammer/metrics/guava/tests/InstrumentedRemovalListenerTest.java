package com.yammer.metrics.guava.tests;

import com.google.common.cache.Cache;
import com.google.common.cache.RemovalListener;
import com.yammer.metrics.core.Meter;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.guava.InstrumentedRemovalListener;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class InstrumentedRemovalListenerTest {
    private MetricsRegistry registry;
    
    private RemovalListener<String, String> listener;

    @Before
    public void setUp() throws Exception {
        this.registry = new MetricsRegistry();

        this.listener = InstrumentedRemovalListener.newListener(registry, "cache", TimeUnit.SECONDS);
    }

    @After
    public void tearDown() throws Exception {
        registry.shutdown();
    }

    @Test
    public void measureEvictions() throws Exception {
        final Meter meter = registry.newMeter(Cache.class,
                                              "cache-evictions",
                                              "cache",
                                              "evictions",
                                              TimeUnit.SECONDS);

        listener.onRemoval(null);
        listener.onRemoval(null);

        assertThat(meter.count(),
                   is(2L));
    }
}
