package com.codahale.metrics.jvm.jvmstat.monitors;

/**
 * Number of compilation tasks performed
 */
public class TotalCompilationTasksMetric extends JvmStatsMetric<Integer> {
    public String getMetricName() {
        return "sun.ci.totalCompiles";
    }
}
