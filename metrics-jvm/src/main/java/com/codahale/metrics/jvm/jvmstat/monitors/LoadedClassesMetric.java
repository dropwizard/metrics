package com.codahale.metrics.jvm.jvmstat.monitors;

/**
 * Numbers of classes loaded
 */
public class LoadedClassesMetric extends JvmStatsMetric<Long> {
    public String getMetricName() {
        return "java.cls.loadedClasses";
    }
}
