package com.codahale.metrics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * An {@link ExecutorService} that monitors the number of tasks submitted, running,
 * completed and also keeps a {@link Timer} for the task duration.
 * <p>
 * It will register the metrics using the given (or auto-generated) name as classifier, e.g:
 * "your-executor-service.submitted", "your-executor-service.running", etc.
 */
public class InstrumentedExecutorService implements ExecutorService {
    private static final AtomicLong NAME_COUNTER = new AtomicLong();

    private final ExecutorService delegate;
    private final Meter submitted;
    private final Counter running;
    private final Meter completed;
    private final Timer idle;
    private final Timer duration;

    /**
     * Wraps an {@link ExecutorService} uses an auto-generated default name.
     *
     * @param delegate {@link ExecutorService} to wrap.
     * @param registry {@link MetricRegistry} that will contain the metrics.
     */
    public InstrumentedExecutorService(ExecutorService delegate, MetricRegistry registry) {
        this(delegate, registry, "instrumented-delegate-" + NAME_COUNTER.incrementAndGet());
    }

    /**
     * Wraps an {@link ExecutorService} with an explicit name.
     *
     * @param delegate {@link ExecutorService} to wrap.
     * @param registry {@link MetricRegistry} that will contain the metrics.
     * @param name     name for this executor service.
     */
    public InstrumentedExecutorService(ExecutorService delegate, MetricRegistry registry, String name) {
        this.delegate = delegate;
        this.submitted = registry.meter(MetricRegistry.name(name, "submitted"));
        this.running = registry.counter(MetricRegistry.name(name, "running"));
        this.completed = registry.meter(MetricRegistry.name(name, "completed"));
        this.idle = registry.timer(MetricRegistry.name(name, "idle"));
        this.duration = registry.timer(MetricRegistry.name(name, "duration"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(Runnable runnable) {
        submitted.mark();
        delegate.execute(new InstrumentedRunnable(runnable));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Future<?> submit(Runnable runnable) {
        submitted.mark();
        return delegate.submit(new InstrumentedRunnable(runnable));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> Future<T> submit(Runnable runnable, T result) {
        submitted.mark();
        return delegate.submit(new InstrumentedRunnable(runnable), result);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> Future<T> submit(Callable<T> task) {
        submitted.mark();
        return delegate.submit(new InstrumentedCallable<>(task));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        submitted.mark(tasks.size());
        Collection<? extends Callable<T>> instrumented = instrument(tasks);
        return delegate.invokeAll(instrumented);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        submitted.mark(tasks.size());
        Collection<? extends Callable<T>> instrumented = instrument(tasks);
        return delegate.invokeAll(instrumented, timeout, unit);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws ExecutionException, InterruptedException {
        submitted.mark(tasks.size());
        Collection<? extends Callable<T>> instrumented = instrument(tasks);
        return delegate.invokeAny(instrumented);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws ExecutionException, InterruptedException, TimeoutException {
        submitted.mark(tasks.size());
        Collection<? extends Callable<T>> instrumented = instrument(tasks);
        return delegate.invokeAny(instrumented, timeout, unit);
    }

    private <T> Collection<? extends Callable<T>> instrument(Collection<? extends Callable<T>> tasks) {
        final List<InstrumentedCallable<T>> instrumented = new ArrayList<>(tasks.size());
        for (Callable<T> task : tasks) {
            instrumented.add(new InstrumentedCallable<>(task));
        }
        return instrumented;
    }

    @Override
    public void shutdown() {
        delegate.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        return delegate.shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return delegate.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return delegate.isTerminated();
    }

    @Override
    public boolean awaitTermination(long l, TimeUnit timeUnit) throws InterruptedException {
        return delegate.awaitTermination(l, timeUnit);
    }

    private class InstrumentedRunnable implements Runnable {
        private final Runnable task;
        private final Timer.Context idleContext;

        InstrumentedRunnable(Runnable task) {
            this.task = task;
            this.idleContext = idle.time();
        }

        @Override
        public void run() {
            idleContext.stop();
            running.inc();
            try (Timer.Context durationContext = duration.time()) {
                task.run();
            } finally {
                running.dec();
                completed.mark();
            }
        }
    }

    private class InstrumentedCallable<T> implements Callable<T> {
        private final Callable<T> callable;
        private final Timer.Context idleContext;

        InstrumentedCallable(Callable<T> callable) {
            this.callable = callable;
            this.idleContext = idle.time();
        }

        @Override
        public T call() throws Exception {
            idleContext.stop();
            running.inc();
            try (Timer.Context context = duration.time()) {
                return callable.call();
            } finally {
                running.dec();
                completed.mark();
            }
        }
    }
}
