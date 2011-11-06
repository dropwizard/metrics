package com.yammer.metrics.jetty;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.GaugeMetric;
import com.yammer.metrics.core.MetricsRegistry;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

public class InstrumentedQueuedThreadPool extends QueuedThreadPool {
    public InstrumentedQueuedThreadPool() {
        this(Metrics.defaultRegistry());
    }

    public InstrumentedQueuedThreadPool(MetricsRegistry registry) {
        super();
        registry.newGauge(QueuedThreadPool.class, "percent-idle", new GaugeMetric<Integer>() {
            @Override
            public Integer value() {
                final double percent = getThreads() > 0 ?
                        getIdleThreads() / ((double) getThreads()) :
                        0.0;
                return (int) (percent * 100);
            }
        });
        registry.newGauge(QueuedThreadPool.class, "active-threads", new GaugeMetric<Integer>() {
            @Override
            public Integer value() {
                return getThreads();
            }
        });
        registry.newGauge(QueuedThreadPool.class, "idle-threads", new GaugeMetric<Integer>() {
            @Override
            public Integer value() {
                return getIdleThreads();
            }
        });
    }
}
