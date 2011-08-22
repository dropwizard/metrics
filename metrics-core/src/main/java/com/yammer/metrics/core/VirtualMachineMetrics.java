package com.yammer.metrics.core;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.Thread.State;
import java.lang.management.*;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static java.lang.management.ManagementFactory.*;

/**
 * A collection of Java Virtual Machine metrics.
 */
public class VirtualMachineMetrics {
    public static class GarbageCollector {
        private final long runs, timeMS;

        public GarbageCollector(long runs, long timeMS) {
            this.runs = runs;
            this.timeMS = timeMS;
        }

        public long getRuns() {
            return runs;
        }

        public long getTime(TimeUnit unit) {
            return unit.convert(timeMS, TimeUnit.MILLISECONDS);
        }
    }

    private VirtualMachineMetrics() { /* unused */ }

    /**
     * Returns the percentage of the JVM's heap which is being used.
     *
     * @return the percentage of the JVM's heap which is being used
     */
    public static double heapUsage() {
        final MemoryUsage bean = getMemoryMXBean().getHeapMemoryUsage();
        return bean.getUsed() / (double) bean.getMax();
    }

    /**
     * Returns the percentage of the JVM's non-heap memory (e.g., direct
     * buffers) which is being used.
     *
     * @return the percentage of the JVM's non-heap memory which is being used
     */
    public static double nonHeapUsage() {
        final MemoryUsage bean = getMemoryMXBean().getNonHeapMemoryUsage();
        return bean.getUsed() / (double) bean.getMax();
    }

    /**
     * Returns a map of memory pool names to the percentage of that pool which
     * is being used.
     *
     * @return a map of memory pool names to the percentage of that pool which
     *         is being used
     */
    public static Map<String, Double> memoryPoolUsage() {
        final Map<String, Double> pools = new TreeMap<String, Double>();
        for (MemoryPoolMXBean bean : getMemoryPoolMXBeans()) {
            final double max = bean.getUsage().getMax() == -1 ?
                    bean.getUsage().getCommitted() :
                    bean.getUsage().getMax();
            pools.put(bean.getName(), bean.getUsage().getUsed() / max);
        }
        return pools;
    }

    /**
     * Returns the percentage of available file descriptors which are currently
     * in use.
     *
     * @return the percentage of available file descriptors which are currently
     *         in use, or {@code NaN} if the running JVM does not have access to
     *         this information
     */
    public static double fileDescriptorUsage() {
        try {
            final OperatingSystemMXBean bean = getOperatingSystemMXBean();
            final Method getOpenFileDescriptorCount = bean.getClass().getDeclaredMethod("getOpenFileDescriptorCount");
            getOpenFileDescriptorCount.setAccessible(true);
            final Long openFds = (Long) getOpenFileDescriptorCount.invoke(bean);
            final Method getMaxFileDescriptorCount = bean.getClass().getDeclaredMethod("getMaxFileDescriptorCount");
            getMaxFileDescriptorCount.setAccessible(true);
            final Long maxFds = (Long) getMaxFileDescriptorCount.invoke(bean);
            return openFds.doubleValue() / maxFds.doubleValue();
        } catch (Exception e) {
            return Double.NaN;
        }
    }

    /**
     * Returns the version of the currently-running jvm.
     *
     * @see <a href="http://java.sun.com/j2se/versioning_naming.html">J2SE SDK/JRE Version String Naming Convention</a>
     * @return the version of the currently-running jvm, eg "1.6.0_24"
     */
    public static String vmVersion() {
        return System.getProperty("java.runtime.version");
    }

    /**
     * Returns the name of the currently-running jvm.
     *
     * @see <a href="http://download.oracle.com/javase/6/docs/api/java/lang/System.html#getProperties()">System.getProperties()</a>
     * @return the name of the currently-running jvm, eg  "Java HotSpot(TM) Client VM"
     */
    public static String vmName() {
        return System.getProperty("java.vm.name");
    }

    /**
     * Returns the number of seconds the JVM process has been running.
     *
     * @return the number of seconds the JVM process has been running
     */
    public static long uptime() {
        return getRuntimeMXBean().getUptime() / 1000;
    }

    /**
     * Returns the number of live threads (includes {@link #daemonThreadCount()}.
     *
     * @return the number of live threads
     */
    public static long threadCount() {
        return getThreadMXBean().getThreadCount();
    }

    /**
     * Returns the number of live daemon threads.
     *
     * @return the number of live daemon threads
     */
    public static long daemonThreadCount() {
        return getThreadMXBean().getDaemonThreadCount();
    }

    /**
     * Returns a map of garbage collector names to garbage collector information.
     *
     * @return a map of garbage collector names to garbage collector information
     */
    public static Map<String, GarbageCollector> garbageCollectors() {
        final Map<String, GarbageCollector> gcs = new HashMap<String, GarbageCollector>();
        for (GarbageCollectorMXBean bean : getGarbageCollectorMXBeans()) {
            gcs.put(bean.getName(), new GarbageCollector(bean.getCollectionCount(), bean.getCollectionTime()));
        }
        return gcs;
    }

    /**
     * Returns a set of strings describing deadlocked threads, if any are
     * deadlocked.
     *
     * @return a set of any deadlocked threads
     */
    public static Set<String> deadlockedThreads() {
        final long[] threadIds = getThreadMXBean().findDeadlockedThreads();
        if (threadIds != null) {
            final Set<String> threads = new HashSet<String>();
            final ThreadInfo[] infos = getThreadMXBean().getThreadInfo(threadIds, 100);
            for (ThreadInfo info : infos) {
                final StringBuilder stackTrace = new StringBuilder();
                for (StackTraceElement element : info.getStackTrace()) {
                    stackTrace.append("\t at ").append(element.toString()).append('\n');
                }

                threads.add(
                    String.format(
                            "%s locked on %s (owned by %s):\n%s",
                            info.getThreadName(), info.getLockName(),
                            info.getLockOwnerName(),
                            stackTrace.toString()
                    )
                );
            }
            return threads;
        }
        return Collections.emptySet();
    }

    /**
     * Returns a map of thread states to the percentage of all threads which
     * are in that state.
     *
     * @return a map of thread states to percentages
     */
    public static Map<State, Double> threadStatePercentages() {
        final Map<State, Double> conditions = new HashMap<State, Double>();
        for (State state : State.values()) {
            conditions.put(state, 0.0);
        }

        final long[] allThreadIds = getThreadMXBean().getAllThreadIds();
        final ThreadInfo[] allThreads = getThreadMXBean().getThreadInfo(allThreadIds);
        for (ThreadInfo info : allThreads) {
            final State state = info.getThreadState();
            conditions.put(state, conditions.get(state) + 1);
        }
        for (State state : new ArrayList<State>(conditions.keySet())) {
            conditions.put(state, conditions.get(state) / allThreads.length);
        }

        return conditions;
    }

    /**
     * Dumps all of the threads' current information to an output stream.
     *
     * @param out an output stream
     * @throws IOException if something goes wrong
     */
    public static void threadDump(OutputStream out) throws IOException {
        final ThreadInfo[] threads = ManagementFactory.getThreadMXBean().dumpAllThreads(true, true);
        final PrintWriter writer = new PrintWriter(out, true);

        for (int ti = threads.length - 1; ti > 0; ti--) {
            final ThreadInfo t = threads[ti];
            writer.printf("%s id=%d state=%s", t.getThreadName(), t.getThreadId(), t.getThreadState());
            final LockInfo lock = t.getLockInfo();
            if (lock != null && t.getThreadState() != Thread.State.BLOCKED) {
                writer.printf("\n    - waiting on <0x%08x> (a %s)", lock.getIdentityHashCode(), lock.getClassName());
                writer.printf("\n    - locked <0x%08x> (a %s)", lock.getIdentityHashCode(), lock.getClassName());
            } else if (lock != null && t.getThreadState() == Thread.State.BLOCKED) {
                writer.printf("\n    - waiting to lock <0x%08x> (a %s)", lock.getIdentityHashCode(), lock.getClassName());
            }

            if (t.isSuspended()) {
                writer.print(" (suspended)");
            }

            if (t.isInNative()) {
                writer.print(" (running in native)");
            }

            writer.println();
            if (t.getLockOwnerName() != null) {
                writer.printf("     owned by %s id=%d\n", t.getLockOwnerName(), t.getLockOwnerId());
            }

            final StackTraceElement[] elements = t.getStackTrace();
            final MonitorInfo[] monitors = t.getLockedMonitors();

            for (int i = 0; i < elements.length; i++) {
                final StackTraceElement element = elements[i];
                writer.printf("    at %s\n", element);
                for (int j = 1; j < monitors.length; j++) {
                    final MonitorInfo monitor = monitors[j];
                    if (monitor.getLockedStackDepth() == i) {
                        writer.printf("      - locked %s\n", monitor);
                    }
                }
            }
            writer.println();

            final LockInfo[] locks = t.getLockedSynchronizers();
            if (locks.length > 0) {
                writer.printf("    Locked synchronizers: count = %d\n", locks.length);
                for (LockInfo l : locks) {
                    writer.printf("      - %s\n", l);
                }
                writer.println();
            }
        }
        
        writer.println();
        writer.flush();
    }
}
