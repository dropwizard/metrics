package com.codahale.metrics;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class InstrumentedExecutorServiceTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(InstrumentedExecutorServiceTest.class);
    private ExecutorService executor;
    private MetricRegistry registry;
    private InstrumentedExecutorService instrumentedExecutorService;
    private Meter submitted;
    private Counter running;
    private Meter completed;
    private Timer duration;
    private Timer idle;

    @Before
    public void setup() {
        executor = Executors.newCachedThreadPool();
        registry = new MetricRegistry();
        instrumentedExecutorService = new InstrumentedExecutorService(executor, registry, "xs");
        submitted = registry.meter("xs.submitted");
        running = registry.counter("xs.running");
        completed = registry.meter("xs.completed");
        duration = registry.timer("xs.duration");
        idle = registry.timer("xs.idle");
    }

    @After
    public void tearDown() throws Exception {
        instrumentedExecutorService.shutdown();
        if (!instrumentedExecutorService.awaitTermination(2, TimeUnit.SECONDS)) {
            LOGGER.error("InstrumentedExecutorService did not terminate.");
        }
    }

    @Test
    public void reportsTasksInformationForRunnable() throws Exception {

        assertThat(submitted.getCount()).isEqualTo(0);
        assertThat(running.getCount()).isEqualTo(0);
        assertThat(completed.getCount()).isEqualTo(0);
        assertThat(duration.getCount()).isEqualTo(0);
        assertThat(idle.getCount()).isEqualTo(0);

        Runnable runnable = () -> {
            assertThat(submitted.getCount()).isEqualTo(1);
            assertThat(running.getCount()).isEqualTo(1);
            assertThat(completed.getCount()).isEqualTo(0);
            assertThat(duration.getCount()).isEqualTo(0);
            assertThat(idle.getCount()).isEqualTo(1);
        };

        Future<?> theFuture = instrumentedExecutorService.submit(runnable);

        theFuture.get();

        assertThat(submitted.getCount()).isEqualTo(1);
        assertThat(running.getCount()).isEqualTo(0);
        assertThat(completed.getCount()).isEqualTo(1);
        assertThat(duration.getCount()).isEqualTo(1);
        assertThat(duration.getSnapshot().size()).isEqualTo(1);
        assertThat(idle.getCount()).isEqualTo(1);
        assertThat(idle.getSnapshot().size()).isEqualTo(1);
    }

    @Test
    public void reportsTasksInformationForCallable() throws Exception {

        assertThat(submitted.getCount()).isEqualTo(0);
        assertThat(running.getCount()).isEqualTo(0);
        assertThat(completed.getCount()).isEqualTo(0);
        assertThat(duration.getCount()).isEqualTo(0);
        assertThat(idle.getCount()).isEqualTo(0);

        Callable<Void> callable = () -> {
            assertThat(submitted.getCount()).isEqualTo(1);
            assertThat(running.getCount()).isEqualTo(1);
            assertThat(completed.getCount()).isEqualTo(0);
            assertThat(duration.getCount()).isEqualTo(0);
            assertThat(idle.getCount()).isEqualTo(1);
            return null;
        };

        Future<?> theFuture = instrumentedExecutorService.submit(callable);

        theFuture.get();

        assertThat(submitted.getCount()).isEqualTo(1);
        assertThat(running.getCount()).isEqualTo(0);
        assertThat(completed.getCount()).isEqualTo(1);
        assertThat(duration.getCount()).isEqualTo(1);
        assertThat(duration.getSnapshot().size()).isEqualTo(1);
        assertThat(idle.getCount()).isEqualTo(1);
        assertThat(idle.getSnapshot().size()).isEqualTo(1);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void reportsTasksInformationForThreadPoolExecutor() throws Exception {
        executor = new ThreadPoolExecutor(4, 16,
                0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(32));
        instrumentedExecutorService = new InstrumentedExecutorService(executor, registry, "tp");
        submitted = registry.meter("tp.submitted");
        running = registry.counter("tp.running");
        completed = registry.meter("tp.completed");
        duration = registry.timer("tp.duration");
        idle = registry.timer("tp.idle");
        final Gauge<Integer> poolSize = (Gauge<Integer>) registry.getGauges().get("tp.pool.size");
        final Gauge<Integer> poolCoreSize = (Gauge<Integer>) registry.getGauges().get("tp.pool.core");
        final Gauge<Integer> poolMaxSize = (Gauge<Integer>) registry.getGauges().get("tp.pool.max");
        final Gauge<Integer> tasksActive = (Gauge<Integer>) registry.getGauges().get("tp.tasks.active");
        final Gauge<Long> tasksCompleted = (Gauge<Long>) registry.getGauges().get("tp.tasks.completed");
        final Gauge<Integer> tasksQueued = (Gauge<Integer>) registry.getGauges().get("tp.tasks.queued");
        final Gauge<Integer> tasksCapacityRemaining = (Gauge<Integer>) registry.getGauges().get("tp.tasks.capacity");

        assertThat(submitted.getCount()).isEqualTo(0);
        assertThat(running.getCount()).isEqualTo(0);
        assertThat(completed.getCount()).isEqualTo(0);
        assertThat(duration.getCount()).isEqualTo(0);
        assertThat(idle.getCount()).isEqualTo(0);
        assertThat(poolSize.getValue()).isEqualTo(0);
        assertThat(poolCoreSize.getValue()).isEqualTo(4);
        assertThat(poolMaxSize.getValue()).isEqualTo(16);
        assertThat(tasksActive.getValue()).isEqualTo(0);
        assertThat(tasksCompleted.getValue()).isEqualTo(0L);
        assertThat(tasksQueued.getValue()).isEqualTo(0);
        assertThat(tasksCapacityRemaining.getValue()).isEqualTo(32);

        Runnable runnable = () -> {
            assertThat(submitted.getCount()).isEqualTo(1);
            assertThat(running.getCount()).isEqualTo(1);
            assertThat(completed.getCount()).isEqualTo(0);
            assertThat(duration.getCount()).isEqualTo(0);
            assertThat(idle.getCount()).isEqualTo(1);
            assertThat(tasksActive.getValue()).isEqualTo(1);
            assertThat(tasksQueued.getValue()).isEqualTo(0);
        };

        Future<?> theFuture = instrumentedExecutorService.submit(runnable);

        assertThat(theFuture).succeedsWithin(Duration.ofSeconds(5L));

        assertThat(submitted.getCount()).isEqualTo(1);
        assertThat(running.getCount()).isEqualTo(0);
        assertThat(completed.getCount()).isEqualTo(1);
        assertThat(duration.getCount()).isEqualTo(1);
        assertThat(duration.getSnapshot().size()).isEqualTo(1);
        assertThat(idle.getCount()).isEqualTo(1);
        assertThat(idle.getSnapshot().size()).isEqualTo(1);
        assertThat(poolSize.getValue()).isEqualTo(1);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void reportsTasksInformationForForkJoinPool() throws Exception {
        executor = Executors.newWorkStealingPool(4);
        instrumentedExecutorService = new InstrumentedExecutorService(executor, registry, "fjp");
        submitted = registry.meter("fjp.submitted");
        running = registry.counter("fjp.running");
        completed = registry.meter("fjp.completed");
        duration = registry.timer("fjp.duration");
        idle = registry.timer("fjp.idle");
        final Gauge<Long> tasksStolen = (Gauge<Long>) registry.getGauges().get("fjp.tasks.stolen");
        final Gauge<Long> tasksQueued = (Gauge<Long>) registry.getGauges().get("fjp.tasks.queued");
        final Gauge<Integer> threadsActive = (Gauge<Integer>) registry.getGauges().get("fjp.threads.active");
        final Gauge<Integer> threadsRunning = (Gauge<Integer>) registry.getGauges().get("fjp.threads.running");

        assertThat(submitted.getCount()).isEqualTo(0);
        assertThat(running.getCount()).isEqualTo(0);
        assertThat(completed.getCount()).isEqualTo(0);
        assertThat(duration.getCount()).isEqualTo(0);
        assertThat(idle.getCount()).isEqualTo(0);
        assertThat(tasksStolen.getValue()).isEqualTo(0L);
        assertThat(tasksQueued.getValue()).isEqualTo(0L);
        assertThat(threadsActive.getValue()).isEqualTo(0);
        assertThat(threadsRunning.getValue()).isEqualTo(0);

        Runnable runnable = () -> {
            assertThat(submitted.getCount()).isEqualTo(1);
            assertThat(running.getCount()).isEqualTo(1);
            assertThat(completed.getCount()).isEqualTo(0);
            assertThat(duration.getCount()).isEqualTo(0);
            assertThat(idle.getCount()).isEqualTo(1);
            assertThat(tasksQueued.getValue()).isEqualTo(0L);
            assertThat(threadsActive.getValue()).isEqualTo(1);
            assertThat(threadsRunning.getValue()).isEqualTo(1);
        };

        Future<?> theFuture = instrumentedExecutorService.submit(runnable);

        assertThat(theFuture).succeedsWithin(Duration.ofSeconds(5L));

        assertThat(submitted.getCount()).isEqualTo(1);
        assertThat(running.getCount()).isEqualTo(0);
        assertThat(completed.getCount()).isEqualTo(1);
        assertThat(duration.getCount()).isEqualTo(1);
        assertThat(duration.getSnapshot().size()).isEqualTo(1);
        assertThat(idle.getCount()).isEqualTo(1);
        assertThat(idle.getSnapshot().size()).isEqualTo(1);
    }
}
