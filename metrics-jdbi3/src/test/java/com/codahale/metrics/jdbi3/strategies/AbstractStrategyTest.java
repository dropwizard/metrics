package com.codahale.metrics.jdbi3.strategies;

import com.codahale.metrics.MetricRegistry;
import org.jdbi.v3.core.statement.StatementContext;
import org.junit.Before;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AbstractStrategyTest {

    MetricRegistry registry = new MetricRegistry();
    StatementContext ctx = mock(StatementContext.class);

    @Before
    public void setUp() throws Exception {
        when(ctx.getRawSql()).thenReturn("SELECT 1");
    }

    long getTimerMaxValue(String name) {
        return registry.timer(name).getSnapshot().getMax();
    }
}
