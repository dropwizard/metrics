package com.codahale.metrics.jvm;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricName;
import com.codahale.metrics.MetricSet;

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
        final Map<MetricName, Metric> gauges = new HashMap<MetricName, Metric>();

        gauges.put(MetricName.build("loaded"), new Gauge<Long>() {
            @Override
            public Long getValue() {
                return mxBean.getTotalLoadedClassCount();
            }
        });

        gauges.put(MetricName.build("unloaded"), new Gauge<Long>() {
            @Override
            public Long getValue() {
                return mxBean.getUnloadedClassCount();
            }
        });

        return gauges;
    }
}
