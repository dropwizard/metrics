package com.yammer.metrics.tests;

import com.yammer.metrics.Meter;
import com.yammer.metrics.MetricRegistry;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.offset;

public class MeterTest {
    private final MetricRegistry registry = new MetricRegistry("test");
    private final Meter meter = registry.meter("things");

    @Test
    public void aBlankMeter() throws Exception {
        assertThat(meter.getCount())
                .isZero();

        assertThat(meter.getMeanRate())
                .isEqualTo(0.0, offset(0.001));
    }

    @Test
    public void aMeterWithThreeEvents() throws Exception {
        meter.mark(3);

        assertThat(meter.getCount())
                .isEqualTo(3);
    }
}
