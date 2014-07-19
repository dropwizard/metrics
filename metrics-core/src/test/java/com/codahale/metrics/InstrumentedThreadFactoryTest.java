package com.codahale.metrics;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

public class InstrumentedThreadFactoryTest {
    private static final int THREAD_COUNT = 10;

    private final ThreadFactory factory = Executors.defaultThreadFactory();
    private final MetricRegistry registry = new MetricRegistry();
    private final InstrumentedThreadFactory instrumentedFactory = new InstrumentedThreadFactory(factory, registry, "factory");
    private final ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT, instrumentedFactory);

    /**
     * Tests all parts of the InstrumentedThreadFactory except for termination since that
     * is currently difficult to do without race conditions.
     * 
     * TODO: Try not using real threads in a unit test?
     */
    @Test
    public void reportsThreadInformation() throws Exception {
        final CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
        final Object lock = new Object();
        final AtomicInteger interrupted = new AtomicInteger();

        /*
         * Implements a runnable that notifies a latch after locking 'lock'.
         * This asserts that all threads have to enter the critical block before the
         * testing thread notifies all.
         *
         * We have to do this to guarantee that the thread pool has 10 LIVE threads
         * before we check the 'created' Meter.
         */
        Runnable fastOne = new Runnable() {
            @Override
            public void run() {
                synchronized (lock) {
                    latch.countDown();

                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        interrupted.incrementAndGet();
                    }
                }
            }
        };

        Meter created = registry.meter("factory.created");
        Meter terminated = registry.meter("factory.terminated");

        assertThat(created.getCount()).isEqualTo(0);
        assertThat(terminated.getCount()).isEqualTo(0);

        // generate demand so the executor service creates the threads through our factory.
        for (int i = 0; i < THREAD_COUNT + 1; i++) {
            executor.submit(fastOne);
        }

        latch.await(1, TimeUnit.SECONDS);

        synchronized (lock) {
            // wake up all threads.
            lock.notifyAll();
        }

        assertThat(created.getCount()).isEqualTo(10);
        assertThat(terminated.getCount()).isEqualTo(0);

        // terminate all threads in the executor service.
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.SECONDS);

        // assert that all threads from the factory have been terminated.
        // TODO: Remove this?
        //       There is no guarantee that all threads have entered the block where they are
        //       counted as terminated by this time.
        // assertThat(terminated.getCount()).isEqualTo(10);

        // Check that none of the threads were interrupted.
        assertThat(interrupted.get()).isEqualTo(0);
    }
}
