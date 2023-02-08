package io.dropwizard.metrics5.jdbi3.strategies;

import io.dropwizard.metrics5.MetricName;
import io.dropwizard.metrics5.jdbi3.InstrumentedTimingCollector;
import org.jdbi.v3.core.extension.ExtensionMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static io.dropwizard.metrics5.MetricRegistry.name;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

class SmartNameStrategyTest extends AbstractStrategyTest {

    private final StatementNameStrategy smartNameStrategy = new SmartNameStrategy();
    private InstrumentedTimingCollector collector;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        collector = new InstrumentedTimingCollector(registry, smartNameStrategy);
    }

    @Test
    void updatesTimerForSqlObjects() throws Exception {
        when(ctx.getExtensionMethod()).thenReturn(
                new ExtensionMethod(getClass(), getClass().getMethod("someMethod")));

        collector.collect(TimeUnit.SECONDS.toNanos(1), ctx);

        MetricName name = smartNameStrategy.getStatementName(ctx);
        assertThat(name).isEqualTo(name(getClass(), "someMethod"));
        assertThat(getTimerMaxValue(name)).isEqualTo(1000000000);
    }

    @Test
    void updatesTimerForRawSql() throws Exception {
        collector.collect(TimeUnit.SECONDS.toNanos(2), ctx);

        MetricName name = smartNameStrategy.getStatementName(ctx);
        assertThat(name).isEqualTo(name("sql", "raw"));
        assertThat(getTimerMaxValue(name)).isEqualTo(2000000000);
    }

    @Test
    void updatesTimerForNoRawSql() throws Exception {
        reset(ctx);

        collector.collect(TimeUnit.SECONDS.toNanos(2), ctx);

        MetricName name = smartNameStrategy.getStatementName(ctx);
        assertThat(name).isEqualTo(name("sql", "empty"));
        assertThat(getTimerMaxValue(name)).isEqualTo(2000000000);
    }

    @Test
    void updatesTimerForContextClass() throws Exception {
        when(ctx.getExtensionMethod()).thenReturn(new ExtensionMethod(getClass(),
                getClass().getMethod("someMethod")));
        collector.collect(TimeUnit.SECONDS.toNanos(3), ctx);

        MetricName name = smartNameStrategy.getStatementName(ctx);
        assertThat(name).isEqualTo(name(getClass(), "someMethod"));
        assertThat(getTimerMaxValue(name)).isEqualTo(3000000000L);
    }

    public void someMethod() {
    }
}
