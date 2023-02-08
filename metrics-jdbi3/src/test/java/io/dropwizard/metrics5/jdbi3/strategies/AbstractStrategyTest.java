package io.dropwizard.metrics5.jdbi3.strategies;

import io.dropwizard.metrics5.MetricName;
import io.dropwizard.metrics5.MetricRegistry;
import org.jdbi.v3.core.statement.StatementContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AbstractStrategyTest {

    MetricRegistry registry = new MetricRegistry();
    @Mock
    StatementContext ctx;

    @BeforeEach
    void setUp() throws Exception {
        when(ctx.getRawSql()).thenReturn("SELECT 1");
    }

    long getTimerMaxValue(MetricName name) {
        return registry.timer(name).getSnapshot().getMax();
    }
}
