package com.yammer.metrics.jdbi.strategies;


/**
 * Adds statistics for JDBI queries that set the
 * {@link NameStrategies#STATEMENT_CLASS} and
 * {@link NameStrategies#STATEMENT_NAME} for class based display or
 * {@link NameStrategies#STATEMENT_GROUP} and
 * {@link NameStrategies#STATEMENT_NAME} for group based display.
 *
 * Also knows how to deal with SQL Object statements.
 */
public class SmartNameStrategy extends DelegatingStatementNameStrategy
{
    public SmartNameStrategy()
    {
        super(NameStrategies.CHECK_EMPTY,
              NameStrategies.CONTEXT_CLASS,
              NameStrategies.CONTEXT_NAME,
              NameStrategies.SQL_OBJECT,
              NameStrategies.CHECK_RAW,
              NameStrategies.NAIVE_NAME);
    }
}
