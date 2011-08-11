package com.yammer.metrics.core;
import java.lang.management.ThreadMXBean;
import java.lang.management.ManagementFactory;

/**
 * An abstraction for how time is ticked. It is passed to {@link TimerMetric} to track timing.
 */
 
public interface TimerTicker{
  
  /**
   * Gets the current time tick
   * @return time tick in nanoseconds
   */
  long tickTime();
  
  /**
   * Default implementation, uses System.nanoTime()
   */
  public static class UserTimerTicker implements TimerTicker{
    @Override
    public long tickTime(){
      return System.nanoTime();
    }
  }
  
  
  /**
   * Another implementation, uses ThreadMXBean.getCurrentThreadCpuTime()
   */
  public static class CPUTimerTicker implements TimerTicker{
    private static ThreadMXBean threadMxBean = ManagementFactory.getThreadMXBean();
    @Override
    public long tickTime(){
      return threadMxBean.getCurrentThreadCpuTime();
    }
  }
}
