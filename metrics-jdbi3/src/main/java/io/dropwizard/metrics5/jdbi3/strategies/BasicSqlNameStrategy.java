package io.dropwizard.metrics5.jdbi3.strategies;

/**
 * Collects metrics by respective SQLObject methods.
 */
public class BasicSqlNameStrategy extends DelegatingStatementNameStrategy {

    public BasicSqlNameStrategy() {
        super(DefaultNameStrategy.CHECK_EMPTY,
                DefaultNameStrategy.SQL_OBJECT);
    }
}
