package com.codahale.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InstrumentedExecutorServiceTest {

    @Rule public TestName testName = new TestName();

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
        
        if (testName.getMethodName().startsWith("reportsTasksInformationForInvokeAll")) {
            executor = Executors.newCachedThreadPool();
            registry = new MetricRegistry();
            instrumentedExecutorService = new InstrumentedExecutorService(executor, registry);
            submitted = instrumentedExecutorService.getSubmitted();
            running = instrumentedExecutorService.getRunning();
            completed = instrumentedExecutorService.getCompleted();
            duration = instrumentedExecutorService.getDuration();
            idle = instrumentedExecutorService.getIdle();
        } else {
            executor = Executors.newCachedThreadPool();
            registry = new MetricRegistry();
            instrumentedExecutorService = new InstrumentedExecutorService(executor, registry, "xs");
            submitted = registry.meter("xs.submitted");
            running = registry.counter("xs.running");
            completed = registry.meter("xs.completed");
            duration = registry.timer("xs.duration");
            idle = registry.timer("xs.idle");
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
    public void reportsTasksInformationForInvokeAllWithTimeout() throws InterruptedException, ExecutionException {

        assertThat(submitted.getCount()).isEqualTo(0);
        assertThat(running.getCount()).isEqualTo(0);
        assertThat(completed.getCount()).isEqualTo(0);
        assertThat(duration.getCount()).isEqualTo(0);
        assertThat(idle.getCount()).isEqualTo(0);

        Collection<Callable<Void>> tasks = new ArrayList<>();
        tasks.add(() -> {
            return null; 
        });
        tasks.add(() -> {
            return null; 
        });
        List<Future<Void>> fs = instrumentedExecutorService.invokeAll(tasks, 1, TimeUnit.MINUTES);

        for (Future<Void> f : fs) {
            f.get();
        }
        
        assertThat(submitted.getCount()).isEqualTo(fs.size());
        assertThat(running.getCount()).isEqualTo(0);
        assertThat(completed.getCount()).isEqualTo(fs.size());
        assertThat(duration.getCount()).isEqualTo(fs.size());
        assertThat(duration.getSnapshot().size()).isEqualTo(fs.size());
        assertThat(idle.getCount()).isEqualTo(fs.size());
        assertThat(idle.getSnapshot().size()).isEqualTo(fs.size());
    }

    @Test
    public void reportsTasksInformationForInvokeAll() throws InterruptedException, ExecutionException {

        assertThat(submitted.getCount()).isEqualTo(0);
        assertThat(running.getCount()).isEqualTo(0);
        assertThat(completed.getCount()).isEqualTo(0);
        assertThat(duration.getCount()).isEqualTo(0);
        assertThat(idle.getCount()).isEqualTo(0);

        Collection<Callable<Void>> tasks = new ArrayList<>();
        tasks.add(() -> {
            return null; 
        });
        tasks.add(() -> {
            return null; 
        });
        List<Future<Void>> fs = instrumentedExecutorService.invokeAll(tasks);

        for (Future<Void> f : fs) {
            f.get();
        }
        
        assertThat(submitted.getCount()).isEqualTo(fs.size());
        assertThat(running.getCount()).isEqualTo(0);
        assertThat(completed.getCount()).isEqualTo(fs.size());
        assertThat(duration.getCount()).isEqualTo(fs.size());
        assertThat(duration.getSnapshot().size()).isEqualTo(fs.size());
        assertThat(idle.getCount()).isEqualTo(fs.size());
        assertThat(idle.getSnapshot().size()).isEqualTo(fs.size());
    }

    @After
    public void tearDown() throws Exception {
        instrumentedExecutorService.shutdown();
        if (!instrumentedExecutorService.awaitTermination(2, TimeUnit.SECONDS)) {
            LOGGER.error("InstrumentedExecutorService did not terminate.");
        }
    }

}
