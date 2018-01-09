package io.dropwizard.metrics5.jdbi.strategies;

import io.dropwizard.metrics5.MetricName;
import org.skife.jdbi.v2.StatementContext;

/**
 * Interface for strategies to statement contexts to metric names.
 */
public interface StatementNameStrategy {
    MetricName getStatementName(StatementContext statementContext);
}
