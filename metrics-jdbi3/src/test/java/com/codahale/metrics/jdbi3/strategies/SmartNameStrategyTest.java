package com.codahale.metrics.jdbi3.strategies;

import com.codahale.metrics.jdbi3.InstrumentedTimingCollector;
import org.jdbi.v3.core.extension.ExtensionMethod;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static com.codahale.metrics.MetricRegistry.name;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

public class SmartNameStrategyTest extends AbstractStrategyTest {

    private StatementNameStrategy smartNameStrategy = new SmartNameStrategy();
    private InstrumentedTimingCollector collector;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        collector = new InstrumentedTimingCollector(registry, smartNameStrategy);
    }

    @Test
    public void updatesTimerForSqlObjects() throws Exception {
        when(ctx.getExtensionMethod()).thenReturn(
                new ExtensionMethod(getClass(), getClass().getMethod("updatesTimerForSqlObjects")));

        collector.collect(TimeUnit.SECONDS.toNanos(1), ctx);

        String name = smartNameStrategy.getStatementName(ctx);
        assertThat(name).isEqualTo(name(getClass(), "updatesTimerForSqlObjects"));
        assertThat(getTimerMaxValue(name)).isEqualTo(1000000000);
    }

    @Test
    public void updatesTimerForRawSql() throws Exception {
        collector.collect(TimeUnit.SECONDS.toNanos(2), ctx);

        String name = smartNameStrategy.getStatementName(ctx);
        assertThat(name).isEqualTo(name("sql", "raw"));
        assertThat(getTimerMaxValue(name)).isEqualTo(2000000000);
    }

    @Test
    public void updatesTimerForNoRawSql() throws Exception {
        reset(ctx);

        collector.collect(TimeUnit.SECONDS.toNanos(2), ctx);

        String name = smartNameStrategy.getStatementName(ctx);
        assertThat(name).isEqualTo(name("sql", "empty"));
        assertThat(getTimerMaxValue(name)).isEqualTo(2000000000);
    }

    @Test
    public void updatesTimerForContextClass() throws Exception {
        when(ctx.getExtensionMethod()).thenReturn(new ExtensionMethod(getClass(),
                getClass().getMethod("updatesTimerForContextClass")));
        collector.collect(TimeUnit.SECONDS.toNanos(3), ctx);

        String name = smartNameStrategy.getStatementName(ctx);
        assertThat(name).isEqualTo(name(getClass(), "updatesTimerForContextClass"));
        assertThat(getTimerMaxValue(name)).isEqualTo(3000000000L);
    }
}
