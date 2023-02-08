package io.dropwizard.metrics5.jdbi3.strategies;

import io.dropwizard.metrics5.MetricName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NaiveNameStrategyTest extends AbstractStrategyTest {

    private final NaiveNameStrategy naiveNameStrategy = new NaiveNameStrategy();

    @Test
    void producesSqlRawMetrics() throws Exception {
        MetricName name = naiveNameStrategy.getStatementName(ctx);
        assertThat(name.getKey()).isEqualToIgnoringCase("SELECT 1");
    }

}
