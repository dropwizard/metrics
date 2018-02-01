package com.codahale.metrics;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("deprecation")
public class InstrumentedThreadFactoryTest {

    @Test
    public void testFactory() throws Exception {
        MetricRegistry registry = new MetricRegistry();
        InstrumentedThreadFactory threadFactory = new InstrumentedThreadFactory(Thread::new, registry,
                "test-instrumented-thread-factory");
        CountDownLatch latch = new CountDownLatch(4);
        for (int i = 0; i < 4; i++) {
            threadFactory.newThread(latch::countDown).run();
        }
        latch.await(5, TimeUnit.SECONDS);
        assertThat(registry.meter("test-instrumented-thread-factory.created").getCount()).isEqualTo(4);
        assertThat(registry.counter("test-instrumented-thread-factory.running").getCount()).isEqualTo(0);
        assertThat(registry.meter("test-instrumented-thread-factory.terminated").getCount()).isEqualTo(4);
    }
}
