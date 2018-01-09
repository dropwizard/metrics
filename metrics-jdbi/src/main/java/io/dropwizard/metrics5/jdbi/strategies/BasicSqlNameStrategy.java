package io.dropwizard.metrics5.jdbi.strategies;

public class BasicSqlNameStrategy extends DelegatingStatementNameStrategy {
    public BasicSqlNameStrategy() {
        super(NameStrategies.CHECK_EMPTY,
                NameStrategies.SQL_OBJECT);
    }
}
