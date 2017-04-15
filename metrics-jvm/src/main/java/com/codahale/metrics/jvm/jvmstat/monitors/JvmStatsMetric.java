package com.codahale.metrics.jvm.jvmstat.monitors;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.jvm.jvmstat.providers.JvmStatsProvider;

/**
 * Basic abstraction for all the metrics provided by the JvmStat tool
 * <p>
 * In order to add a new metric to the existing collection of metrics  we just need to
 * extend this class, returning the name of the metric we want to include.
 *
 * @see <a href="http://cr.openjdk.java.net/~jmasa/8004172/webrev.00/src/share/classes/sun/tools/jstat/resources/jstat_options.cdiff.html">JvmStats options</a>
 */
public abstract class JvmStatsMetric<T> implements Gauge<T> {

    private JvmStatsProvider jvmStatsProvider = new JvmStatsProvider();;

    public T getValue() {
        return (T) jvmStatsProvider.getMetric(getMetricName()).getValue();
    }

    public abstract String getMetricName();
}
