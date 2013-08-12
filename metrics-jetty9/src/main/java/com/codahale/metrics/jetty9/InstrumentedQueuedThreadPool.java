package com.codahale.metrics.jetty9;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.RatioGauge;
import org.eclipse.jetty.util.annotation.Name;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import java.util.concurrent.BlockingQueue;

import static com.codahale.metrics.MetricRegistry.name;

public class InstrumentedQueuedThreadPool extends QueuedThreadPool {
    private final MetricRegistry metricRegistry;

    public InstrumentedQueuedThreadPool(@Name("registry") MetricRegistry registry) {
        this(registry, 200);
    }

    public InstrumentedQueuedThreadPool(@Name("registry") MetricRegistry registry,
                                        @Name("maxThreads") int maxThreads) {
        this(registry, maxThreads, 8);
    }

    public InstrumentedQueuedThreadPool(@Name("registry") MetricRegistry registry,
                                        @Name("maxThreads") int maxThreads,
                                        @Name("minThreads") int minThreads) {
        this(registry, maxThreads, minThreads, 60000);
    }

    public InstrumentedQueuedThreadPool(@Name("registry") MetricRegistry registry,
                                        @Name("maxThreads") int maxThreads,
                                        @Name("minThreads") int minThreads,
                                        @Name("idleTimeout") int idleTimeout) {
        this(registry, maxThreads, minThreads, idleTimeout, null);
    }

    public InstrumentedQueuedThreadPool(@Name("registry") MetricRegistry registry,
                                        @Name("maxThreads") int maxThreads,
                                        @Name("minThreads") int minThreads,
                                        @Name("idleTimeout") int idleTimeout,
                                        @Name("queue") BlockingQueue<Runnable> queue) {
        super(maxThreads, minThreads, idleTimeout, queue);
        this.metricRegistry = registry;
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();
        metricRegistry.register(name(QueuedThreadPool.class, getName(), "utilization"), new RatioGauge() {
            @Override
            protected Ratio getRatio() {
                return Ratio.of(getThreads() - getIdleThreads(), getThreads());
            }
        });
        metricRegistry.register(name(QueuedThreadPool.class, getName(), "utilization-max"), new RatioGauge() {
            @Override
            protected Ratio getRatio() {
                return Ratio.of(getThreads() - getIdleThreads(), getMaxThreads());
            }
        });
        metricRegistry.register(name(QueuedThreadPool.class, getName(), "size"), new Gauge<Integer>() {
            @Override
            public Integer getValue() {
                return getThreads();
            }
        });
        metricRegistry.register(name(QueuedThreadPool.class, getName(), "jobs"), new Gauge<Integer>() {
            @Override
            public Integer getValue() {
                // This assumes the QueuedThreadPool is using a BlockingArrayQueue or
                // ArrayBlockingQueue for its queue, and is therefore a constant-time operation.
                return getQueue().size();
            }
        });
    }
}
