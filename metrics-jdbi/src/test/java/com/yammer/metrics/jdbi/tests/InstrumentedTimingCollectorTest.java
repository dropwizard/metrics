package com.yammer.metrics.jdbi.tests;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.skife.jdbi.v2.StatementContext;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.jdbi.InstrumentedTimingCollector;
import com.yammer.metrics.jdbi.strategies.NameStrategies;
import com.yammer.metrics.jdbi.strategies.ShortNameStrategy;
import com.yammer.metrics.jdbi.strategies.SmartNameStrategy;
import com.yammer.metrics.jdbi.strategies.StatementNameStrategy;

public class InstrumentedTimingCollectorTest {
    @Test
    public void updatesTimerForSqlObjects() throws Exception {
        final StatementNameStrategy strategy = new SmartNameStrategy();
        final InstrumentedTimingCollector collector = new InstrumentedTimingCollector(Metrics.defaultRegistry(), strategy);
        final StatementContext ctx = mock(StatementContext.class);
        doReturn("SELECT 1").when(ctx).getRawSql();
        doReturn(getClass()).when(ctx).getSqlObjectType();
        doReturn(getClass().getMethod("updatesTimerForSqlObjects")).when(ctx).getSqlObjectMethod();

        collector.collect(TimeUnit.SECONDS.toNanos(1), ctx);

        final MetricName name = strategy.getStatementName(ctx);
        final Timer timer = Metrics.newTimer(name, TimeUnit.MILLISECONDS, TimeUnit.SECONDS);

        assertThat(name,
                   is(new MetricName(getClass(), "updatesTimerForSqlObjects")));
        assertThat(timer.max(),
                   is(closeTo(1000.0, 1)));
    }

    @Test
    public void updatesTimerForSqlObjectsWithoutMethod() throws Exception {
        final StatementNameStrategy strategy = new SmartNameStrategy();
        final InstrumentedTimingCollector collector = new InstrumentedTimingCollector(Metrics.defaultRegistry(), strategy);
        final StatementContext ctx = mock(StatementContext.class);
        doReturn("SELECT 1").when(ctx).getRawSql();
        doReturn(getClass()).when(ctx).getSqlObjectType();

        collector.collect(TimeUnit.SECONDS.toNanos(1), ctx);

        final MetricName name = strategy.getStatementName(ctx);
        final Timer timer = Metrics.newTimer(name, TimeUnit.MILLISECONDS, TimeUnit.SECONDS);

        assertThat(name,
                   is(new MetricName(getClass(), "SELECT_1")));
        assertThat(timer.max(),
                   is(closeTo(1000.0, 1)));
    }

    @Test
    public void updatesTimerForRawSql() throws Exception {
        final StatementNameStrategy strategy = new SmartNameStrategy();
        final InstrumentedTimingCollector collector = new InstrumentedTimingCollector(Metrics.defaultRegistry(), strategy);
        final StatementContext ctx = mock(StatementContext.class);
        doReturn("SELECT 1").when(ctx).getRawSql();

        collector.collect(TimeUnit.SECONDS.toNanos(2), ctx);

        final MetricName name = strategy.getStatementName(ctx);
        final Timer timer = Metrics.newTimer(name, TimeUnit.MILLISECONDS, TimeUnit.SECONDS);

        assertThat(name,
                   is(new MetricName("sql", "raw", "SELECT_1")));
        assertThat(timer.max(),
                   is(closeTo(2000.0, 1)));
    }

    @Test
    public void updatesTimerForNoRawSql() throws Exception {
        final StatementNameStrategy strategy = new SmartNameStrategy();
        final InstrumentedTimingCollector collector = new InstrumentedTimingCollector(Metrics.defaultRegistry(), strategy);
        final StatementContext ctx = mock(StatementContext.class);

        collector.collect(TimeUnit.SECONDS.toNanos(2), ctx);

        final MetricName name = strategy.getStatementName(ctx);
        final Timer timer = Metrics.newTimer(name, TimeUnit.MILLISECONDS, TimeUnit.SECONDS);

        assertThat(name,
                   is(new MetricName("sql", "empty", "")));
        assertThat(timer.max(),
                   is(closeTo(2000.0, 1)));
    }

    @Test
    public void updatesTimerForNonSqlishRawSql() throws Exception {
        final StatementNameStrategy strategy = new SmartNameStrategy();
        final InstrumentedTimingCollector collector = new InstrumentedTimingCollector(Metrics.defaultRegistry(), strategy);
        final StatementContext ctx = mock(StatementContext.class);
        doReturn("don't know what it is but it's not SQL").when(ctx).getRawSql();

        collector.collect(TimeUnit.SECONDS.toNanos(3), ctx);

        final MetricName name = strategy.getStatementName(ctx);
        final Timer timer = Metrics.newTimer(name, TimeUnit.MILLISECONDS, TimeUnit.SECONDS);

        assertThat(name,
                   is(new MetricName("sql", "raw", "don_t_know_what_it_is_but_it_s_not_SQL")));
        assertThat(timer.max(),
                   is(closeTo(3000.0, 1)));
    }

    @Test
    public void updatesTimerForContextClass() throws Exception {
        final StatementNameStrategy strategy = new SmartNameStrategy();
        final InstrumentedTimingCollector collector = new InstrumentedTimingCollector(Metrics.defaultRegistry(), strategy);
        final StatementContext ctx = mock(StatementContext.class);
        doReturn("SELECT 1").when(ctx).getRawSql();
        doReturn(getClass().getName()).when(ctx).getAttribute(NameStrategies.STATEMENT_CLASS);
        doReturn("updatesTimerForContextClass").when(ctx).getAttribute(NameStrategies.STATEMENT_NAME);

        collector.collect(TimeUnit.SECONDS.toNanos(3), ctx);

        final MetricName name = strategy.getStatementName(ctx);
        final Timer timer = Metrics.newTimer(name, TimeUnit.MILLISECONDS, TimeUnit.SECONDS);

        assertThat(name,
                   is(new MetricName(getClass(), "updatesTimerForContextClass")));
        assertThat(timer.max(),
                   is(closeTo(3000.0, 1)));
    }

    @Test
    public void updatesTimerForTemplateFile() throws Exception {
        final StatementNameStrategy strategy = new SmartNameStrategy();
        final InstrumentedTimingCollector collector = new InstrumentedTimingCollector(Metrics.defaultRegistry(), strategy);
        final StatementContext ctx = mock(StatementContext.class);
        doReturn("SELECT 1").when(ctx).getRawSql();
        doReturn("foo/bar.stg").when(ctx).getAttribute(NameStrategies.STATEMENT_GROUP);
        doReturn("updatesTimerForTemplateFile").when(ctx).getAttribute(NameStrategies.STATEMENT_NAME);

        collector.collect(TimeUnit.SECONDS.toNanos(4), ctx);

        final MetricName name = strategy.getStatementName(ctx);
        final Timer timer = Metrics.newTimer(name, TimeUnit.MILLISECONDS, TimeUnit.SECONDS);

        assertThat(name,
                   is(new MetricName("foo", "bar", "updatesTimerForTemplateFile")));
        assertThat(timer.max(),
                   is(closeTo(4000.0, 1)));
    }

    @Test
    public void updatesTimerForContextGroupAndName() throws Exception {
        final StatementNameStrategy strategy = new SmartNameStrategy();
        final InstrumentedTimingCollector collector = new InstrumentedTimingCollector(Metrics.defaultRegistry(), strategy);
        final StatementContext ctx = mock(StatementContext.class);
        doReturn("SELECT 1").when(ctx).getRawSql();
        doReturn("my-group").when(ctx).getAttribute(NameStrategies.STATEMENT_GROUP);
        doReturn("updatesTimerForContextGroupAndName").when(ctx).getAttribute(NameStrategies.STATEMENT_NAME);

        collector.collect(TimeUnit.SECONDS.toNanos(4), ctx);

        final MetricName name = strategy.getStatementName(ctx);
        final Timer timer = Metrics.newTimer(name, TimeUnit.MILLISECONDS, TimeUnit.SECONDS);

        assertThat(name,
                   is(new MetricName("my-group", "updatesTimerForContextGroupAndName", "")));
        assertThat(timer.max(),
                   is(closeTo(4000.0, 1)));
    }

    @Test
    public void updatesTimerForContextGroupTypeAndName() throws Exception {
        final StatementNameStrategy strategy = new SmartNameStrategy();
        final InstrumentedTimingCollector collector = new InstrumentedTimingCollector(Metrics.defaultRegistry(), strategy);
        final StatementContext ctx = mock(StatementContext.class);
        doReturn("SELECT 1").when(ctx).getRawSql();
        doReturn("my-group").when(ctx).getAttribute(NameStrategies.STATEMENT_GROUP);
        doReturn("my-type").when(ctx).getAttribute(NameStrategies.STATEMENT_TYPE);
        doReturn("updatesTimerForContextGroupTypeAndName").when(ctx).getAttribute(NameStrategies.STATEMENT_NAME);

        collector.collect(TimeUnit.SECONDS.toNanos(5), ctx);

        final MetricName name = strategy.getStatementName(ctx);
        final Timer timer = Metrics.newTimer(name, TimeUnit.MILLISECONDS, TimeUnit.SECONDS);

        assertThat(name,
                   is(new MetricName("my-group", "my-type", "updatesTimerForContextGroupTypeAndName")));
        assertThat(timer.max(),
                   is(closeTo(5000.0, 1)));
    }

    @Test
    public void updatesTimerForShortSqlObjectStrategy() throws Exception {
        final StatementNameStrategy strategy = new ShortNameStrategy("jdbi");
        final InstrumentedTimingCollector collector = new InstrumentedTimingCollector(Metrics.defaultRegistry(), strategy);
        final StatementContext ctx = mock(StatementContext.class);
        doReturn("SELECT 1").when(ctx).getRawSql();
        doReturn(getClass()).when(ctx).getSqlObjectType();
        doReturn(getClass().getMethod("updatesTimerForShortSqlObjectStrategy")).when(ctx).getSqlObjectMethod();

        collector.collect(TimeUnit.SECONDS.toNanos(1), ctx);

        final MetricName name = strategy.getStatementName(ctx);
        final Timer timer = Metrics.newTimer(name, TimeUnit.MILLISECONDS, TimeUnit.SECONDS);

        assertThat(name,
                   is(new MetricName("jdbi", getClass().getSimpleName(), "updatesTimerForShortSqlObjectStrategy")));
        assertThat(timer.max(),
                   is(closeTo(1000.0, 1)));
    }

    @Test
    public void updatesTimerForShortContextClassStrategy() throws Exception {
        final StatementNameStrategy strategy = new ShortNameStrategy("jdbi");
        final InstrumentedTimingCollector collector = new InstrumentedTimingCollector(Metrics.defaultRegistry(), strategy);
        final StatementContext ctx = mock(StatementContext.class);
        doReturn("SELECT 1").when(ctx).getRawSql();
        doReturn(getClass().getName()).when(ctx).getAttribute(NameStrategies.STATEMENT_CLASS);
        doReturn("updatesTimerForShortContextClassStrategy").when(ctx).getAttribute(NameStrategies.STATEMENT_NAME);

        collector.collect(TimeUnit.SECONDS.toNanos(3), ctx);

        final MetricName name = strategy.getStatementName(ctx);
        final Timer timer = Metrics.newTimer(name, TimeUnit.MILLISECONDS, TimeUnit.SECONDS);

        assertThat(name,
                   is(new MetricName("jdbi", getClass().getSimpleName(), "updatesTimerForShortContextClassStrategy")));
        assertThat(timer.max(),
                   is(closeTo(3000.0, 1)));
    }
}
