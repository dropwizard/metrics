package io.dropwizard.metrics5.jdbi3.strategies;

import io.dropwizard.metrics5.MetricName;
import org.jdbi.v3.core.extension.ExtensionMethod;
import org.junit.Test;

import static io.dropwizard.metrics5.MetricRegistry.name;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class BasicSqlNameStrategyTest extends AbstractStrategyTest {

    private BasicSqlNameStrategy basicSqlNameStrategy = new BasicSqlNameStrategy();

    @Test
    public void producesMethodNameAsMetric() throws Exception {
        when(ctx.getExtensionMethod()).thenReturn(new ExtensionMethod(getClass(), getClass().getMethod("producesMethodNameAsMetric")));
        MetricName name = basicSqlNameStrategy.getStatementName(ctx);
        assertThat(name).isEqualTo(name(getClass(), "producesMethodNameAsMetric"));
    }

}
