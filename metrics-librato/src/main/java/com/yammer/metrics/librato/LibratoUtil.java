package com.yammer.metrics.librato;

import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.VirtualMachineMetrics;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * User: mihasya
 * Date: 6/14/12
 * Time: 2:05 PM
 * keeping general Librato utilities out of the way
 */
public class LibratoUtil {
    /**
     * turn a MetricName into a Librato-able string key
     */
    public String nameToString(MetricName name) {
        StringBuilder builder = new StringBuilder();
        builder
                .append(name.getGroup()).append(".")
                .append(name.getType()).append(".")
                .append(name.getName());

        if (name.hasScope()) {
            builder.append(".").append(name.getScope());
        }

        return builder.toString();
    }

    /**
     * helper method for adding VM metrics to a batch
     */
    public void addVmMetricsToBatch(VirtualMachineMetrics vm, LibratoBatch batch) {
        // memory
        batch.addGaugeMeasurement("jvm.memory.heap_usage", vm.getHeapUsage());
        batch.addGaugeMeasurement("jvm.memory.non_heap_usage", vm.getNonHeapUsage());
        for (Map.Entry<String, Double> pool : vm.getMemoryPoolUsage().entrySet()) {
            batch.addGaugeMeasurement("jvm.memory.memory_pool_usages."+pool.getKey(), pool.getValue());
        }

        // threads
        batch.addGaugeMeasurement("jvm.daemon_thread_count", vm.getDaemonThreadCount());
        batch.addGaugeMeasurement("jvm.thread_count", vm.getThreadCount());
        batch.addGaugeMeasurement("jvm.uptime", vm.getUptime());
        batch.addGaugeMeasurement("jvm.fd_usage", vm.getFileDescriptorUsage());

        for (Map.Entry<Thread.State, Double> entry : vm.getThreadStatePercentages().entrySet()) {
            batch.addGaugeMeasurement("jvm.thread-states." + entry.getKey().toString().toLowerCase(), entry.getValue());
        }

        // garbage collection
        for (Map.Entry<String, VirtualMachineMetrics.GarbageCollectorStats> entry : vm.getGarbageCollectors().entrySet()) {
            final String name = "jvm.gc." + entry.getKey();
            batch.addCounterMeasurement(name +".time", entry.getValue().getTime(TimeUnit.MILLISECONDS));
            batch.addCounterMeasurement(name +".runs", entry.getValue().getRuns());
        }
    }

    public static interface Sanitizer {
        public String apply(String name);
    }

    private static Pattern disallowed = Pattern.compile("([^A-Za-z0-9.:-_]|[\\[\\]])");
    private static int LENGTH_LIMIT = 256;

    /**
     * Metric names restrictions are described <a href="http://dev.librato.com/v1/metrics">here</a>.
     */
    public static final Sanitizer lastPassSanitizer = new Sanitizer() {
        // not sure I understand why brackets need to be specified separately, but behavior is the same in python
        @Override
        public String apply(String name) {
            name = disallowed.matcher(name).replaceAll("");
            if (name.length() > LENGTH_LIMIT) {
                name = name.substring(name.length() - LENGTH_LIMIT, name.length());
            }
            return name;
        }
    };

    /**
     * a stub to be used when the user doesn't specify a sanitizer
     */
    public static final Sanitizer noopSanitizer = new Sanitizer() {
        @Override
        public String apply(String name) {
            return name;
        }
    };
}
