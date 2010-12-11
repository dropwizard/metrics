package com.yammer.metrics.core;

import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import static java.lang.management.ManagementFactory.*;

/**
 * A collection of Java Virtual Machine metrics.
 *
 * @author coda
 */
public class VirtualMachineMetrics {
	static class GcMonitor implements Runnable {
		private final Map<String, Long> gcTimestamps = new ConcurrentHashMap<String, Long>();
		private final Map<String, TimerMetric> gcTimers = new ConcurrentHashMap<String, TimerMetric>();
		private final Map<String, MeterMetric> gcMeters = new ConcurrentHashMap<String, MeterMetric>();
		private final Class<?> gcBeanClass;
		private final List<Object> beans = new ArrayList<Object>();

		public GcMonitor() throws Exception {
			Class.forName("com.sun.management.GcInfo");
			this.gcBeanClass = Class.forName("com.sun.management.GarbageCollectorMXBean");
			final ObjectName gcName = new ObjectName(GARBAGE_COLLECTOR_MXBEAN_DOMAIN_TYPE + ",*");
			final MBeanServer serve = getPlatformMBeanServer();
			final Set<ObjectName> names = serve.queryNames(gcName, null);
			for (ObjectName name : names) {
				beans.add(newPlatformMXBeanProxy(serve, name.getCanonicalName(), gcBeanClass));
			}

		}

		@Override
		@SuppressWarnings("unchecked")
		public void run() {
			try {
				for (Object bean : beans) {
					final Method getGcInfo = bean.getClass().getDeclaredMethod("getLastGcInfo");
					final Object gcInfo = getGcInfo.invoke(bean);
					if (gcInfo != null) {
						final Long duration = (Long) gcInfo.getClass().getDeclaredMethod("getDuration").invoke(gcInfo);
						final String name = (String) bean.getClass().getDeclaredMethod("getName").invoke(bean);
						final Long timestamp = (Long) bean.getClass().getDeclaredMethod("getCollectionTime").invoke(bean);
						final Long lastTimestamp = gcTimestamps.get(name);

						if (lastTimestamp == null || timestamp > lastTimestamp) {
							collectGcDuration(name, duration);
							collectGcThroughput(name, gcInfo);
							gcTimestamps.put(name, timestamp);
						}
					}
				}
			} catch (Exception ignored) {

			}
		}

		@SuppressWarnings("unchecked")
		private void collectGcThroughput(String name, Object gcInfo) throws Exception {
			MeterMetric meter = gcMeters.get(name);
			if (meter == null) {
				meter = MeterMetric.newMeter(TimeUnit.SECONDS);
				gcMeters.put(name, meter);
			}

			final Map<String, MemoryUsage> beforeUsages = (Map<String, MemoryUsage>) gcInfo.getClass().getDeclaredMethod("getMemoryUsageBeforeGc").invoke(gcInfo);
			final Map<String, MemoryUsage> afterUsages = (Map<String, MemoryUsage>) gcInfo.getClass().getDeclaredMethod("getMemoryUsageAfterGc").invoke(gcInfo);

			long memoryCollected = 0;
			for (MemoryUsage memoryUsage : beforeUsages.values()) {
				memoryCollected += memoryUsage.getUsed();
			}
			for (MemoryUsage memoryUsage : afterUsages.values()) {
				memoryCollected -= memoryUsage.getUsed();
			}

			meter.mark(memoryCollected);
		}

		private void collectGcDuration(String name, Long duration) {
			TimerMetric timer = gcTimers.get(name);
			if (timer == null) {
				timer = new TimerMetric(TimeUnit.MILLISECONDS, TimeUnit.HOURS);
				gcTimers.put(name, timer);
			}

			timer.update(duration, TimeUnit.MILLISECONDS);
		}
	}

	private static final ScheduledExecutorService MONITOR_THREAD = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("gc-monitor"));
	private static final GcMonitor GC_MONITOR = initializeGcMonitor();
	private static GcMonitor initializeGcMonitor() {
		try {
			final GcMonitor monitor = new GcMonitor();
			MONITOR_THREAD.scheduleAtFixedRate(monitor, 0, 5, TimeUnit.SECONDS);
			return monitor;
		} catch (Exception e) {
			return null;
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
			pools.put(bean.getName(), bean.getUsage().getUsed() / (double) bean.getUsage().getMax());
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
	@SuppressWarnings("unchecked")
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
	 * Returns a map of garbage collector names to {@link TimerMetric} instances
	 * which record GC run duration metrics.
	 *
	 * @return a map of garbage collector names to {@link TimerMetric} instances
	 */
	public static Map<String, TimerMetric> gcDurations() {
		if (GC_MONITOR == null) {
			return Collections.emptyMap();
		}
		return GC_MONITOR.gcTimers;
	}

	/**
	 * Returns a map of garbage collector names to {@link MeterMetric} instances
	 * which record the throughput of GC collection in bytes.
	 *
	 * @return a map of garbage collector names to {@link MeterMetric} instances
	 */
	public static Map<String, MeterMetric> gcThroughputs() {
		if (GC_MONITOR == null) {
			return Collections.emptyMap();
		}
		return GC_MONITOR.gcMeters;
	}
}
