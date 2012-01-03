package com.yammer.metrics.core;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.Thread.State;
import java.lang.management.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static java.lang.management.ManagementFactory.*;

/**
 * A collection of Java Virtual Machine metrics.
 */
public class VirtualMachineMetrics {
    private static final int MAX_STACK_TRACE_DEPTH = 100;

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

    public static final VirtualMachineMetrics INSTANCE = new VirtualMachineMetrics();

    private VirtualMachineMetrics() { /* unused */ }

    /**
     * Returns the total initial memory of the current JVM.
     *
     * @return total Heap and non-heap initial JVM memory in bytes.
     */
    public double totalInit() {
        return getMemoryMXBean().getHeapMemoryUsage().getInit() +
                getMemoryMXBean().getNonHeapMemoryUsage().getInit();
    }

    /**
     * Returns the total memory currently used by the current JVM.
     *
     * @return total Heap and non-heap memory currently used by JVM in bytes.
     */
    public double totalUsed() {
        return getMemoryMXBean().getHeapMemoryUsage().getUsed() +
                getMemoryMXBean().getNonHeapMemoryUsage().getUsed();
    }

    /**
     * Returns the total memory currently used by the current JVM.
     *
     * @return total Heap and non-heap memory currently used by JVM in bytes.
     */
    public double totalMax() {
        return getMemoryMXBean().getHeapMemoryUsage().getMax() +
                getMemoryMXBean().getNonHeapMemoryUsage().getMax();
    }
    /**
     * Returns the total memory committed to the JVM.
     *
     * @return total Heap and non-heap memory currently committed to the JVM in bytes.
     */
    public double totalCommitted() {
        return getMemoryMXBean().getHeapMemoryUsage().getCommitted() +
                getMemoryMXBean().getNonHeapMemoryUsage().getCommitted();
    }
    /**
     * Returns the heap initial memory of the current JVM.
     *
     * @return Heap initial JVM memory in bytes.
     */
    public double heapInit() {
        return getMemoryMXBean().getHeapMemoryUsage().getInit();
    }
    /**
     * Returns the heap memory currently used by the current JVM.
     *
     * @return Heap memory currently used by JVM in bytes.
     */
    public double heapUsed() {
        return getMemoryMXBean().getHeapMemoryUsage().getUsed();
    }
    /**
     * Returns the heap memory currently used by the current JVM.
     *
     * @return Heap memory currently used by JVM in bytes.
     */
    public double heapMax() {
        return getMemoryMXBean().getHeapMemoryUsage().getMax();
    }
    /**
     * Returns the heap memory committed to the JVM.
     *
     * @return Heap memory currently committed to the JVM in bytes.
     */
    public double heapCommitted() {
        return getMemoryMXBean().getHeapMemoryUsage().getCommitted();
    }

    /**
     * Returns the percentage of the JVM's heap which is being used.
     *
     * @return the percentage of the JVM's heap which is being used
     */
    public double heapUsage() {
        final MemoryUsage bean = getMemoryMXBean().getHeapMemoryUsage();
        return bean.getUsed() / (double) bean.getMax();
    }

    /**
     * Returns the percentage of the JVM's non-heap memory (e.g., direct buffers) which is being
     * used.
     *
     * @return the percentage of the JVM's non-heap memory which is being used
     */
    public double nonHeapUsage() {
        final MemoryUsage bean = getMemoryMXBean().getNonHeapMemoryUsage();
        return bean.getUsed() / (double) bean.getMax();
    }

    /**
     * Returns a map of memory pool names to the percentage of that pool which is being used.
     *
     * @return a map of memory pool names to the percentage of that pool which is being used
     */
    public Map<String, Double> memoryPoolUsage() {
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
     * Returns the percentage of available file descriptors which are currently in use.
     *
     * @return the percentage of available file descriptors which are currently in use, or {@code
     *         NaN} if the running JVM does not have access to this information
     */
    public double fileDescriptorUsage() {
        try {
            final OperatingSystemMXBean bean = getOperatingSystemMXBean();
            final Method getOpenFileDescriptorCount = bean.getClass().getDeclaredMethod("getOpenFileDescriptorCount");
            getOpenFileDescriptorCount.setAccessible(true);
            final Long openFds = (Long) getOpenFileDescriptorCount.invoke(bean);
            final Method getMaxFileDescriptorCount = bean.getClass().getDeclaredMethod("getMaxFileDescriptorCount");
            getMaxFileDescriptorCount.setAccessible(true);
            final Long maxFds = (Long) getMaxFileDescriptorCount.invoke(bean);
            return openFds.doubleValue() / maxFds.doubleValue();
        } catch (NoSuchMethodException e) {
            return Double.NaN;
        } catch (IllegalAccessException e) {
            return Double.NaN;
        } catch (InvocationTargetException e) {
            return Double.NaN;
        }
    }

    /**
     * Returns the version of the currently-running jvm.
     *
     * @return the version of the currently-running jvm, eg "1.6.0_24"
     * @see <a href="http://java.sun.com/j2se/versioning_naming.html">J2SE SDK/JRE Version String
     *      Naming Convention</a>
     */
    public String version() {
        return System.getProperty("java.runtime.version");
    }

    /**
     * Returns the name of the currently-running jvm.
     *
     * @return the name of the currently-running jvm, eg  "Java HotSpot(TM) Client VM"
     * @see <a href="http://download.oracle.com/javase/6/docs/api/java/lang/System.html#getProperties()">System.getProperties()</a>
     */
    public String name() {
        return System.getProperty("java.vm.name");
    }

    /**
     * Returns the number of seconds the JVM process has been running.
     *
     * @return the number of seconds the JVM process has been running
     */
    public long uptime() {
        return TimeUnit.MILLISECONDS.toSeconds(getRuntimeMXBean().getUptime());
    }

    /**
     * Returns the number of live threads (includes {@link #daemonThreadCount()}.
     *
     * @return the number of live threads
     */
    public long threadCount() {
        return getThreadMXBean().getThreadCount();
    }

    /**
     * Returns the number of live daemon threads.
     *
     * @return the number of live daemon threads
     */
    public long daemonThreadCount() {
        return getThreadMXBean().getDaemonThreadCount();
    }

    /**
     * Returns a map of garbage collector names to garbage collector information.
     *
     * @return a map of garbage collector names to garbage collector information
     */
    public Map<String, GarbageCollector> garbageCollectors() {
        final Map<String, GarbageCollector> gcs = new HashMap<String, GarbageCollector>();
        for (GarbageCollectorMXBean bean : getGarbageCollectorMXBeans()) {
            gcs.put(bean.getName(),
                    new GarbageCollector(bean.getCollectionCount(), bean.getCollectionTime()));
        }
        return gcs;
    }

    /**
     * Returns a set of strings describing deadlocked threads, if any are deadlocked.
     *
     * @return a set of any deadlocked threads
     */
    public Set<String> deadlockedThreads() {
        final long[] threadIds = getThreadMXBean().findDeadlockedThreads();
        if (threadIds != null) {
            final Set<String> threads = new HashSet<String>();
            for (ThreadInfo info : getThreadMXBean().getThreadInfo(threadIds, MAX_STACK_TRACE_DEPTH)) {
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
     * Returns a map of thread states to the percentage of all threads which are in that state.
     *
     * @return a map of thread states to percentages
     */
    public Map<State, Double> threadStatePercentages() {
        final Map<State, Double> conditions = new HashMap<State, Double>();
        for (State state : State.values()) {
            conditions.put(state, 0.0);
        }

        final long[] allThreadIds = getThreadMXBean().getAllThreadIds();
        final ThreadInfo[] allThreads = getThreadMXBean().getThreadInfo(allThreadIds);
        int liveCount = 0;
        for (ThreadInfo info : allThreads) {
            if (info != null) {
                final State state = info.getThreadState();
                conditions.put(state, conditions.get(state) + 1);
                liveCount++;
            }
        }
        for (State state : new ArrayList<State>(conditions.keySet())) {
            conditions.put(state, conditions.get(state) / liveCount);
        }

        return conditions;
    }

    /**
     * Dumps all of the threads' current information to an output stream.
     *
     * @param out an output stream
     */
    public void threadDump(OutputStream out) {
        final ThreadInfo[] threads = ManagementFactory.getThreadMXBean().dumpAllThreads(true, true);
        final PrintWriter writer = new PrintWriter(out, true);

        for (int ti = threads.length - 1; ti > 0; ti--) {
            final ThreadInfo t = threads[ti];
            writer.printf("%s id=%d state=%s",
                          t.getThreadName(),
                          t.getThreadId(),
                          t.getThreadState());
            final LockInfo lock = t.getLockInfo();
            if (lock != null && t.getThreadState() != Thread.State.BLOCKED) {
                writer.printf("\n    - waiting on <0x%08x> (a %s)",
                              lock.getIdentityHashCode(),
                              lock.getClassName());
                writer.printf("\n    - locked <0x%08x> (a %s)",
                              lock.getIdentityHashCode(),
                              lock.getClassName());
            } else if (lock != null && t.getThreadState() == Thread.State.BLOCKED) {
                writer.printf("\n    - waiting to lock <0x%08x> (a %s)",
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
