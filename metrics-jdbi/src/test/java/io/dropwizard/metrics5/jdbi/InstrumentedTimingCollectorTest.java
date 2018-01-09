package io.dropwizard.metrics5.jdbi;

import io.dropwizard.metrics5.MetricName;
import io.dropwizard.metrics5.MetricRegistry;
import io.dropwizard.metrics5.Timer;
import io.dropwizard.metrics5.jdbi.strategies.NameStrategies;
import io.dropwizard.metrics5.jdbi.strategies.ShortNameStrategy;
import io.dropwizard.metrics5.jdbi.strategies.SmartNameStrategy;
import io.dropwizard.metrics5.jdbi.strategies.StatementNameStrategy;
import org.junit.Test;
import org.skife.jdbi.v2.StatementContext;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class InstrumentedTimingCollectorTest {
    private final MetricRegistry registry = new MetricRegistry();

    @Test
    public void updatesTimerForSqlObjects() throws Exception {
        final StatementNameStrategy strategy = new SmartNameStrategy();
        final InstrumentedTimingCollector collector = new InstrumentedTimingCollector(registry,
                                                                                      strategy);
        final StatementContext ctx = mock(StatementContext.class);
        doReturn("SELECT 1").when(ctx).getRawSql();
        doReturn(getClass()).when(ctx).getSqlObjectType();
        doReturn(getClass().getMethod("updatesTimerForSqlObjects")).when(ctx).getSqlObjectMethod();

        collector.collect(TimeUnit.SECONDS.toNanos(1), ctx);

        final MetricName name = strategy.getStatementName(ctx);
        final Timer timer = registry.timer(name);

        assertThat(name)
                .isEqualTo(MetricRegistry.name(getClass(), "updatesTimerForSqlObjects"));
        assertThat(timer.getSnapshot().getMax())
                .isEqualTo(1000000000);
    }

    @Test
    public void updatesTimerForSqlObjectsWithoutMethod() throws Exception {
        final StatementNameStrategy strategy = new SmartNameStrategy();
        final InstrumentedTimingCollector collector = new InstrumentedTimingCollector(registry,
                                                                                      strategy);
        final StatementContext ctx = mock(StatementContext.class);
        doReturn("SELECT 1").when(ctx).getRawSql();
        doReturn(getClass()).when(ctx).getSqlObjectType();

        collector.collect(TimeUnit.SECONDS.toNanos(1), ctx);

        final MetricName name = strategy.getStatementName(ctx);
        final Timer timer = registry.timer(name);

        assertThat(name)
                .isEqualTo(MetricRegistry.name(getClass(), "SELECT 1"));
        assertThat(timer.getSnapshot().getMax())
                .isEqualTo(1000000000);
    }

    @Test
    public void updatesTimerForRawSql() throws Exception {
        final StatementNameStrategy strategy = new SmartNameStrategy();
        final InstrumentedTimingCollector collector = new InstrumentedTimingCollector(registry,
                                                                                      strategy);
        final StatementContext ctx = mock(StatementContext.class);
        doReturn("SELECT 1").when(ctx).getRawSql();

        collector.collect(TimeUnit.SECONDS.toNanos(2), ctx);

        final MetricName name = strategy.getStatementName(ctx);
        final Timer timer = registry.timer(name);

        assertThat(name)
                .isEqualTo(MetricRegistry.name("sql", "raw", "SELECT 1"));
        assertThat(timer.getSnapshot().getMax())
                .isEqualTo(2000000000);
    }

    @Test
    public void updatesTimerForNoRawSql() throws Exception {
        final StatementNameStrategy strategy = new SmartNameStrategy();
        final InstrumentedTimingCollector collector = new InstrumentedTimingCollector(registry,
                                                                                      strategy);
        final StatementContext ctx = mock(StatementContext.class);

        collector.collect(TimeUnit.SECONDS.toNanos(2), ctx);

        final MetricName name = strategy.getStatementName(ctx);
        final Timer timer = registry.timer(name);

        assertThat(name)
                .isEqualTo(MetricRegistry.name("sql", "empty"));
        assertThat(timer.getSnapshot().getMax())
                .isEqualTo(2000000000);
    }

    @Test
    public void updatesTimerForNonSqlishRawSql() throws Exception {
        final StatementNameStrategy strategy = new SmartNameStrategy();
        final InstrumentedTimingCollector collector = new InstrumentedTimingCollector(registry,
                                                                                      strategy);
        final StatementContext ctx = mock(StatementContext.class);
        doReturn("don't know what it is but it's not SQL").when(ctx).getRawSql();

        collector.collect(TimeUnit.SECONDS.toNanos(3), ctx);

        final MetricName name = strategy.getStatementName(ctx);
        final Timer timer = registry.timer(name);

        assertThat(name)
                .isEqualTo(MetricRegistry.name("sql", "raw", "don't know what it is but it's not SQL"));
        assertThat(timer.getSnapshot().getMax())
                .isEqualTo(3000000000L);
    }

    @Test
    public void updatesTimerForContextClass() throws Exception {
        final StatementNameStrategy strategy = new SmartNameStrategy();
        final InstrumentedTimingCollector collector = new InstrumentedTimingCollector(registry,
                                                                                      strategy);
        final StatementContext ctx = mock(StatementContext.class);
        doReturn("SELECT 1").when(ctx).getRawSql();
        doReturn(getClass().getName()).when(ctx).getAttribute(NameStrategies.STATEMENT_CLASS);
        doReturn("updatesTimerForContextClass").when(ctx)
                .getAttribute(NameStrategies.STATEMENT_NAME);

        collector.collect(TimeUnit.SECONDS.toNanos(3), ctx);

        final MetricName name = strategy.getStatementName(ctx);
        final Timer timer = registry.timer(name);

        assertThat(name)
                .isEqualTo(MetricRegistry.name(getClass(), "updatesTimerForContextClass"));
        assertThat(timer.getSnapshot().getMax())
                .isEqualTo(3000000000L);
    }

    @Test
    public void updatesTimerForTemplateFile() throws Exception {
        final StatementNameStrategy strategy = new SmartNameStrategy();
        final InstrumentedTimingCollector collector = new InstrumentedTimingCollector(registry,
                                                                                      strategy);
        final StatementContext ctx = mock(StatementContext.class);
        doReturn("SELECT 1").when(ctx).getRawSql();
        doReturn("foo/bar.stg").when(ctx).getAttribute(NameStrategies.STATEMENT_GROUP);
        doReturn("updatesTimerForTemplateFile").when(ctx)
                .getAttribute(NameStrategies.STATEMENT_NAME);

        collector.collect(TimeUnit.SECONDS.toNanos(4), ctx);

        final MetricName name = strategy.getStatementName(ctx);
        final Timer timer = registry.timer(name);

        assertThat(name)
                .isEqualTo(MetricRegistry.name("foo", "bar", "updatesTimerForTemplateFile"));
        assertThat(timer.getSnapshot().getMax())
                .isEqualTo(4000000000L);
    }

    @Test
    public void updatesTimerForContextGroupAndName() throws Exception {
        final StatementNameStrategy strategy = new SmartNameStrategy();
        final InstrumentedTimingCollector collector = new InstrumentedTimingCollector(registry,
                                                                                      strategy);
        final StatementContext ctx = mock(StatementContext.class);
        doReturn("SELECT 1").when(ctx).getRawSql();
        doReturn("my-group").when(ctx).getAttribute(NameStrategies.STATEMENT_GROUP);
        doReturn("updatesTimerForContextGroupAndName").when(ctx)
                .getAttribute(NameStrategies.STATEMENT_NAME);

        collector.collect(TimeUnit.SECONDS.toNanos(4), ctx);

        final MetricName name = strategy.getStatementName(ctx);
        final Timer timer = registry.timer(name);

        assertThat(name)
                .isEqualTo(MetricRegistry.name("my-group", "updatesTimerForContextGroupAndName", ""));
        assertThat(timer.getSnapshot().getMax())
                .isEqualTo(4000000000L);
    }

    @Test
    public void updatesTimerForContextGroupTypeAndName() throws Exception {
        final StatementNameStrategy strategy = new SmartNameStrategy();
        final InstrumentedTimingCollector collector = new InstrumentedTimingCollector(registry,
                                                                                      strategy);
        final StatementContext ctx = mock(StatementContext.class);
        doReturn("SELECT 1").when(ctx).getRawSql();
        doReturn("my-group").when(ctx).getAttribute(NameStrategies.STATEMENT_GROUP);
        doReturn("my-type").when(ctx).getAttribute(NameStrategies.STATEMENT_TYPE);
        doReturn("updatesTimerForContextGroupTypeAndName").when(ctx)
                .getAttribute(NameStrategies.STATEMENT_NAME);

        collector.collect(TimeUnit.SECONDS.toNanos(5), ctx);

        final MetricName name = strategy.getStatementName(ctx);
        final Timer timer = registry.timer(name);

        assertThat(name)
                .isEqualTo(MetricRegistry.name("my-group", "my-type", "updatesTimerForContextGroupTypeAndName"));
        assertThat(timer.getSnapshot().getMax())
                .isEqualTo(5000000000L);
    }

    @Test
    public void updatesTimerForShortSqlObjectStrategy() throws Exception {
        final StatementNameStrategy strategy = new ShortNameStrategy("jdbi");
        final InstrumentedTimingCollector collector = new InstrumentedTimingCollector(registry,
                                                                                      strategy);
        final StatementContext ctx = mock(StatementContext.class);
        doReturn("SELECT 1").when(ctx).getRawSql();
        doReturn(getClass()).when(ctx).getSqlObjectType();
        doReturn(getClass().getMethod("updatesTimerForShortSqlObjectStrategy")).when(ctx)
                .getSqlObjectMethod();

        collector.collect(TimeUnit.SECONDS.toNanos(1), ctx);

        final MetricName name = strategy.getStatementName(ctx);
        final Timer timer = registry.timer(name);

        assertThat(name)
                .isEqualTo(MetricRegistry.name("jdbi",
                                getClass().getSimpleName(),
                                "updatesTimerForShortSqlObjectStrategy"));
        assertThat(timer.getSnapshot().getMax())
                .isEqualTo(1000000000);
    }

    @Test
    public void updatesTimerForShortContextClassStrategy() throws Exception {
        final StatementNameStrategy strategy = new ShortNameStrategy("jdbi");
        final InstrumentedTimingCollector collector = new InstrumentedTimingCollector(registry,
                                                                                      strategy);
        final StatementContext ctx = mock(StatementContext.class);
        doReturn("SELECT 1").when(ctx).getRawSql();
        doReturn(getClass().getName()).when(ctx).getAttribute(NameStrategies.STATEMENT_CLASS);
        doReturn("updatesTimerForShortContextClassStrategy").when(ctx)
                .getAttribute(NameStrategies.STATEMENT_NAME);

        collector.collect(TimeUnit.SECONDS.toNanos(3), ctx);

        final MetricName name = strategy.getStatementName(ctx);
        final Timer timer = registry.timer(name);

        assertThat(name)
                .isEqualTo(MetricRegistry.name("jdbi",
                                getClass().getSimpleName(),
                                "updatesTimerForShortContextClassStrategy"));
        assertThat(timer.getSnapshot().getMax())
                .isEqualTo(3000000000L);
    }
}
