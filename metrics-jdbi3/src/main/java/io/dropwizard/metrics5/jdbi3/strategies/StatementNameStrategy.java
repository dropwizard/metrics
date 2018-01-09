package io.dropwizard.metrics5.jdbi3.strategies;

import io.dropwizard.metrics5.MetricName;
import org.jdbi.v3.core.statement.StatementContext;

/**
 * Interface for strategies to statement contexts to metric names.
 */
@FunctionalInterface
public interface StatementNameStrategy {

    MetricName getStatementName(StatementContext statementContext);
}
