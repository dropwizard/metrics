package io.dropwizard.metrics5.jdbi3.strategies;

import io.dropwizard.metrics5.MetricName;
import io.dropwizard.metrics5.MetricRegistry;
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

    long getTimerMaxValue(MetricName name) {
        return registry.timer(name).getSnapshot().getMax();
    }
}
