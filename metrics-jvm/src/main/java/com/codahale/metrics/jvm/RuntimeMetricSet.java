package com.codahale.metrics.jvm;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricSet;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.HashMap;
import java.util.Map;

/**
 * A block-level metric set including runtime metrics.
 */
@Component
public class RuntimeMetricSet implements MetricSet {

    public Map<String, Metric> getMetrics() {
        final Map<String, Metric> metrics = new HashMap<>();
        final RuntimeMXBean mxBean = ManagementFactory.getRuntimeMXBean();

        metrics.put("startTime", new Gauge<Long>(){
            public Long getValue() {
                return mxBean.getStartTime();
            }
        });
        metrics.put("upTime", new Gauge<Long>(){
            public Long getValue() {
                return mxBean.getUptime();
            }
        });

        return metrics;
    }

}
