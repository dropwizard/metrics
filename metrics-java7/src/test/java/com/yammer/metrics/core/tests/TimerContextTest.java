package com.yammer.metrics.core.tests;

import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.core.TimerContext;
import com.yammer.metrics.core.TimerMetric;
import org.junit.After;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.closeTo;
import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.is;

public class TimerContextTest {
    private final MetricsRegistry registry = new MetricsRegistry();
    private final TimerMetric timer = registry.newTimer(TimerContextTest.class,
                                                        "example",
                                                        TimeUnit.MILLISECONDS,
                                                        TimeUnit.SECONDS);

    @After
    public void tearDown() throws Exception {
        registry.threadPools().shutdownThreadPools();
    }

    @Test
    public void timesAnExecutionContext() throws Exception {
        try (TimerContext context = TimerContext.time(timer)) {
            Thread.sleep(100);
        }

        assertThat("the timer is updated",
                   timer.count(),
                   is(1L));

        assertThat("the context is timed",
                   timer.min(),
                   is(closeTo(100, 1)));
    }
}
