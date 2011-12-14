package com.yammer.metrics.jdbi.strategies;

import org.skife.jdbi.v2.StatementContext;

import com.yammer.metrics.core.MetricName;

/**
 * Interface for strategies to statement contexts to metric names.
 */
public interface StatementNameStrategy {
    MetricName getStatementName(StatementContext statementContext);
}
