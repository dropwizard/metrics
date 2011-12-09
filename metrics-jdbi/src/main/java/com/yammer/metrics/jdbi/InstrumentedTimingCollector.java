package com.yammer.metrics.jdbi;

import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.core.TimerMetric;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.TimingCollector;

import java.util.concurrent.TimeUnit;

/**
 * A {@link TimingCollector} implementation for JDBI which uses the SQL objects' class names and
 * method names for millisecond-precision timers.
 */
public class InstrumentedTimingCollector implements TimingCollector {
    private final MetricsRegistry registry;
    private final TimerMetric defaultTimer;

    public InstrumentedTimingCollector(MetricsRegistry registry, Class<?> klass) {
        this.registry = registry;
        this.defaultTimer = registry.newTimer(klass, "raw-sql");
    }

    @Override
    public void collect(long elapsedTime, StatementContext ctx) {
        final TimerMetric timer = getTimer(ctx);
        timer.update(elapsedTime, TimeUnit.NANOSECONDS);
    }

    private TimerMetric getTimer(StatementContext ctx) {
        if ((ctx.getSqlObjectType() == null) || (ctx.getSqlObjectMethod() == null)) {
            return defaultTimer;
        }
        return registry.newTimer(ctx.getSqlObjectType(), ctx.getSqlObjectMethod().getName());
    }
}
