package com.codahale.metrics;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

public class InstrumentedThreadFactory implements ThreadFactory
{
  private static final AtomicLong nameCounter = new AtomicLong();

  private final ThreadFactory factory;
  final Counter created;
  final Counter running;
  final Counter finished;

  public InstrumentedThreadFactory(ThreadFactory factory, MetricRegistry registry)
  {
    this(factory, registry, "instrumented-thread-factory-" + nameCounter.incrementAndGet());
  }

  public InstrumentedThreadFactory(ThreadFactory factory, MetricRegistry registry, String name)
  {
    this.factory = factory;
    this.created = registry.counter(name + ".created");
    this.running = registry.counter(name + ".running");
    this.finished = registry.counter(name + ".finished");
  }

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
