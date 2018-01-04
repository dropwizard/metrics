package com.codahale.metrics.jdbi3.strategies;

import com.codahale.metrics.MetricName;
import org.jdbi.v3.core.statement.StatementContext;

/**
 * Interface for strategies to statement contexts to metric names.
 */
@FunctionalInterface
public interface StatementNameStrategy {

    MetricName getStatementName(StatementContext statementContext);
}
