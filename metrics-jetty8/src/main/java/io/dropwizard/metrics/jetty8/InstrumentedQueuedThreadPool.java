package io.dropwizard.metrics.jetty8;

import static io.dropwizard.metrics.MetricRegistry.name;

import org.eclipse.jetty.util.thread.QueuedThreadPool;

import io.dropwizard.metrics.Gauge;
import io.dropwizard.metrics.MetricRegistry;
import io.dropwizard.metrics.RatioGauge;

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
        registry.register(name(QueuedThreadPool.class, "jobs"), new Gauge<Integer>() {
            @Override
            public Integer getValue() {
                // This assumes the QueuedThreadPool is using a BlockingArrayQueue or
                // ArrayBlockingQueue for its queue, and is therefore a constant-time operation.
                return getQueue() != null ? getQueue().size() : 0;
            }
        });
        registry.register(name(QueuedThreadPool.class, "utilization-max"), new RatioGauge() {
            @Override
            protected Ratio getRatio() {
                return Ratio.of(getThreads() - getIdleThreads(), getMaxThreads());
            }
        });
    }
}
