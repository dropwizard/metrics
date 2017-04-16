package com.codahale.metrics.jvm;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class NonAccumulatingGarbageCollectorMetricSetTest {
    GarbageCollectorMetricSet garbageCollectorMetricSet;
    NonAccumulatingGarbageCollectorMetricSet nonAccumulatingGarbageCollectorMetricSet;
    long interval;
    ScheduledExecutorService deterministicScheduledExecutorService;

    @Before
    public void setUp() throws Exception {
        garbageCollectorMetricSet = mock(GarbageCollectorMetricSet.class);
        interval = 60000L; //1 minute in milliseconds
        deterministicScheduledExecutorService = new MockScheduledExecutorService();
    }

    @After
    public void tearDown() throws Exception {
        nonAccumulatingGarbageCollectorMetricSet = null;
    }

    @Test
    public void testGetMetrics() throws Exception {
        Map<String, Metric> mockMetricMap = getStringMetricMap(4L, 800L);
        when(garbageCollectorMetricSet.getMetrics()).thenReturn(mockMetricMap);
        nonAccumulatingGarbageCollectorMetricSet = new NonAccumulatingGarbageCollectorMetricSet(
                garbageCollectorMetricSet, interval, deterministicScheduledExecutorService);

        // on first call, there is no previous data, so it should just return the unaltered mock data
        Map<String, Metric> actualMetricMap = nonAccumulatingGarbageCollectorMetricSet.getMetrics();
        assertEquals(4L, ((Gauge) actualMetricMap.get("count")).getValue());
        assertEquals(800L, ((Gauge) actualMetricMap.get("time")).getValue());
        assertEquals(98.666, (Double) ((Gauge) actualMetricMap.get(
                NonAccumulatingGarbageCollectorMetricSet.GC_THROUGHPUT_METRIC_NAME)).getValue(), 0.001);
        assertEquals(3, actualMetricMap.size());

        // on second call, we still have not run the background process that updates the
        // non-accumulating values, so we expect to get the same readings again
        assertEquals(4L, ((Gauge) actualMetricMap.get("count")).getValue());
        assertEquals(800L, ((Gauge) actualMetricMap.get("time")).getValue());
        assertEquals(98.666, (Double) ((Gauge) actualMetricMap.get(
                NonAccumulatingGarbageCollectorMetricSet.GC_THROUGHPUT_METRIC_NAME)).getValue(), 0.001);
        assertEquals(3, actualMetricMap.size());

        // trigger an update of the the background process that updates non-accumulating values
        // normally this will be scheduled, but we invoke it deterministically for the unit test.
        nonAccumulatingGarbageCollectorMetricSet.scheduleBackgroundCollectionOfNonAccumulatingValues();

        // now previous data should be subtracted from current gauge readings.
        // Since the readings were the same in both cases, the difference should be 0.
        // Since there were no increases in the GC time, we expect GC throughput to be 100
        assertEquals(0L, ((Gauge) actualMetricMap.get("count")).getValue());
        assertEquals(0L, ((Gauge) actualMetricMap.get("time")).getValue());
        assertEquals(100.000, (Double) ((Gauge) actualMetricMap.get(
                NonAccumulatingGarbageCollectorMetricSet.GC_THROUGHPUT_METRIC_NAME)).getValue(), 0.001);
        assertEquals(3, actualMetricMap.size());

        // Now we change the mock data for the underlying garbage collectors. They now report an
        // increase in the total GC count and time. We trigger the background update process again.
        // We should see the non-accumulating values (6-4=2) and (1100-800=300)
        mockMetricMap = getStringMetricMap(6L, 1100L);
        when(garbageCollectorMetricSet.getMetrics()).thenReturn(mockMetricMap);
        nonAccumulatingGarbageCollectorMetricSet.scheduleBackgroundCollectionOfNonAccumulatingValues();
        assertEquals(2L, ((Gauge) actualMetricMap.get("count")).getValue());
        assertEquals(300L, ((Gauge) actualMetricMap.get("time")).getValue());
        assertEquals(99.5, (Double) ((Gauge) actualMetricMap.get(
                NonAccumulatingGarbageCollectorMetricSet.GC_THROUGHPUT_METRIC_NAME)).getValue(), 0.001);
        assertEquals(3, actualMetricMap.size());

    }

    @Test
    public void testNameOfBackgroundUpdateThread() throws Exception {
        Map<String, Metric> mockMetricMap = getStringMetricMap(4L, 800L);
        when(garbageCollectorMetricSet.getMetrics()).thenReturn(mockMetricMap);
        nonAccumulatingGarbageCollectorMetricSet = new NonAccumulatingGarbageCollectorMetricSet(
                garbageCollectorMetricSet, interval);

        assertTrue(foundBackgroundUpdateThread());
    }

    private Boolean foundBackgroundUpdateThread() {
        Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
        Boolean foundName = false;
        for(Thread thread : threadSet) {
            if ("metrics-gc-stats-update-1".equals(thread.getName())) {
                foundName = true;
            }
        }
        return foundName;
    }

    private Map<String, Metric> getStringMetricMap(final Long gcCount, final Long gcTime) {
        Gauge gcCountGauge = new Gauge<Long>() {
            @Override
            public Long getValue() {
                return gcCount;
            }
        };
        Gauge gcTimeGauge = new Gauge<Long>() {
            @Override
            public Long getValue() {
                return gcTime;
            }
        };
        Map<String, Metric> metricStringMap = new HashMap<String, Metric>();
        metricStringMap.put("count", gcCountGauge);
        metricStringMap.put("time", gcTimeGauge);
        return metricStringMap;
    }

    /**
     * This is a very simple mock implementation of a scheduled executor service that executes
     * the task synchronously in the main thread, rather than at a non-deterministic time
     * in another thread. Use this to make unit tests behave deterministically while still
     * exercising all the logic in the scheduled task.
     */
    private static class MockScheduledExecutorService implements ScheduledExecutorService {
        private boolean shutdown;

        private MockScheduledExecutorService() {
        }

        @Override
        public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
            throw new UnsupportedOperationException("No mock implementation available");
        }

        @Override
        public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
            throw new UnsupportedOperationException("No mock implementation available");
        }

        @Override
        public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
            //run the task synchronously
            command.run();
            // return a dummy implementation of the scheduled future, since we are running the task synchronously.
            return new ScheduledFuture<Object>() {
                @Override
                public long getDelay(TimeUnit unit) {
                    throw new UnsupportedOperationException("No mock implementation available");
                }

                @Override
                public int compareTo(Delayed o) {
                    throw new UnsupportedOperationException("No mock implementation available");
                }

                @Override
                public boolean cancel(boolean mayInterruptIfRunning) {
                    throw new UnsupportedOperationException("No mock implementation available");
                }

                @Override
                public boolean isCancelled() {
                    throw new UnsupportedOperationException("No mock implementation available");
                }

                @Override
                public boolean isDone() {
                    throw new UnsupportedOperationException("No mock implementation available");
                }

                @Override
                public Object get() throws InterruptedException, ExecutionException {
                    throw new UnsupportedOperationException("No mock implementation available");
                }

                @Override
                public Object get(long timeout,
                                  TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                    throw new UnsupportedOperationException("No mock implementation available");
                }
            };
        }

        @Override
        public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay,
                                                         TimeUnit unit) {
            throw new UnsupportedOperationException("No mock implementation available");
        }

        @Override
        public void shutdown() {
            shutdown = true;
        }

        @Override
        public List<Runnable> shutdownNow() {
            shutdown = true;
            return Collections.emptyList();
        }

        @Override
        public boolean isShutdown() {
            shutdown = true;
            return shutdown;
        }

        @Override
        public boolean isTerminated() {
            return shutdown;
        }

        @Override
        public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
            return true;
        }

        @Override
        public <T> Future<T> submit(Callable<T> task) {
            {
                throw new UnsupportedOperationException("No mock implementation available");
            }
        }

        @Override
        public <T> Future<T> submit(Runnable task, T result) {
            {
                throw new UnsupportedOperationException("No mock implementation available");
            }
        }

        @Override
        public Future<?> submit(Runnable task) {
            {
                throw new UnsupportedOperationException("No mock implementation available");
            }
        }

        @Override
        public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
            {
                throw new UnsupportedOperationException("No mock implementation available");
            }
        }

        @Override
        public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout,
                                             TimeUnit unit) throws InterruptedException {
            {
                throw new UnsupportedOperationException("No mock implementation available");
            }
        }

        @Override
        public <T> T invokeAny(
                Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
            {
                throw new UnsupportedOperationException("No mock implementation available");
            }
        }

        @Override
        public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout,
                               TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            {
                throw new UnsupportedOperationException("No mock implementation available");
            }
        }

        @Override
        public void execute(Runnable command) {
            {
                throw new UnsupportedOperationException("No mock implementation available");
            }
        }
    }
}