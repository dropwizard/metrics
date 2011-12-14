package com.yammer.metrics.jdbi.strategies;

/**
 * Very simple strategy, can be used with any JDBI loader to build basic statistics.
 */
public class NaiveNameStrategy extends DelegatingStatementNameStrategy
{
    public NaiveNameStrategy()
    {
        super(NameStrategies.CHECK_EMPTY,
              NameStrategies.CHECK_RAW,
              NameStrategies.NAIVE_NAME);
    }
}
