package com.yammer.metrics.jetty8;

import com.yammer.metrics.Gauge;
import com.yammer.metrics.MetricRegistry;
import com.yammer.metrics.RatioGauge;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import static com.yammer.metrics.MetricRegistry.name;

public class InstrumentedQueuedThreadPool extends QueuedThreadPool {
    public InstrumentedQueuedThreadPool(MetricRegistry registry) {
        super();
        registry.register(name(QueuedThreadPool.class, "percent-idle"), new RatioGauge() {
            @Override
            protected Ratio getRatio() {
                return Ratio.of(getIdleThreads(),
                                getThreads());
            }
        });
        registry.register(name(QueuedThreadPool.class, "active-threads"), new Gauge<Integer>() {
            @Override
            public Integer getValue() {
                return getThreads();
            }
        });
        registry.register(name(QueuedThreadPool.class, "idle-threads"), new Gauge<Integer>() {
            @Override
            public Integer getValue() {
                return getIdleThreads();
            }
        });
    }
}
