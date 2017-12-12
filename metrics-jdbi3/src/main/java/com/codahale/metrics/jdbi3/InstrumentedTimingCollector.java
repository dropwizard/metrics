package com.codahale.metrics.jdbi3;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jdbi3.strategies.SmartNameStrategy;
import com.codahale.metrics.jdbi3.strategies.StatementNameStrategy;
import org.jdbi.v3.core.statement.StatementContext;
import org.jdbi.v3.core.statement.TimingCollector;

import java.util.concurrent.TimeUnit;

/**
 * A {@link TimingCollector} implementation for JDBI which uses the SQL objects' class names and
 * method names for millisecond-precision timers.
 */
public class InstrumentedTimingCollector implements TimingCollector {

    private final MetricRegistry registry;
    private final StatementNameStrategy statementNameStrategy;

    public InstrumentedTimingCollector(MetricRegistry registry) {
        this(registry, new SmartNameStrategy());
    }

    public InstrumentedTimingCollector(MetricRegistry registry,
                                       StatementNameStrategy statementNameStrategy) {
        this.registry = registry;
        this.statementNameStrategy = statementNameStrategy;
    }

    @Override
    public void collect(long elapsedTime, StatementContext ctx) {
        String statementName = statementNameStrategy.getStatementName(ctx);
        if (statementName != null) {
            registry.timer(statementName).update(elapsedTime, TimeUnit.NANOSECONDS);
        }
    }
}
