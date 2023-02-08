package io.dropwizard.metrics5.jdbi3.strategies;

import io.dropwizard.metrics5.MetricName;
import org.jdbi.v3.core.extension.ExtensionMethod;
import org.junit.jupiter.api.Test;

import static io.dropwizard.metrics5.MetricRegistry.name;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class BasicSqlNameStrategyTest extends AbstractStrategyTest {

    private final BasicSqlNameStrategy basicSqlNameStrategy = new BasicSqlNameStrategy();

    @Test
    void producesMethodNameAsMetric() throws Exception {
        when(ctx.getExtensionMethod()).thenReturn(new ExtensionMethod(getClass(), getClass().getMethod("someMethod")));
        MetricName name = basicSqlNameStrategy.getStatementName(ctx);
        assertThat(name).isEqualTo(name(getClass(), "someMethod"));
    }

    public void someMethod() {
    }
}
