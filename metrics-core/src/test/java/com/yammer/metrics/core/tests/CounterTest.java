package com.yammer.metrics.core.tests;

import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricsProcessor;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class CounterTest {
    final Counter counter = new Counter();

    @Test
    public void startsAtZero() throws Exception {
        assertThat("the counter's initial value is zero",
                   counter.count(),
                   is(0L));
    }

    @Test
    public void incrementsByOne() throws Exception {
        counter.inc();

        assertThat("the counter's value after being incremented is one",
                   counter.count(),
                   is(1L));
    }

    @Test
    public void incrementsByAnArbitraryDelta() throws Exception {
        counter.inc(12);

        assertThat("the counter's value after being incremented by 12 is 12",
                   counter.count(),
                   is(12L));
    }

    @Test
    public void decrementsByOne() throws Exception {
        counter.dec();

        assertThat("the counter's value after being decremented is negative one",
                   counter.count(),
                   is(-1L));
    }

    @Test
    public void decrementsByAnArbitraryDelta() throws Exception {
        counter.dec(12);

        assertThat("the counter's value after being decremented by 12 is -12",
                   counter.count(),
                   is(-12L));
    }

    @Test
    public void isZeroAfterBeingCleared() throws Exception {
        counter.inc(3);
        counter.clear();

        assertThat("the counter's value after being cleared is zero",
                   counter.count(),
                   is(0L));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void isProcessedAsACounter() throws Exception {
        final MetricName name = new MetricName(CounterTest.class, "counter");
        final Object context = new Object();
        final MetricsProcessor<Object> processor = mock(MetricsProcessor.class);

        counter.processWith(processor, name, context);

        verify(processor).processCounter(name, counter, context);
    }
}
