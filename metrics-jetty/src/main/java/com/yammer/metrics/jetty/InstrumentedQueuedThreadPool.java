package com.yammer.metrics.jetty;

import com.yammer.metrics.util.RatioGauge;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.core.MetricsRegistry;

public class InstrumentedQueuedThreadPool extends QueuedThreadPool {
    public InstrumentedQueuedThreadPool() {
        this(Metrics.defaultRegistry());
    }

    public InstrumentedQueuedThreadPool(MetricsRegistry registry) {
        super();
        registry.newGauge(QueuedThreadPool.class, "percent-idle", new RatioGauge() {
            @Override
            protected double getNumerator() {
                return getIdleThreads();
            }

            @Override
            protected double getDenominator() {
                return getThreads();
            }
        });
        registry.newGauge(QueuedThreadPool.class, "active-threads", new Gauge<Integer>() {
            @Override
            public Integer value() {
                return getThreads();
            }
        });
        registry.newGauge(QueuedThreadPool.class, "idle-threads", new Gauge<Integer>() {
            @Override
            public Integer value() {
                return getIdleThreads();
            }
        });
    }
}
