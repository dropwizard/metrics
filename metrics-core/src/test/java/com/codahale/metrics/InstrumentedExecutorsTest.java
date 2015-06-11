package com.codahale.metrics;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class InstrumentedExecutorsTest {
    private final ThreadFactory defaultThreadFactory = Executors.defaultThreadFactory();
    private MetricRegistry registry;

    @Before
    public void setUp() {
        registry = new MetricRegistry();
    }

    @Test
    public void testNewFixedThreadPool() throws Exception {
        final ExecutorService executorService = InstrumentedExecutors.newFixedThreadPool(2, registry, "xs");
        executorService.submit(new NoopRunnable());

        assertThat(registry.meter("xs.submitted").getCount()).isEqualTo(1l);

        final Field delegateField = InstrumentedExecutorService.class.getDeclaredField("delegate");
        delegateField.setAccessible(true);
        final ThreadPoolExecutor delegate = (ThreadPoolExecutor) delegateField.get(executorService);
        assertThat(delegate.getCorePoolSize()).isEqualTo(2);
        assertThat(delegate.getMaximumPoolSize()).isEqualTo(2);
    }

    @Test
    public void testNewFixedThreadPoolWithThreadFactory() throws Exception {
        final ExecutorService executorService = InstrumentedExecutors.newFixedThreadPool(2, defaultThreadFactory, registry);
        executorService.submit(new NoopRunnable());

        final Field delegateField = InstrumentedExecutorService.class.getDeclaredField("delegate");
        delegateField.setAccessible(true);
        final ThreadPoolExecutor delegate = (ThreadPoolExecutor) delegateField.get(executorService);
        assertThat(delegate.getCorePoolSize()).isEqualTo(2);
        assertThat(delegate.getMaximumPoolSize()).isEqualTo(2);
        assertThat(delegate.getThreadFactory()).isSameAs(defaultThreadFactory);
    }

    @Test
    public void testNewFixedThreadPoolWithThreadFactoryAndName() throws Exception {
        final ExecutorService executorService = InstrumentedExecutors.newFixedThreadPool(2, defaultThreadFactory, registry, "xs");
        executorService.submit(new NoopRunnable());

        assertThat(registry.meter("xs.submitted").getCount()).isEqualTo(1l);

        final Field delegateField = InstrumentedExecutorService.class.getDeclaredField("delegate");
        delegateField.setAccessible(true);
        final ThreadPoolExecutor delegate = (ThreadPoolExecutor) delegateField.get(executorService);
        assertThat(delegate.getCorePoolSize()).isEqualTo(2);
        assertThat(delegate.getMaximumPoolSize()).isEqualTo(2);
        assertThat(delegate.getThreadFactory()).isSameAs(defaultThreadFactory);
    }

    @Test
    public void testNewSingleThreadExecutor() throws Exception {
        final ExecutorService executorService = InstrumentedExecutors.newSingleThreadExecutor(registry, "xs");
        executorService.submit(new NoopRunnable());

        assertThat(registry.meter("xs.submitted").getCount()).isEqualTo(1l);
    }

    @Test
    public void testNewSingleThreadExecutorWithThreadFactory() throws Exception {
        final ExecutorService executorService = InstrumentedExecutors.newSingleThreadExecutor(defaultThreadFactory, registry);
        executorService.submit(new NoopRunnable());
    }

    @Test
    public void testNewSingleThreadExecutorWithThreadFactoryAndName() throws Exception {
        final ExecutorService executorService = InstrumentedExecutors.newSingleThreadExecutor(defaultThreadFactory, registry, "xs");
        executorService.submit(new NoopRunnable());

        assertThat(registry.meter("xs.submitted").getCount()).isEqualTo(1l);
    }

    @Test
    public void testNewCachedThreadPool() throws Exception {
        final ExecutorService executorService = InstrumentedExecutors.newCachedThreadPool(registry, "xs");
        executorService.submit(new NoopRunnable());
        executorService.submit(new NoopRunnable());

        assertThat(registry.meter("xs.submitted").getCount()).isEqualTo(2l);

        final Field delegateField = InstrumentedExecutorService.class.getDeclaredField("delegate");
        delegateField.setAccessible(true);
        final ThreadPoolExecutor delegate = (ThreadPoolExecutor) delegateField.get(executorService);
        assertThat(delegate.getCorePoolSize()).isEqualTo(0);
        assertThat(delegate.getPoolSize()).isEqualTo(2);
    }

    @Test
    public void testNewCachedThreadPoolWithThreadFactory() throws Exception {
        final ExecutorService executorService = InstrumentedExecutors.newCachedThreadPool(defaultThreadFactory, registry);
        executorService.submit(new NoopRunnable());
        executorService.submit(new NoopRunnable());

        final Field delegateField = InstrumentedExecutorService.class.getDeclaredField("delegate");
        delegateField.setAccessible(true);
        final ThreadPoolExecutor delegate = (ThreadPoolExecutor) delegateField.get(executorService);
        assertThat(delegate.getCorePoolSize()).isEqualTo(0);
        assertThat(delegate.getPoolSize()).isEqualTo(2);
        assertThat(delegate.getThreadFactory()).isSameAs(defaultThreadFactory);
    }

    @Test
    public void testNewCachedThreadPoolWithThreadFactoryAndName() throws Exception {
        final ExecutorService executorService = InstrumentedExecutors.newCachedThreadPool(defaultThreadFactory, registry, "xs");
        executorService.submit(new NoopRunnable());
        executorService.submit(new NoopRunnable());

        assertThat(registry.meter("xs.submitted").getCount()).isEqualTo(2l);

        final Field delegateField = InstrumentedExecutorService.class.getDeclaredField("delegate");
        delegateField.setAccessible(true);
        final ThreadPoolExecutor delegate = (ThreadPoolExecutor) delegateField.get(executorService);
        assertThat(delegate.getCorePoolSize()).isEqualTo(0);
        assertThat(delegate.getPoolSize()).isEqualTo(2);
        assertThat(delegate.getThreadFactory()).isSameAs(defaultThreadFactory);
    }

    @Test
    public void testNewSingleThreadScheduledExecutor() throws Exception {
        final ScheduledExecutorService executorService = InstrumentedExecutors.newSingleThreadScheduledExecutor(registry);
        executorService.schedule(new NoopRunnable(), 0, TimeUnit.SECONDS);
    }

    @Test
    public void testNewSingleThreadScheduledExecutorWithName() throws Exception {
        final ScheduledExecutorService executorService = InstrumentedExecutors.newSingleThreadScheduledExecutor(registry, "xs");
        executorService.schedule(new NoopRunnable(), 0, TimeUnit.SECONDS);

        assertThat(registry.meter("xs.scheduled.once").getCount()).isEqualTo(1l);
    }

    @Test
    public void testNewScheduledThreadPool() throws Exception {
        final ScheduledExecutorService executorService = InstrumentedExecutors.newScheduledThreadPool(2, registry, "xs");
        executorService.schedule(new NoopRunnable(), 0, TimeUnit.SECONDS);

        assertThat(registry.meter("xs.scheduled.once").getCount()).isEqualTo(1l);

        final Field delegateField = InstrumentedScheduledExecutorService.class.getDeclaredField("delegate");
        delegateField.setAccessible(true);
        final ScheduledThreadPoolExecutor delegate = (ScheduledThreadPoolExecutor) delegateField.get(executorService);
        assertThat(delegate.getCorePoolSize()).isEqualTo(2);
    }

    @Test
    public void testNewScheduledThreadPoolWithThreadFactory() throws Exception {
        final ScheduledExecutorService executorService = InstrumentedExecutors.newScheduledThreadPool(2, defaultThreadFactory, registry);
        executorService.schedule(new NoopRunnable(), 0, TimeUnit.SECONDS);

        final Field delegateField = InstrumentedScheduledExecutorService.class.getDeclaredField("delegate");
        delegateField.setAccessible(true);
        final ScheduledThreadPoolExecutor delegate = (ScheduledThreadPoolExecutor) delegateField.get(executorService);
        assertThat(delegate.getCorePoolSize()).isEqualTo(2);
        assertThat(delegate.getThreadFactory()).isSameAs(defaultThreadFactory);
    }

    @Test
    public void testNewScheduledThreadPoolWithThreadFactoryAndName() throws Exception {
        final ScheduledExecutorService executorService = InstrumentedExecutors.newScheduledThreadPool(2, defaultThreadFactory, registry, "xs");
        executorService.schedule(new NoopRunnable(), 0, TimeUnit.SECONDS);

        assertThat(registry.meter("xs.scheduled.once").getCount()).isEqualTo(1l);

        final Field delegateField = InstrumentedScheduledExecutorService.class.getDeclaredField("delegate");
        delegateField.setAccessible(true);
        final ScheduledThreadPoolExecutor delegate = (ScheduledThreadPoolExecutor) delegateField.get(executorService);
        assertThat(delegate.getCorePoolSize()).isEqualTo(2);
        assertThat(delegate.getThreadFactory()).isSameAs(defaultThreadFactory);
    }

    @Test
    public void testDefaultThreadFactory() throws Exception {
        final ThreadFactory threadFactory = InstrumentedExecutors.defaultThreadFactory(registry);
        threadFactory.newThread(new NoopRunnable());

        final Field delegateField = InstrumentedThreadFactory.class.getDeclaredField("delegate");
        delegateField.setAccessible(true);
        final ThreadFactory delegate = (ThreadFactory) delegateField.get(threadFactory);
        assertThat(delegate.getClass().getCanonicalName()).isEqualTo("java.util.concurrent.Executors.DefaultThreadFactory");
    }

    @Test
    public void testDefaultThreadFactoryWithName() throws Exception {
        final ThreadFactory threadFactory = InstrumentedExecutors.defaultThreadFactory(registry, "tf");
        threadFactory.newThread(new NoopRunnable());

        assertThat(registry.meter("tf.created").getCount()).isEqualTo(1l);

        final Field delegateField = InstrumentedThreadFactory.class.getDeclaredField("delegate");
        delegateField.setAccessible(true);
        final ThreadFactory delegate = (ThreadFactory) delegateField.get(threadFactory);
        assertThat(delegate.getClass().getCanonicalName()).isEqualTo("java.util.concurrent.Executors.DefaultThreadFactory");
    }

    @Test
    public void testPrivilegedThreadFactory() throws Exception {
        final ThreadFactory threadFactory = InstrumentedExecutors.privilegedThreadFactory(registry);
        threadFactory.newThread(new NoopRunnable());

        final Field delegateField = InstrumentedThreadFactory.class.getDeclaredField("delegate");
        delegateField.setAccessible(true);
        final ThreadFactory delegate = (ThreadFactory) delegateField.get(threadFactory);
        assertThat(delegate.getClass().getCanonicalName()).isEqualTo("java.util.concurrent.Executors.PrivilegedThreadFactory");
    }

    @Test
    public void testPrivilegedThreadFactoryWithName() throws Exception {
        final ThreadFactory threadFactory = InstrumentedExecutors.privilegedThreadFactory(registry, "tf");
        threadFactory.newThread(new NoopRunnable());

        assertThat(registry.meter("tf.created").getCount()).isEqualTo(1l);

        final Field delegateField = InstrumentedThreadFactory.class.getDeclaredField("delegate");
        delegateField.setAccessible(true);
        final ThreadFactory delegate = (ThreadFactory) delegateField.get(threadFactory);
        assertThat(delegate.getClass().getCanonicalName()).isEqualTo("java.util.concurrent.Executors.PrivilegedThreadFactory");
    }

    private static class NoopRunnable implements Runnable {
        @Override
        public void run() {
        }
    }
}