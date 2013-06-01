package com.codahale.metrics;

import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.fest.assertions.api.Assertions.assertThat;

public class InstrumentedExecutorServiceTest
{
  private final ExecutorService executor = Executors.newCachedThreadPool();
  private final MetricRegistry registry = new MetricRegistry();
  private final InstrumentedExecutorService instrumentedExecutorService = new InstrumentedExecutorService(executor, registry);

  @Test
  public void reportsTasksInformation() throws Exception {
    Runnable fastOne = new FastRunnable();
    Runnable slowOne = new SlowRunnable();

    assertThat(instrumentedExecutorService.submitted.getCount()).isEqualTo(0);
    assertThat(instrumentedExecutorService.running.getCount()).isEqualTo(0);
    assertThat(instrumentedExecutorService.completed.getCount()).isEqualTo(0);
    assertThat(instrumentedExecutorService.duration.getCount()).isEqualTo(0);

    Future<?> fastFuture = instrumentedExecutorService.submit(fastOne);
    Future<?> slowFuture = instrumentedExecutorService.submit(slowOne);

    assertThat(instrumentedExecutorService.submitted.getCount()).isEqualTo(2);

    fastFuture.get();
    assertThat(instrumentedExecutorService.running.getCount()).isEqualTo(1);

    slowFuture.get();
    assertThat(instrumentedExecutorService.running.getCount()).isEqualTo(0);
  }

  private static class FastRunnable implements Runnable
  {
    @Override
    public void run()
    {
      // do nothing, die young and leave a good looking corpse.
    }
  }

  private static class SlowRunnable implements Runnable
  {
    @Override
    public void run()
    {
      try
      {
        // sleep a little, then die.
        Thread.sleep(500);
      }
      catch (Exception e)
      {

      }
    }
  }

}
