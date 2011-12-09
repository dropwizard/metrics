package com.yammer.metrics.aop.tests;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.MeterMetric;
import com.yammer.metrics.core.TimerMetric;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static com.yammer.metrics.aop.Instrumentation.instrument;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class InstrumentationTest {
    private final Example instrumented = instrument(new Example());

    @Test
    public void meteredCallsIncrementAMeter() throws Exception {
        assertThat(instrumented.meteredMethod(),
                   is("metered"));

        final MeterMetric meter = Metrics.newMeter(Example.class,
                                                         "meteredMethod",
                                                         "calls",
                                                         TimeUnit.SECONDS);
        assertThat(meter.count(),
                   is(1L));
    }

    @Test
    public void timedCallsUseATimer() throws Exception {
        assertThat(instrumented.timedMethod(),
                   is("timed"));

        final TimerMetric timer = Metrics.newTimer(Example.class, "timedMethod");
        assertThat(timer.count(),
                   is(1L));
        
        assertThat(timer.max(),
                   is(closeTo(50, 5)));
    }

    @Test
    public void exceptionMeteredCountsExceptions() throws Exception {
        assertThat(instrumented.exceptionMethod(false),
                   is("exception"));

        try {
            instrumented.exceptionMethod(true);
            fail("should have thrown an IOException");
        } catch (IOException ignored) {}

        final MeterMetric meter = Metrics.newMeter(Example.class,
                                                   "exceptionMethodExceptions",
                                                   "exceptions",
                                                   TimeUnit.SECONDS);
        
        assertThat(meter.count(),
                   is(1L));
    }
}
