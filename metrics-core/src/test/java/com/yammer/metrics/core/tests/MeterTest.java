package com.yammer.metrics.core.tests;

import com.yammer.metrics.core.Meter;
import com.yammer.metrics.core.MetricsRegistry;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class MeterTest {
    private final MetricsRegistry registry = new MetricsRegistry();
    private final Meter meter = registry.newMeter(MeterTest.class, "things", "thing", TimeUnit.SECONDS);

    @Test
    public void aBlankMeter() throws Exception {
        assertThat("the meter has a count of zero",
                   meter.getCount(),
                   is(0L));

        assertThat("the meter has a mean rate of zero",
                   meter.getMeanRate(),
                   is(closeTo(0.0, 0.001)));
    }

    @Test
    public void aMeterWithThreeEvents() throws Exception {
        meter.mark(3);

        assertThat("the meter has a count of three",
                   meter.getCount(),
                   is(3L));
    }
}
