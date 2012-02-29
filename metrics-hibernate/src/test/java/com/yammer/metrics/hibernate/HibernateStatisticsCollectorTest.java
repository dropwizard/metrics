package com.yammer.metrics.hibernate;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Gauge;
import org.hibernate.stat.Statistics;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;

public class HibernateStatisticsCollectorTest {

    @Before
    public void setUp() throws Exception {

        Statistics statistics = Mockito.mock(Statistics.class);
        Mockito.when(statistics.getQueryExecutionCount()).thenReturn(125L);

        HibernateStatisticsCollector.register(statistics);

    }

    @Test
    public void measuresQueryExecutionCount() throws Exception {

        final Gauge gauge = Metrics.newGauge(Statistics.class, "query-execution-count", new Gauge<Long>() {
            public Long value() { return -1L; }
        });

        assertEquals(gauge.value(), 125L);

    }

}
