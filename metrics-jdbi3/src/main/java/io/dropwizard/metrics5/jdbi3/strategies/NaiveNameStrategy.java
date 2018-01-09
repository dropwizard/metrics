package io.dropwizard.metrics5.jdbi3.strategies;

/**
 * Very simple strategy, can be used with any JDBI loader to build basic statistics.
 */
public class NaiveNameStrategy extends DelegatingStatementNameStrategy {

    public NaiveNameStrategy() {
        super(DefaultNameStrategy.CHECK_EMPTY,
              DefaultNameStrategy.NAIVE_NAME);
    }
}
