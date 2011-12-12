package com.yammer.metrics.jdbi.strategies;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.skife.jdbi.v2.StatementContext;

import com.yammer.metrics.core.MetricName;

public abstract class DelegatingStatementNameStrategy implements StatementNameStrategy
{
    private final List<StatementNameStrategy> strategies = new ArrayList<StatementNameStrategy>();

    protected DelegatingStatementNameStrategy(final StatementNameStrategy ... strategies)
    {
        registerStrategies(strategies);
    }

    protected void registerStrategies(final StatementNameStrategy ... strategies)
    {
        this.strategies.addAll(Arrays.asList(strategies));
    }

    @Override
    public MetricName getStatementName(final StatementContext statementContext)
    {
        if (strategies != null) {
            for (StatementNameStrategy strategy : strategies)
            {
                MetricName statementName = strategy.getStatementName(statementContext);
                if (statementName != null) {
                    return statementName;
                }
            }
        }

        return NameStrategies.UNKNOWN_SQL;
    }
}