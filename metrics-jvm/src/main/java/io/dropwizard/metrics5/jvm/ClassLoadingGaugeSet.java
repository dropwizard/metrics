package io.dropwizard.metrics5.jvm;

import io.dropwizard.metrics5.Gauge;
import io.dropwizard.metrics5.Metric;
import io.dropwizard.metrics5.MetricName;
import io.dropwizard.metrics5.MetricSet;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;

/**
 * A set of gauges for JVM classloader usage.
 */
public class ClassLoadingGaugeSet implements MetricSet {

    private final ClassLoadingMXBean mxBean;

    public ClassLoadingGaugeSet() {
        this(ManagementFactory.getClassLoadingMXBean());
    }

    public ClassLoadingGaugeSet(ClassLoadingMXBean mxBean) {
        this.mxBean = mxBean;
    }

    @Override
    public Map<MetricName, Metric> getMetrics() {
        final Map<MetricName, Metric> gauges = new HashMap<>();
        gauges.put(MetricName.build("loaded"), (Gauge<Long>) mxBean::getTotalLoadedClassCount);
        gauges.put(MetricName.build("unloaded"), (Gauge<Long>) mxBean::getUnloadedClassCount);

        return gauges;
    }
}
