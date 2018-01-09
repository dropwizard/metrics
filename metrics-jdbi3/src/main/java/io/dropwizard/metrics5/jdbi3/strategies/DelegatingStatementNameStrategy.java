package io.dropwizard.metrics5.jdbi3.strategies;

import io.dropwizard.metrics5.MetricName;
import org.jdbi.v3.core.statement.StatementContext;

import java.util.Arrays;
import java.util.List;

public abstract class DelegatingStatementNameStrategy implements StatementNameStrategy {

    /**
     * Unknown SQL.
     */
    private static final MetricName UNKNOWN_SQL = MetricName.build("sql.unknown");

    private final List<StatementNameStrategy> strategies;

    protected DelegatingStatementNameStrategy(StatementNameStrategy... strategies) {
        this.strategies = Arrays.asList(strategies);
    }

    @Override
    public MetricName getStatementName(StatementContext statementContext) {
        for (StatementNameStrategy strategy : strategies) {
            final MetricName statementName = strategy.getStatementName(statementContext);
            if (statementName != null) {
                return statementName;
            }
        }

        return UNKNOWN_SQL;
    }
}
