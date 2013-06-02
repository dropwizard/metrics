package com.codahale.metrics;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A {@link ThreadFactory} that monitors the number of threads created, running and terminated.
 *
 * It will register the metrics using the given (or auto-generated) name as classifier, e.g:
 * "your-thread-factory.created", "your-thread-factory.running", etc.
 */
public class InstrumentedThreadFactory implements ThreadFactory
{
  private static final AtomicLong nameCounter = new AtomicLong();

  private final ThreadFactory factory;
  final Counter created;
  final Counter running;
  final Counter finished;

  /**
   * Wraps a {@link ThreadFactory}, uses a default auto-generated name.
   *
   * @param factory {@link ThreadFactory} to wrap.
   * @param registry {@link MetricRegistry} that will contain the metrics.
   */
  public InstrumentedThreadFactory(ThreadFactory factory, MetricRegistry registry)
  {
    this(factory, registry, "instrumented-thread-factory-" + nameCounter.incrementAndGet());
  }

  /**
   * Wraps a {@link ThreadFactory} with an explicit name.
   *
   * @param factory {@link ThreadFactory} to wrap.
   * @param registry {@link MetricRegistry} that will contain the metrics.
   * @param name name for this factory.
   */
  public InstrumentedThreadFactory(ThreadFactory factory, MetricRegistry registry, String name)
  {
    this.factory = factory;
    this.created = registry.counter(name + ".created");
    this.running = registry.counter(name + ".running");
    this.finished = registry.counter(name + ".finished");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Thread newThread(Runnable runnable)
  {
    Runnable wrappedRunnable = new InstrumentedRunnable(runnable);
    Thread thread = factory.newThread(wrappedRunnable);
    created.inc();
    return thread;
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
      try
      {
        task.run();
      }
      finally
      {
        running.dec();
        finished.inc();
      }
    }
  }
}
