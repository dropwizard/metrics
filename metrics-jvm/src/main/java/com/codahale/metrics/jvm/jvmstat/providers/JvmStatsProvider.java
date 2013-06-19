package com.codahale.metrics.jvm.jvmstat.providers;

import java.lang.management.ManagementFactory;

import sun.jvmstat.monitor.Monitor;
import sun.jvmstat.monitor.MonitorException;
import sun.jvmstat.monitor.MonitoredHost;
import sun.jvmstat.monitor.MonitoredVm;
import sun.jvmstat.monitor.VmIdentifier;
import sun.jvmstat.perfdata.monitor.protocol.file.MonitoredHostProvider;

/**
 * <p>
 * Basic abstraction over the data exposed by the JVM stat tools.
 * </p>
 * <p>
 * We actively poll the perf data stored by JvmStat. If required we could implement
 * the required interfaces in order to be notified periodically by the JvmStat tool.
 * Since the metrics-core is taking care of collecting the metrics in a periodic manner
 * this approach seems to fet better at this moment.
 * </p>
 * <p>
 * At this moment this abstraction only provides the ability to monitor
 * a local jvm.
 * </p>
 *
 * @see sun.jvmstat.monitor.event.VmListener
 */
public class JvmStatsProvider {
    /**
     * Reference to the VM which we are monitoring
     */
    private MonitoredVm monitoredVm;

    /**
     * Default constructor which establish a reference to a LOCAL
     * vm. The local JVM machine is determined through the
     * PID of the process which is running this class.
     */
    public JvmStatsProvider() {
        setUpMonitoredVm(null);
    }

    /**
     * Constructor which establish a reference to a LOCAL vm. The local
     * JVM machine to connect to is discovered through the pid parameter
     *
     * @param pid Id of the JVM process
     */
    public JvmStatsProvider(Integer pid) {
        setUpMonitoredVm(pid);
    }

    /**
     * Determine the PID of the JVM process which is running the current
     * class
     *
     * @return PID of the JVM process which is running the current class
     *
     * @see java.lang.management.ManagementFactory#getRuntimeMXBean()
     */
    private Integer getCurrentProcessPid() {
        String name = ManagementFactory.getRuntimeMXBean().getName();
        final String pid = name.substring(0, name.indexOf('@'));

        return Integer.valueOf(pid);
    }

    private void setUpMonitoredVm(Integer pid) {
        Integer currentPid = pid != null? pid : getCurrentProcessPid();

        try {
            VmIdentifier vmIdentifier = new VmIdentifier("//" + currentPid);
            MonitoredHost monitoredHost = MonitoredHostProvider.getMonitoredHost(vmIdentifier);

            this.monitoredVm = monitoredHost.getMonitoredVm(vmIdentifier);
        } catch (Exception e) {
        }
    }

    public Monitor getMetric(String metricName) {
        try {
            return this.monitoredVm.findByName(metricName);
        } catch (MonitorException e) {
            return null;
        }
    }
}
