package com.yammer.metrics.jdbi.tests;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.TimerMetric;
import com.yammer.metrics.jdbi.InstrumentedTimingCollector;
import org.junit.Test;
import org.skife.jdbi.v2.StatementContext;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class InstrumentedTimingCollectorTest {
    private final InstrumentedTimingCollector collector = new InstrumentedTimingCollector(Metrics.defaultRegistry(),
                                                                                          String.class);

    @Test
    public void updatesTimerForSqlObjects() throws Exception {
        final StatementContext ctx = mock(StatementContext.class);
        doReturn(getClass()).when(ctx).getSqlObjectType();
        doReturn(getClass().getMethod("updatesTimerForSqlObjects")).when(ctx).getSqlObjectMethod();

        collector.collect(TimeUnit.SECONDS.toNanos(1), ctx);

        final TimerMetric timer = Metrics.newTimer(getClass(), "updatesTimerForSqlObjects");

        assertThat(timer.max(),
                   is(closeTo(1000.0, 1)));
    }

    @Test
    public void updatesTimerForRawSql() throws Exception {
        final StatementContext ctx = mock(StatementContext.class);
        collector.collect(TimeUnit.SECONDS.toNanos(2), ctx);

        final TimerMetric timer = Metrics.newTimer(String.class, "raw-sql");

        assertThat(timer.max(),
                   is(closeTo(2000.0, 1)));
    }
}

