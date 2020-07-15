package com.codahale.metrics.jvm;

import com.codahale.metrics.Gauge;
import org.junit.Before;
import org.junit.Test;

import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ThreadStatesGaugeSetTest {
    private final ThreadMXBean threads = mock(ThreadMXBean.class);
    private final ThreadDeadlockDetector detector = mock(ThreadDeadlockDetector.class);
    private final ThreadStatesGaugeSet gauges = new ThreadStatesGaugeSet(threads, detector);
    private final long[] ids = new long[]{1, 2, 3};

    private final ThreadInfo newThread = mock(ThreadInfo.class);
    private final ThreadInfo runnableThread = mock(ThreadInfo.class);
    private final ThreadInfo blockedThread = mock(ThreadInfo.class);
    private final ThreadInfo waitingThread = mock(ThreadInfo.class);
    private final ThreadInfo timedWaitingThread = mock(ThreadInfo.class);
    private final ThreadInfo terminatedThread = mock(ThreadInfo.class);

    private final Set<String> deadlocks = new HashSet<>();

    @Before
    public void setUp() {
        deadlocks.add("yay");

        when(newThread.getThreadState()).thenReturn(Thread.State.NEW);
        when(runnableThread.getThreadState()).thenReturn(Thread.State.RUNNABLE);
        when(blockedThread.getThreadState()).thenReturn(Thread.State.BLOCKED);
        when(waitingThread.getThreadState()).thenReturn(Thread.State.WAITING);
        when(timedWaitingThread.getThreadState()).thenReturn(Thread.State.TIMED_WAITING);
        when(terminatedThread.getThreadState()).thenReturn(Thread.State.TERMINATED);

        when(threads.getAllThreadIds()).thenReturn(ids);
        when(threads.getThreadInfo(ids, 0)).thenReturn(new ThreadInfo[]{
            newThread, runnableThread, blockedThread,
            waitingThread, timedWaitingThread, terminatedThread
        });

        when(threads.getThreadCount()).thenReturn(12);
        when(threads.getDaemonThreadCount()).thenReturn(10);
        when(threads.getPeakThreadCount()).thenReturn(30);
        when(threads.getTotalStartedThreadCount()).thenReturn(42L);

        when(detector.getDeadlockedThreads()).thenReturn(deadlocks);
    }

    @Test
    public void hasASetOfGauges() {
        assertThat(gauges.getMetrics().keySet())
            .containsOnly("terminated.count",
                "new.count",
                "count",
                "timed_waiting.count",
                "deadlocks",
                "blocked.count",
                "waiting.count",
                "daemon.count",
                "runnable.count",
                "deadlock.count",
                "total_started.count",
                "peak.count");
    }

    @Test
    public void hasAGaugeForEachThreadState() {
        assertThat(((Gauge<?>) gauges.getMetrics().get("new.count")).getValue())
            .isEqualTo(1);

        assertThat(((Gauge<?>) gauges.getMetrics().get("runnable.count")).getValue())
            .isEqualTo(1);

        assertThat(((Gauge<?>) gauges.getMetrics().get("blocked.count")).getValue())
            .isEqualTo(1);

        assertThat(((Gauge<?>) gauges.getMetrics().get("waiting.count")).getValue())
            .isEqualTo(1);

        assertThat(((Gauge<?>) gauges.getMetrics().get("timed_waiting.count")).getValue())
            .isEqualTo(1);

        assertThat(((Gauge<?>) gauges.getMetrics().get("terminated.count")).getValue())
            .isEqualTo(1);
    }

    @Test
    public void hasAGaugeForTheNumberOfThreads() {
        assertThat(((Gauge<?>) gauges.getMetrics().get("count")).getValue())
            .isEqualTo(12);
    }

    @Test
    public void hasAGaugeForTheNumberOfDaemonThreads() {
        assertThat(((Gauge<?>) gauges.getMetrics().get("daemon.count")).getValue())
            .isEqualTo(10);
    }

    @Test
    public void hasAGaugeForAnyDeadlocks() {
        assertThat(((Gauge<?>) gauges.getMetrics().get("deadlocks")).getValue())
            .isEqualTo(deadlocks);
    }

    @Test
    public void hasAGaugeForAnyDeadlockCount() {
        assertThat(((Gauge<?>) gauges.getMetrics().get("deadlock.count")).getValue())
            .isEqualTo(1);
    }

    @Test
    public void hasAGaugeForPeakThreadCount() {
        assertThat(((Gauge<?>) gauges.getMetrics().get("peak.count")).getValue())
            .isEqualTo(30);
    }

    @Test
    public void hasAGaugeForTotalStartedThreadsCount() {
        assertThat(((Gauge<?>) gauges.getMetrics().get("total_started.count")).getValue())
            .isEqualTo(42L);
    }

    @Test
    public void autoDiscoversTheMXBeans() {
        final ThreadStatesGaugeSet set = new ThreadStatesGaugeSet();
        assertThat(((Gauge<?>) set.getMetrics().get("count")).getValue())
            .isNotNull();
        assertThat(((Gauge<?>) set.getMetrics().get("deadlocks")).getValue())
            .isNotNull();
    }
}
