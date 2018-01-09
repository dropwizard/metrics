package io.dropwizard.metrics5.jdbi.strategies;

import io.dropwizard.metrics5.MetricName;
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
    public MetricName getStatementName(StatementContext statementContext) {
        for (StatementNameStrategy strategy : strategies) {
            final MetricName statementName = strategy.getStatementName(statementContext);
            if (statementName != null) {
                return statementName;
            }
        }

        return NameStrategies.UNKNOWN_SQL;
    }
}
