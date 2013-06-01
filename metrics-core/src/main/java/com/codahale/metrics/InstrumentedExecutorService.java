package com.codahale.metrics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

public class InstrumentedExecutorService extends AbstractExecutorService
{
  private static final AtomicLong nameCounter = new AtomicLong();

  private final ExecutorService executorService;
  final Counter submitted;
  final Counter running;
  final Counter completed;
  final Timer duration;

  public InstrumentedExecutorService(ExecutorService executorService, MetricRegistry registry)
  {
    this(executorService, registry, "instrumented-executorService-" + nameCounter.incrementAndGet());
  }

  public InstrumentedExecutorService(ExecutorService executorService, MetricRegistry registry, String name)
  {
    this.executorService = executorService;
    this.submitted = registry.counter(name + ".submitted");
    this.running = registry.counter(name + ".running");
    this.completed = registry.counter(name + ".completed");
    this.duration = registry.timer(name + ".duration");
  }

  @Override
  public void execute(Runnable runnable)
  {
    System.out.println("executing");
    executorService.execute(new InstrumentedRunnable(runnable));
  }

  @Override
  public Future<?> submit(Runnable runnable)
  {
    submitted.inc();
    return executorService.submit(new InstrumentedRunnable(runnable));
  }

  @Override
  public <T> Future<T> submit(Runnable runnable, T result)
  {
    submitted.inc();
    return executorService.submit(new InstrumentedRunnable(runnable), result);
  }

  @Override
  public <T> Future<T> submit(Callable<T> task)
  {
    submitted.inc();
    return executorService.submit(new InstrumentedCallable<T>(task));
  }

  @Override
  public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException
  {
    submitted.inc(tasks.size());
    Collection<? extends Callable<T>> instrumented = instrument(tasks);
    return executorService.invokeAll(instrumented);
  }

  @Override
  public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
          throws InterruptedException
  {
    submitted.inc(tasks.size());
    Collection<? extends Callable<T>> instrumented = instrument(tasks);
    return executorService.invokeAll(instrumented, timeout, unit);
  }

  @Override
  public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws ExecutionException, InterruptedException
  {
    submitted.inc(tasks.size());
    Collection<? extends Callable<T>> instrumented = instrument(tasks);
    return executorService.invokeAny(instrumented);
  }

  @Override
  public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
          throws ExecutionException, InterruptedException, TimeoutException
  {
    submitted.inc(tasks.size());
    Collection<? extends Callable<T>> instrumented = instrument(tasks);
    return executorService.invokeAny(instrumented, timeout, unit);
  }

  private <T> Collection<? extends Callable<T>> instrument(Collection<? extends Callable<T>> tasks)
  {
    List<InstrumentedCallable<T>> instrumented = new ArrayList<InstrumentedCallable<T>>();
    for (Callable<T> task: tasks)
    {
      instrumented.add(new InstrumentedCallable(task));
    }
    return instrumented;
  }

  @Override
  public void shutdown()
  {
    executorService.shutdown();
  }

  @Override
  public List<Runnable> shutdownNow()
  {
    return executorService.shutdownNow();
  }

  @Override
  public boolean isShutdown()
  {
    return executorService.isShutdown();
  }

  @Override
  public boolean isTerminated()
  {
    return executorService.isTerminated();
  }

  @Override
  public boolean awaitTermination(long l, TimeUnit timeUnit) throws InterruptedException
  {
    return executorService.awaitTermination(l, timeUnit);
  }

  private class InstrumentedRunnable implements Runnable
  {
    private final Runnable task;
    InstrumentedRunnable(Runnable task)
    {
      this.task = task;
    }

    @Override
    public void run()
    {
      running.inc();
      Timer.Context context = duration.time();
      try
      {
        task.run();
      }
      finally
      {
        context.stop();
        running.dec();
        completed.inc();
      }
    }
  }

  private class InstrumentedCallable<T> implements Callable<T>
  {
    private final Callable<T> callable;
    InstrumentedCallable(Callable<T> callable)
    {
      this.callable = callable;
    }

    @Override
    public T call() throws Exception
    {
      running.inc();
      Timer.Context context = duration.time();
      try
      {
        return callable.call();
      }
      finally
      {
        context.stop();
        running.dec();
        completed.inc();
      }
    }
  }
}
