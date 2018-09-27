package io.dropwizard.metrics5.jdbi3;

import io.dropwizard.metrics5.MetricName;
import io.dropwizard.metrics5.MetricRegistry;
import io.dropwizard.metrics5.jdbi3.strategies.SmartNameStrategy;
import io.dropwizard.metrics5.jdbi3.strategies.StatementNameStrategy;
import org.jdbi.v3.core.statement.SqlLogger;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.SQLException;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

/**
 * A {@link SqlLogger} implementation for JDBI which uses the SQL objects' class names and
 * method names for millisecond-precision timers.
 */
public class InstrumentedSqlLogger implements SqlLogger {
    private final MetricRegistry registry;
    private final StatementNameStrategy statementNameStrategy;

    public InstrumentedSqlLogger(MetricRegistry registry) {
        this(registry, new SmartNameStrategy());
    }

    public InstrumentedSqlLogger(MetricRegistry registry, StatementNameStrategy statementNameStrategy) {
        this.registry = registry;
        this.statementNameStrategy = statementNameStrategy;
    }

    @Override
    public void logAfterExecution(StatementContext context) {
        log(context);
    }

    @Override
    public void logException(StatementContext context, SQLException ex) {
        log(context);
    }

    private void log(StatementContext context) {
        MetricName statementName = statementNameStrategy.getStatementName(context);
        if (statementName != null) {
            final long elapsed = context.getElapsedTime(ChronoUnit.NANOS);
            registry.timer(statementName).update(elapsed, TimeUnit.NANOSECONDS);
        }
    }
}
