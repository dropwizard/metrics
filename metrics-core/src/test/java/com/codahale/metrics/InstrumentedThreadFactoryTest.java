package com.codahale.metrics;

import org.junit.Test;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import static org.fest.assertions.api.Assertions.assertThat;

public class InstrumentedThreadFactoryTest {
  private final ThreadFactory factory = Executors.defaultThreadFactory();
  private final MetricRegistry registry = new MetricRegistry();
  private final InstrumentedThreadFactory instrumentedFactory = new InstrumentedThreadFactory(factory, registry);

  @Test
  public void reportsThreadInformation() throws Exception {
    Runnable fastOne = new FastRunnable();
    Runnable slowOne = new SlowRunnable();

    Thread fastThread = instrumentedFactory.newThread(fastOne);
    Thread slowThread = instrumentedFactory.newThread(slowOne);

    assertThat(instrumentedFactory.created.getCount()).isEqualTo(2);
    assertThat(instrumentedFactory.running.getCount()).isEqualTo(0);
    assertThat(instrumentedFactory.finished.getCount()).isEqualTo(0);

    fastThread.start();slowThread.start();

    Thread.sleep(100);
    assertThat(instrumentedFactory.running.getCount()).isEqualTo(1);
    assertThat(instrumentedFactory.finished.getCount()).isEqualTo(1);

    Thread.sleep(1000);
    assertThat(instrumentedFactory.running.getCount()).isEqualTo(0);
    assertThat(instrumentedFactory.finished.getCount()).isEqualTo(2);
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
