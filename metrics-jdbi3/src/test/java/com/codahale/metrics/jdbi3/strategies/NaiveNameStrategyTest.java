package com.codahale.metrics.jdbi3.strategies;

import com.codahale.metrics.MetricName;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class NaiveNameStrategyTest extends AbstractStrategyTest {

    private NaiveNameStrategy naiveNameStrategy = new NaiveNameStrategy();

    @Test
    public void producesSqlRawMetrics() throws Exception {
        MetricName name = naiveNameStrategy.getStatementName(ctx);
        assertThat(name.getKey()).isEqualToIgnoringCase("SELECT 1");
    }

}
