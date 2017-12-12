package com.codahale.metrics.jdbi3.strategies;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class NaiveNameStrategyTest extends AbstractStrategyTest {

    private NaiveNameStrategy naiveNameStrategy = new NaiveNameStrategy();

    @Test
    public void producesSqlRawMetrics() throws Exception {
        String name = naiveNameStrategy.getStatementName(ctx);
        assertThat(name).isEqualToIgnoringCase("SELECT 1");
    }

}
