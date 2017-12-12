package com.codahale.metrics.jdbi3.strategies;

import org.jdbi.v3.core.statement.StatementContext;

import java.util.Arrays;
import java.util.List;

public abstract class DelegatingStatementNameStrategy implements StatementNameStrategy {

    /**
     * Unknown SQL.
     */
    private static final String UNKNOWN_SQL = "sql.unknown";

    private final List<StatementNameStrategy> strategies;

    protected DelegatingStatementNameStrategy(StatementNameStrategy... strategies) {
        this.strategies = Arrays.asList(strategies);
    }

    @Override
    public String getStatementName(StatementContext statementContext) {
        for (StatementNameStrategy strategy : strategies) {
            final String statementName = strategy.getStatementName(statementContext);
            if (statementName != null) {
                return statementName;
            }
        }

        return UNKNOWN_SQL;
    }
}
