package com.codahale.metrics;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

public class NullMeterTest {
    @Test
    public void startsOutWithZeroRatesAndOneCount() throws Exception {
        Meter meter = new NullMeter();

        assertThat(meter.getCount())
                .isEqualTo(1);

        assertThat(meter.getMeanRate())
                .isEqualTo(0.0, offset(0.001));

        assertThat(meter.getOneMinuteRate())
                .isEqualTo(0.0, offset(0.001));

        assertThat(meter.getFiveMinuteRate())
                .isEqualTo(0.0, offset(0.001));

        assertThat(meter.getFifteenMinuteRate())
                .isEqualTo(0.0, offset(0.001));
    }
    
    @Test
    public void startsOutWithGivenRates() throws Exception {
        Meter meter = new NullMeter(29.377);

        assertThat(meter.getCount())
                .isEqualTo(1);

        assertThat(meter.getMeanRate())
                .isEqualTo(29.377, offset(0.001));

        assertThat(meter.getOneMinuteRate())
                .isEqualTo(29.377, offset(0.001));

        assertThat(meter.getFiveMinuteRate())
                .isEqualTo(29.377, offset(0.001));

        assertThat(meter.getFifteenMinuteRate())
                .isEqualTo(29.377, offset(0.001));
    }

    @Test
    public void marksEventsAndDoesNothingToRatesAndCount() throws Exception {
        Meter meter = new NullMeter(29.377);
        meter.mark();
        meter.mark(2);

        assertThat(meter.getCount())
                .isEqualTo(1);

        assertThat(meter.getMeanRate())
                .isEqualTo(29.377, offset(0.001));

        assertThat(meter.getOneMinuteRate())
                .isEqualTo(29.377, offset(0.001));

        assertThat(meter.getFiveMinuteRate())
                .isEqualTo(29.377, offset(0.001));

        assertThat(meter.getFifteenMinuteRate())
                .isEqualTo(29.377, offset(0.001));
    }

}
