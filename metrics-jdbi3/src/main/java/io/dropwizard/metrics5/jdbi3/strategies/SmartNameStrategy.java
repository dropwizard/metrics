package io.dropwizard.metrics5.jdbi3.strategies;

/**
 * Uses a {@link BasicSqlNameStrategy} and fallbacks to {@link DefaultNameStrategy#CONSTANT_SQL_RAW}
 */
public class SmartNameStrategy extends DelegatingStatementNameStrategy {

    public SmartNameStrategy() {
        super(DefaultNameStrategy.CHECK_EMPTY,
                DefaultNameStrategy.SQL_OBJECT,
                DefaultNameStrategy.CONSTANT_SQL_RAW);
    }
}
