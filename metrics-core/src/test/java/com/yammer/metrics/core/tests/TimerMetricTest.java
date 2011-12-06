package com.yammer.metrics.core.tests;

import com.yammer.metrics.core.TimerContext;
import com.yammer.metrics.core.TimerMetric;
import org.junit.After;
import org.junit.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class TimerMetricTest {
    final ScheduledExecutorService pool = Executors.newSingleThreadScheduledExecutor();
    final TimerMetric timer = new TimerMetric(pool, TimeUnit.MILLISECONDS, TimeUnit.SECONDS);

    @After
    public void tearDown() throws Exception {
        pool.shutdownNow();
    }

    @Test
    public void hasADurationUnit() throws Exception {
        assertThat("the timer has a duration unit",
                   timer.durationUnit(),
                   is(TimeUnit.MILLISECONDS));
    }

    @Test
    public void hasARateUnit() throws Exception {
        assertThat("the timer has a rate unit",
                   timer.rateUnit(),
                   is(TimeUnit.SECONDS));
    }

    @Test
    public void aBlankTimer() throws Exception {
        assertThat("the timer has a count of zero",
                   timer.count(),
                   is(0L));

        assertThat("the timer has a max duration of zero",
                   timer.max(),
                   is(closeTo(0.0, 0.001)));

        assertThat("the timer has a min duration of zero",
                   timer.min(),
                   is(closeTo(0.0, 0.001)));

        assertThat("the timer has a mean duration of zero",
                   timer.mean(),
                   is(closeTo(0.0, 0.001)));

        assertThat("the timer has a duration standard deviation of zero",
                   timer.stdDev(),
                   is(closeTo(0.0, 0.001)));

        final Double[] percentiles = timer.percentiles(0.5, 0.75, 0.99);

        assertThat("the timer has a median duration of zero",
                   percentiles[0],
                   is(closeTo(0.0, 0.001)));

        assertThat("the timer has a 75th percentile duration of zero",
                   percentiles[1],
                   is(closeTo(0.0, 0.001)));

        assertThat("the timer has a 99th percentile duration of zero",
                   percentiles[2],
                   is(closeTo(0.0, 0.001)));

        assertThat("the timer has a mean rate of zero",
                   timer.meanRate(),
                   is(closeTo(0.0, 0.001)));

        assertThat("the timer has a one-minute rate of zero",
                   timer.oneMinuteRate(),
                   is(closeTo(0.0, 0.001)));

        assertThat("the timer has a five-minute rate of zero",
                   timer.fiveMinuteRate(),
                   is(closeTo(0.0, 0.001)));

        assertThat("the timer has a fifteen-minute rate of zero",
                   timer.fifteenMinuteRate(),
                   is(closeTo(0.0, 0.001)));

        assertThat("the timer has no values",
                   timer.values().isEmpty(),
                   is(true));
    }

    @Test
    public void timingASeriesOfEvents() throws Exception {
        timer.update(10, TimeUnit.MILLISECONDS);
        timer.update(20, TimeUnit.MILLISECONDS);
        timer.update(20, TimeUnit.MILLISECONDS);
        timer.update(30, TimeUnit.MILLISECONDS);
        timer.update(40, TimeUnit.MILLISECONDS);

        assertThat("the timer has a count of 5",
                   timer.count(),
                   is(5L));

        assertThat("the timer has a max duration of 40",
                   timer.max(),
                   is(closeTo(40.0, 0.001)));

        assertThat("the timer has a min duration of 10",
                   timer.min(),
                   is(closeTo(10.0, 0.001)));

        assertThat("the timer has a mean duration of 24",
                   timer.mean(),
                   is(closeTo(24.0, 0.001)));

        assertThat("the timer has a duration standard deviation of zero",
                   timer.stdDev(),
                   is(closeTo(11.401, 0.001)));

        final Double[] percentiles = timer.percentiles(0.5, 0.75, 0.99);

        assertThat("the timer has a median duration of 20",
                   percentiles[0],
                   is(closeTo(20.0, 0.001)));

        assertThat("the timer has a 75th percentile duration of 35",
                   percentiles[1],
                   is(closeTo(35.0, 0.001)));

        assertThat("the timer has a 99th percentile duration of 40",
                   percentiles[2],
                   is(closeTo(40.0, 0.001)));

        assertThat("the timer has no values",
                   timer.values(),
                   hasItems(10.0, 20.0, 20.0, 30.0, 40.0));
    }

    @Test
    public void timingVariantValues() throws Exception {
        timer.update(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        timer.update(0, TimeUnit.NANOSECONDS);

        assertThat("the timer has an accurate standard deviation",
                   timer.stdDev(),
                   is(closeTo(6.521908912666392E12, 0.001)));
    }

    @Test
    public void timingCallableInstances() throws Exception {
        final String value = timer.time(new Callable<String>() {
            @Override
            public String call() throws Exception {
                Thread.sleep(50);
                return "one";
            }
        });

        assertThat("returns the result of the callable",
                   value,
                   is("one"));

        assertThat("records the duration of the Callable#call()",
                   timer.max(),
                   is(closeTo(50, 5)));
    }

    @Test
    public void timingContexts() throws Exception {
        final TimerContext context = timer.time();
        Thread.sleep(50);
        context.stop();

        assertThat("records the duration of the context",
                   timer.max(),
                   is(closeTo(50, 5)));
    }
}
