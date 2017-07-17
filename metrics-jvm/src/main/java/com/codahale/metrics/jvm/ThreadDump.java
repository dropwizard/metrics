package com.codahale.metrics.jvm;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.management.LockInfo;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

import static java.nio.charset.StandardCharsets.UTF_8;


/**
 * A convenience class for getting a thread dump.
 */
public class ThreadDump {

    private final ThreadMXBean threadMXBean;

    public ThreadDump(ThreadMXBean threadMXBean) {
        this.threadMXBean = threadMXBean;
    }

    /**
     * Dumps all of the threads' current information to an output stream.
     *
     * @param out an output stream
     */
    public void dump(OutputStream out) {
        final ThreadInfo[] threads = this.threadMXBean.dumpAllThreads(true, true);
        final PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, UTF_8));

        for (int ti = threads.length - 1; ti >= 0; ti--) {
            final ThreadInfo t = threads[ti];
            writer.printf("\"%s\" id=%d state=%s",
                          t.getThreadName(),
                          t.getThreadId(),
                          t.getThreadState());
            final LockInfo lock = t.getLockInfo();
            if (lock != null && t.getThreadState() != Thread.State.BLOCKED) {
                writer.printf("%n    - waiting on <0x%08x> (a %s)",
                              lock.getIdentityHashCode(),
                              lock.getClassName());
                writer.printf("%n    - locked <0x%08x> (a %s)",
                              lock.getIdentityHashCode(),
                              lock.getClassName());
            } else if (lock != null && t.getThreadState() == Thread.State.BLOCKED) {
                writer.printf("%n    - waiting to lock <0x%08x> (a %s)",
                              lock.getIdentityHashCode(),
                              lock.getClassName());
            }

            if (t.isSuspended()) {
                writer.print(" (suspended)");
            }

            if (t.isInNative()) {
                writer.print(" (running in native)");
            }

            writer.println();
            if (t.getLockOwnerName() != null) {
                writer.printf("     owned by %s id=%d%n", t.getLockOwnerName(), t.getLockOwnerId());
            }

            final StackTraceElement[] elements = t.getStackTrace();
            final MonitorInfo[] monitors = t.getLockedMonitors();

            for (int i = 0; i < elements.length; i++) {
                final StackTraceElement element = elements[i];
                writer.printf("    at %s%n", element);
                for (int j = 1; j < monitors.length; j++) {
                    final MonitorInfo monitor = monitors[j];
                    if (monitor.getLockedStackDepth() == i) {
                        writer.printf("      - locked %s%n", monitor);
                    }
                }
            }
            writer.println();

            final LockInfo[] locks = t.getLockedSynchronizers();
            if (locks.length > 0) {
                writer.printf("    Locked synchronizers: count = %d%n", locks.length);
                for (LockInfo l : locks) {
                    writer.printf("      - %s%n", l);
                }
                writer.println();
            }
        }

        writer.println();
        writer.flush();
    }

}
