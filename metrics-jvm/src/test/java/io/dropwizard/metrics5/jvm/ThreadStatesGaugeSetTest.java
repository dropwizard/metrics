package io.dropwizard.metrics5.jvm;

import io.dropwizard.metrics5.Gauge;
import io.dropwizard.metrics5.MetricName;
import org.assertj.core.api.Assertions;
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

    private static final MetricName TERMINATED_COUNT = MetricName.build("terminated.count");
    private static final MetricName NEW_COUNT = MetricName.build("new.count");
    private static final MetricName COUNT = MetricName.build("count");
    private static final MetricName TIMED_WAITING_COUNT = MetricName.build("timed_waiting.count");
    private static final MetricName DEADLOCKS = MetricName.build("deadlocks");
    private static final MetricName BLOCKED_COUNT = MetricName.build("blocked.count");
    private static final MetricName WAITING_COUNT = MetricName.build("waiting.count");
    private static final MetricName DAEMON_COUNT = MetricName.build("daemon.count");
    private static final MetricName RUNNABLE_COUNT = MetricName.build("runnable.count");
    private static final MetricName DEADLOCK_COUNT = MetricName.build("deadlock.count");

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
        when(threads.getDaemonThreadCount()).thenReturn(13);

        when(detector.getDeadlockedThreads()).thenReturn(deadlocks);
    }

    @Test
    public void hasASetOfGauges() {
        assertThat(gauges.getMetrics().keySet())
                .containsOnly(TERMINATED_COUNT,
                        NEW_COUNT,
                        COUNT,
                        TIMED_WAITING_COUNT,
                        DEADLOCKS,
                        BLOCKED_COUNT,
                        WAITING_COUNT,
                        DAEMON_COUNT,
                        RUNNABLE_COUNT,
                        DEADLOCK_COUNT);
    }

    @Test
    public void hasAGaugeForEachThreadState() {
        Assertions.assertThat(((Gauge<?>) gauges.getMetrics().get(NEW_COUNT)).getValue())
            .isEqualTo(1);

        assertThat(((Gauge<?>) gauges.getMetrics().get(RUNNABLE_COUNT)).getValue())
            .isEqualTo(1);

        assertThat(((Gauge<?>) gauges.getMetrics().get(BLOCKED_COUNT)).getValue())
            .isEqualTo(1);

        assertThat(((Gauge<?>) gauges.getMetrics().get(WAITING_COUNT)).getValue())
            .isEqualTo(1);

        assertThat(((Gauge<?>) gauges.getMetrics().get(TIMED_WAITING_COUNT)).getValue())
            .isEqualTo(1);

        assertThat(((Gauge<?>) gauges.getMetrics().get(TERMINATED_COUNT)).getValue())
            .isEqualTo(1);
    }

    @Test
    public void hasAGaugeForTheNumberOfThreads() {
        assertThat(((Gauge<?>) gauges.getMetrics().get(COUNT)).getValue())
            .isEqualTo(12);
    }

    @Test
    public void hasAGaugeForTheNumberOfDaemonThreads() {
        assertThat(((Gauge<?>) gauges.getMetrics().get(DAEMON_COUNT)).getValue())
            .isEqualTo(13);
    }

    @Test
    public void hasAGaugeForAnyDeadlocks() {
        assertThat(((Gauge<?>) gauges.getMetrics().get(DEADLOCKS)).getValue())
            .isEqualTo(deadlocks);
    }

    @Test
    public void hasAGaugeForAnyDeadlockCount() {
        assertThat(((Gauge<?>) gauges.getMetrics().get(DEADLOCK_COUNT)).getValue())
            .isEqualTo(1);
    }

    @Test
    public void autoDiscoversTheMXBeans() {
        final ThreadStatesGaugeSet set = new ThreadStatesGaugeSet();
        assertThat(((Gauge<?>) set.getMetrics().get(COUNT)).getValue())
            .isNotNull();
        assertThat(((Gauge<?>) set.getMetrics().get(DEADLOCKS)).getValue())
            .isNotNull();
    }
}
