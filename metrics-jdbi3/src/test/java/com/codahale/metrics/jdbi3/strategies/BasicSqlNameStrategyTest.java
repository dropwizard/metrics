package com.codahale.metrics.jdbi3.strategies;

import org.jdbi.v3.core.extension.ExtensionMethod;
import org.junit.Test;

import static com.codahale.metrics.MetricRegistry.name;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class BasicSqlNameStrategyTest extends AbstractStrategyTest {

    private BasicSqlNameStrategy basicSqlNameStrategy = new BasicSqlNameStrategy();

    @Test
    public void producesMethodNameAsMetric() throws Exception {
        when(ctx.getExtensionMethod()).thenReturn(new ExtensionMethod(getClass(), getClass().getMethod("producesMethodNameAsMetric")));
        String name = basicSqlNameStrategy.getStatementName(ctx);
        assertThat(name).isEqualTo(name(getClass(), "producesMethodNameAsMetric"));
    }

}
