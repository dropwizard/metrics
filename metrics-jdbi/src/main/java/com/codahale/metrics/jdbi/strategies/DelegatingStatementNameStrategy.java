package com.codahale.metrics.jdbi.strategies;

import org.skife.jdbi.v2.StatementContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class DelegatingStatementNameStrategy implements StatementNameStrategy {
    private final List<StatementNameStrategy> strategies = new ArrayList<>();

    protected DelegatingStatementNameStrategy(StatementNameStrategy... strategies) {
        registerStrategies(strategies);
    }

    protected void registerStrategies(StatementNameStrategy... strategies) {
        this.strategies.addAll(Arrays.asList(strategies));
    }

    @Override
    public String getStatementName(StatementContext statementContext) {
        for (StatementNameStrategy strategy : strategies) {
            final String statementName = strategy.getStatementName(statementContext);
            if (statementName != null) {
                return statementName;
            }
        }

        return NameStrategies.UNKNOWN_SQL;
    }
}
