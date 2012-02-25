package com.yammer.metrics.core;

import javax.management.*;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.Thread.State;
import java.lang.management.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * A collection of Java Virtual Machine metrics.
 */
public class VirtualMachineMetrics {
    private static final int MAX_STACK_TRACE_DEPTH = 100;

    private static final VirtualMachineMetrics INSTANCE = new VirtualMachineMetrics(
            ManagementFactory.getMemoryMXBean(),
            ManagementFactory.getMemoryPoolMXBeans(),
            ManagementFactory.getOperatingSystemMXBean(),
            ManagementFactory.getThreadMXBean(),
            ManagementFactory.getGarbageCollectorMXBeans(),
            ManagementFactory.getRuntimeMXBean(),
            ManagementFactory.getPlatformMBeanServer());

    /**
     * The default instance of {@link VirtualMachineMetrics}.
     *
     * @return the default {@link VirtualMachineMetrics instance}
     */
    public static VirtualMachineMetrics getInstance() {
        return INSTANCE;
    }

    /**
     * Per-GC statistics.
     */
    public static class GarbageCollectorStats {
        private final long runs, timeMS;

        private GarbageCollectorStats(long runs, long timeMS) {
            this.runs = runs;
            this.timeMS = timeMS;
        }

        /**
         * Returns the number of times the garbage collector has run.
         *
         * @return the number of times the garbage collector has run
         */
        public long getRuns() {
            return runs;
        }

        /**
         * Returns the amount of time in the given unit the garbage collector has taken in total.
         *
         * @param unit    the time unit for the return value
         * @return the amount of time in the given unit the garbage collector
         */
        public long getTime(TimeUnit unit) {
            return unit.convert(timeMS, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * The management interface for a buffer pool, for example a pool of {@link
     * java.nio.ByteBuffer#allocateDirect direct} or {@link java.nio.MappedByteBuffer mapped}
     * buffers.
     */
    public static class BufferPoolStats {
        private final long count, memoryUsed, totalCapacity;

        private BufferPoolStats(long count, long memoryUsed, long totalCapacity) {
            this.count = count;
            this.memoryUsed = memoryUsed;
            this.totalCapacity = totalCapacity;
        }

        /**
         * Returns an estimate of the number of buffers in the pool.
         *
         * @return An estimate of the number of buffers in this pool
         */
        public long getCount() {
            return count;
        }

        /**
         * Returns an estimate of the memory that the Java virtual machine is using for this buffer
         * pool. The value returned by this method may differ from the estimate of the total {@link
         * #getTotalCapacity capacity} of the buffers in this pool. This difference is explained by
         * alignment, memory allocator, and other implementation specific reasons.
         *
         * @return An estimate of the memory that the Java virtual machine is using for this buffer
         *         pool in bytes, or {@code -1L} if an estimate of the memory usage is not
         *         available
         */
        public long getMemoryUsed() {
            return memoryUsed;
        }

        /**
         * Returns an estimate of the total capacity of the buffers in this pool. A buffer's
         * capacity is the number of elements it contains and the value returned by this method is
         * an estimate of the total capacity of buffers in the pool in bytes.
         *
         * @return An estimate of the total capacity of the buffers in this pool in bytes
         */
        public long getTotalCapacity() {
            return totalCapacity;
        }
    }

    private final MemoryMXBean memory;
    private final List<MemoryPoolMXBean> memoryPools;
    private final OperatingSystemMXBean os;
    private final ThreadMXBean threads;
    private final List<GarbageCollectorMXBean> garbageCollectors;
    private final RuntimeMXBean runtime;
    private final MBeanServer mBeanServer;

    VirtualMachineMetrics(MemoryMXBean memory,
                          List<MemoryPoolMXBean> memoryPools,
                          OperatingSystemMXBean os,
                          ThreadMXBean threads,
                          List<GarbageCollectorMXBean> garbageCollectors,
                          RuntimeMXBean runtime, MBeanServer mBeanServer) {
        this.memory = memory;
        this.memoryPools = memoryPools;
        this.os = os;
        this.threads = threads;
        this.garbageCollectors = garbageCollectors;
        this.runtime = runtime;
        this.mBeanServer = mBeanServer;
    }

    /**
     * Returns the total initial memory of the current JVM.
     *
     * @return total Heap and non-heap initial JVM memory in bytes.
     */
    public double totalInit() {
        return memory.getHeapMemoryUsage().getInit() +
                memory.getNonHeapMemoryUsage().getInit();
    }

    /**
     * Returns the total memory currently used by the current JVM.
     *
     * @return total Heap and non-heap memory currently used by JVM in bytes.
     */
    public double totalUsed() {
        return memory.getHeapMemoryUsage().getUsed() +
                memory.getNonHeapMemoryUsage().getUsed();
    }

    /**
     * Returns the total memory currently used by the current JVM.
     *
     * @return total Heap and non-heap memory currently used by JVM in bytes.
     */
    public double totalMax() {
        return memory.getHeapMemoryUsage().getMax() +
                memory.getNonHeapMemoryUsage().getMax();
    }
    /**
     * Returns the total memory committed to the JVM.
     *
     * @return total Heap and non-heap memory currently committed to the JVM in bytes.
     */
    public double totalCommitted() {
        return memory.getHeapMemoryUsage().getCommitted() +
                memory.getNonHeapMemoryUsage().getCommitted();
    }
    /**
     * Returns the heap initial memory of the current JVM.
     *
     * @return Heap initial JVM memory in bytes.
     */
    public double heapInit() {
        return memory.getHeapMemoryUsage().getInit();
    }
    /**
     * Returns the heap memory currently used by the current JVM.
     *
     * @return Heap memory currently used by JVM in bytes.
     */
    public double heapUsed() {
        return memory.getHeapMemoryUsage().getUsed();
    }
    /**
     * Returns the heap memory currently used by the current JVM.
     *
     * @return Heap memory currently used by JVM in bytes.
     */
    public double heapMax() {
        return memory.getHeapMemoryUsage().getMax();
    }
    /**
     * Returns the heap memory committed to the JVM.
     *
     * @return Heap memory currently committed to the JVM in bytes.
     */
    public double heapCommitted() {
        return memory.getHeapMemoryUsage().getCommitted();
    }

    /**
     * Returns the percentage of the JVM's heap which is being used.
     *
     * @return the percentage of the JVM's heap which is being used
     */
    public double heapUsage() {
        final MemoryUsage usage = memory.getHeapMemoryUsage();
        return usage.getUsed() / (double) usage.getMax();
    }

    /**
     * Returns the percentage of the JVM's non-heap memory (e.g., direct buffers) which is being
     * used.
     *
     * @return the percentage of the JVM's non-heap memory which is being used
     */
    public double nonHeapUsage() {
        final MemoryUsage usage = memory.getNonHeapMemoryUsage();
        return usage.getUsed() / (double) usage.getMax();
    }

    /**
     * Returns a map of memory pool names to the percentage of that pool which is being used.
     *
     * @return a map of memory pool names to the percentage of that pool which is being used
     */
    public Map<String, Double> memoryPoolUsage() {
        final Map<String, Double> pools = new TreeMap<String, Double>();
        for (MemoryPoolMXBean pool : memoryPools) {
            final double max = pool.getUsage().getMax() == -1 ?
                    pool.getUsage().getCommitted() :
                    pool.getUsage().getMax();
            pools.put(pool.getName(), pool.getUsage().getUsed() / max);
        }
        return Collections.unmodifiableMap(pools);
    }

    /**
     * Returns the percentage of available file descriptors which are currently in use.
     *
     * @return the percentage of available file descriptors which are currently in use, or {@code
     *         NaN} if the running JVM does not have access to this information
     */
    public double fileDescriptorUsage() {
        try {
            final Method getOpenFileDescriptorCount = os.getClass().getDeclaredMethod("getOpenFileDescriptorCount");
            getOpenFileDescriptorCount.setAccessible(true);
            final Long openFds = (Long) getOpenFileDescriptorCount.invoke(os);
            final Method getMaxFileDescriptorCount = os.getClass().getDeclaredMethod("getMaxFileDescriptorCount");
            getMaxFileDescriptorCount.setAccessible(true);
            final Long maxFds = (Long) getMaxFileDescriptorCount.invoke(os);
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
        return TimeUnit.MILLISECONDS.toSeconds(runtime.getUptime());
    }

    /**
     * Returns the number of live threads (includes {@link #daemonThreadCount()}.
     *
     * @return the number of live threads
     */
    public int threadCount() {
        return threads.getThreadCount();
    }

    /**
     * Returns the number of live daemon threads.
     *
     * @return the number of live daemon threads
     */
    public int daemonThreadCount() {
        return threads.getDaemonThreadCount();
    }

    /**
     * Returns a map of garbage collector names to garbage collector information.
     *
     * @return a map of garbage collector names to garbage collector information
     */
    public Map<String, GarbageCollectorStats> garbageCollectors() {
        final Map<String, GarbageCollectorStats> stats = new HashMap<String, GarbageCollectorStats>();
        for (GarbageCollectorMXBean gc : garbageCollectors) {
            stats.put(gc.getName(),
                      new GarbageCollectorStats(gc.getCollectionCount(),
                                                gc.getCollectionTime()));
        }
        return Collections.unmodifiableMap(stats);
    }

    /**
     * Returns a set of strings describing deadlocked threads, if any are deadlocked.
     *
     * @return a set of any deadlocked threads
     */
    public Set<String> deadlockedThreads() {
        final long[] threadIds = threads.findDeadlockedThreads();
        if (threadIds != null) {
            final Set<String> threads = new HashSet<String>();
            for (ThreadInfo info : this.threads.getThreadInfo(threadIds, MAX_STACK_TRACE_DEPTH)) {
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
            return Collections.unmodifiableSet(threads);
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

        final long[] allThreadIds = threads.getAllThreadIds();
        final ThreadInfo[] allThreads = threads.getThreadInfo(allThreadIds);
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

        return Collections.unmodifiableMap(conditions);
    }

    /**
     * Dumps all of the threads' current information to an output stream.
     *
     * @param out an output stream
     */
    public void threadDump(OutputStream out) {
        final ThreadInfo[] threads = this.threads.dumpAllThreads(true, true);
        final PrintWriter writer = new PrintWriter(out, true);

        for (int ti = threads.length - 1; ti >= 0; ti--) {
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
    
    public Map<String, BufferPoolStats> getBufferPoolStats() {
        try {
            final String[] attributes = { "Count", "MemoryUsed", "TotalCapacity" };

            final ObjectName direct = new ObjectName("java.nio:type=BufferPool,name=direct");
            final ObjectName mapped = new ObjectName("java.nio:type=BufferPool,name=mapped");

            final AttributeList directAttributes = mBeanServer.getAttributes(direct, attributes);
            final AttributeList mappedAttributes = mBeanServer.getAttributes(mapped, attributes);

            final Map<String, BufferPoolStats> stats = new TreeMap<String, BufferPoolStats>();

            final BufferPoolStats directStats = new BufferPoolStats((Long) ((Attribute) directAttributes.get(0)).getValue(),
                                                                    (Long) ((Attribute) directAttributes.get(1)).getValue(),
                                                                    (Long) ((Attribute) directAttributes.get(2)).getValue());

            stats.put("direct", directStats);

            final BufferPoolStats mappedStats = new BufferPoolStats((Long) ((Attribute) mappedAttributes.get(0)).getValue(),
                                                                    (Long) ((Attribute) mappedAttributes.get(1)).getValue(),
                                                                    (Long) ((Attribute) mappedAttributes.get(2)).getValue());

            stats.put("mapped", mappedStats);

            return Collections.unmodifiableMap(stats);
        } catch (JMException e) {
            return Collections.emptyMap();
        }
    }
}
