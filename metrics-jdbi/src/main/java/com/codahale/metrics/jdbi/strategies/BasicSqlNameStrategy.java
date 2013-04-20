package com.codahale.metrics.jdbi.strategies;

public class BasicSqlNameStrategy extends DelegatingStatementNameStrategy {
    public BasicSqlNameStrategy() {
        super(NameStrategies.CHECK_EMPTY,
              NameStrategies.SQL_OBJECT);
    }
}
