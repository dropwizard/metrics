package com.yammer.metrics.jdbi.strategies;

import org.skife.jdbi.v2.StatementContext;

/**
 * Interface for strategies to statement contexts to metric names.
 */
public interface StatementNameStrategy {
    String getStatementName(StatementContext statementContext);
}
