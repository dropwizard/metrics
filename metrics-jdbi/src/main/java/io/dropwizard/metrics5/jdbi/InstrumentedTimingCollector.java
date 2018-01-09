package io.dropwizard.metrics5.jdbi;

import io.dropwizard.metrics5.MetricRegistry;
import io.dropwizard.metrics5.Timer;
import io.dropwizard.metrics5.jdbi.strategies.SmartNameStrategy;
import io.dropwizard.metrics5.jdbi.strategies.StatementNameStrategy;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.TimingCollector;

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
        final Timer timer = getTimer(ctx);
        timer.update(elapsedTime, TimeUnit.NANOSECONDS);
    }

    private Timer getTimer(StatementContext ctx) {
        return registry.timer(statementNameStrategy.getStatementName(ctx));
    }
}
