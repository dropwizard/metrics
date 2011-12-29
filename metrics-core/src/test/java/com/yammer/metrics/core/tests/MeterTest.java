package com.yammer.metrics.core.tests;

import com.yammer.metrics.core.Meter;
import org.junit.After;
import org.junit.Test;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class MeterTest {
    final ScheduledExecutorService pool = Executors.newSingleThreadScheduledExecutor();
    final Meter meter = Meter.newMeter(pool, "thangs", TimeUnit.SECONDS);

    @After
    public void tearDown() throws Exception {
        pool.shutdownNow();
    }

    @Test
    public void aBlankMeter() throws Exception {
        assertThat("the meter has a count of zero",
                   meter.count(),
                   is(0L));

        assertThat("the meter has a mean rate of zero",
                   meter.meanRate(),
                   is(closeTo(0.0, 0.001)));
    }

    @Test
    public void aMeterWithThreeEvents() throws Exception {
        meter.mark(3);

        assertThat("the meter has a count of three",
                   meter.count(),
                   is(3L));
    }
}
