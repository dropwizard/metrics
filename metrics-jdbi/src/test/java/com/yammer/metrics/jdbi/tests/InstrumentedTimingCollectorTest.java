package com.yammer.metrics.jdbi.tests;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.TimerMetric;
import com.yammer.metrics.jdbi.InstrumentedTimingCollector;
import com.yammer.metrics.jdbi.strategies.NameStrategies;

import org.junit.Test;
import org.skife.jdbi.v2.StatementContext;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class InstrumentedTimingCollectorTest {
    @Test
    public void updatesTimerForSqlObjects() throws Exception {
        final InstrumentedTimingCollector collector = new InstrumentedTimingCollector(Metrics.defaultRegistry(),
                                                                                      NameStrategies.SQL_OBJECT);
        final StatementContext ctx = mock(StatementContext.class);
        doReturn(getClass()).when(ctx).getSqlObjectType();
        doReturn(getClass().getMethod("updatesTimerForSqlObjects")).when(ctx).getSqlObjectMethod();

        collector.collect(TimeUnit.SECONDS.toNanos(1), ctx);

        final MetricName name = NameStrategies.SQL_OBJECT.getStatementName(ctx);
        final TimerMetric timer = Metrics.newTimer(name, TimeUnit.MILLISECONDS, TimeUnit.SECONDS);

        assertThat(name,
                   is(new MetricName(getClass(), "updatesTimerForSqlObjects")));
        assertThat(timer.max(),
                   is(closeTo(1000.0, 1)));
    }

    @Test
    public void updatesTimerForRawSql() throws Exception {
        final InstrumentedTimingCollector collector = new InstrumentedTimingCollector(Metrics.defaultRegistry(),
                                                                                      NameStrategies.NAIVE_NAME);
        final StatementContext ctx = mock(StatementContext.class);
        doReturn("SELECT 1").when(ctx).getRawSql();
        collector.collect(TimeUnit.SECONDS.toNanos(2), ctx);

        final MetricName name = NameStrategies.NAIVE_NAME.getStatementName(ctx);
        final TimerMetric timer = Metrics.newTimer(name, TimeUnit.MILLISECONDS, TimeUnit.SECONDS);

        assertThat(name,
                   is(new MetricName("sql", "raw", "SELECT_1")));
        assertThat(timer.max(),
                   is(closeTo(2000.0, 1)));
    }
}

