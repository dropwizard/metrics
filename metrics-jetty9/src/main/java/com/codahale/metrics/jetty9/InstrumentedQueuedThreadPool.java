package com.codahale.metrics.jetty9;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.RatioGauge;
import org.eclipse.jetty.util.annotation.Name;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import java.util.concurrent.BlockingQueue;

import static com.codahale.metrics.MetricRegistry.name;

public class InstrumentedQueuedThreadPool extends QueuedThreadPool {
    public InstrumentedQueuedThreadPool(@Name("registry") MetricRegistry registry,
                                        @Name("name") String name) {
        this(registry, name, 200);
    }

    public InstrumentedQueuedThreadPool(@Name("registry") MetricRegistry registry,
                                        @Name("name") String name,
                                        @Name("maxThreads") int maxThreads) {
        this(registry, name, maxThreads, 8);
    }

    public InstrumentedQueuedThreadPool(@Name("registry") MetricRegistry registry,
                                        @Name("name") String name,
                                        @Name("maxThreads") int maxThreads,
                                        @Name("minThreads") int minThreads) {
        this(registry, name, maxThreads, minThreads, 60000);
    }

    public InstrumentedQueuedThreadPool(@Name("registry") MetricRegistry registry,
                                        @Name("name") String name,
                                        @Name("maxThreads") int maxThreads,
                                        @Name("minThreads") int minThreads,
                                        @Name("idleTimeout") int idleTimeout) {
        this(registry, name, maxThreads, minThreads, idleTimeout, null);
    }

    public InstrumentedQueuedThreadPool(@Name("registry") MetricRegistry registry,
                                        @Name("name") String name,
                                        @Name("maxThreads") int maxThreads,
                                        @Name("minThreads") int minThreads,
                                        @Name("idleTimeout") int idleTimeout,
                                        @Name("queue") BlockingQueue<Runnable> queue) {
        super(maxThreads, minThreads, idleTimeout, queue);

        registry.register(name(QueuedThreadPool.class, name, "utilization"), new RatioGauge() {
            @Override
            protected Ratio getRatio() {
                return Ratio.of(getThreads() - getIdleThreads(), getThreads());
            }
        });
        registry.register(name(QueuedThreadPool.class, name, "size"), new Gauge<Integer>() {
            @Override
            public Integer getValue() {
                return getThreads();
            }
        });
        registry.register(name(QueuedThreadPool.class, name, "jobs"), new Gauge<Integer>() {
            @Override
            public Integer getValue() {
                // This assumes the QueuedThreadPool is using a BlockingArrayQueue or
                // ArrayBlockingQueue for its queue, and is therefore a constant-time operation.
                return getQueue().size();
            }
        });

        setName(name);
    }
}
