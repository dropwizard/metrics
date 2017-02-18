package com.codahale.metrics.jvm;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.lang.management.LockInfo;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

// TODO: 3/12/13 <coda> -- improve test coverage for ThreadDump

public class ThreadDumpTest {
    private final ThreadMXBean threadMXBean = mock(ThreadMXBean.class);
    private final ThreadDump threadDump = new ThreadDump(threadMXBean);

    private final ThreadInfo runnable = mock(ThreadInfo.class);

    @Before
    public void setUp() throws Exception {
        final StackTraceElement rLine1 = new StackTraceElement("Blah", "blee", "Blah.java", 100);

        when(runnable.getThreadName()).thenReturn("runnable");
        when(runnable.getThreadId()).thenReturn(100L);
        when(runnable.getThreadState()).thenReturn(Thread.State.RUNNABLE);
        when(runnable.getStackTrace()).thenReturn(new StackTraceElement[]{ rLine1 });
        when(runnable.getLockedMonitors()).thenReturn(new MonitorInfo[]{ });
        when(runnable.getLockedSynchronizers()).thenReturn(new LockInfo[]{ });

        when(threadMXBean.dumpAllThreads(true, true)).thenReturn(new ThreadInfo[]{
                runnable
        });
    }

    @Test
    public void dumpsAllThreads() throws Exception {
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        threadDump.dump(output);

        assertThat(output.toString())
                .isEqualTo(String.format("\"runnable\" id=100 state=RUNNABLE%n" +
                                                 "    at Blah.blee(Blah.java:100)%n" +
                                                 "%n" +
                                                 "%n"));
    }
}
