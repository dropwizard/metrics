package io.dropwizard.metrics;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class StatisticTest {
    private final Statistic statistic = new Statistic();

    @Test
    public void startsAtZero() throws Exception {
        assertThat(statistic.getCount())
                .isZero();
    }

    @Test
    public void setPositive() throws Exception {
        statistic.set(12);

        assertThat(statistic.getCount())
                .isEqualTo(12);
    }

    @Test
    public void setNegative() throws Exception {
        statistic.set(-12);

        assertThat(statistic.getCount())
                .isEqualTo(-12);
    }
}