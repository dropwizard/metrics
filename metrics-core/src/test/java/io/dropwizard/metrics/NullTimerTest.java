package io.dropwizard.metrics;

import org.junit.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

public class NullTimerTest {

    @Test
    public void hasRates() throws Exception {
        final Timer timer = new NullTimer();

        assertThat(timer.getCount())
                .isEqualTo(1);

        assertThat(timer.getMeanRate())
                .isEqualTo(0.0, offset(0.001));

        assertThat(timer.getOneMinuteRate())
                .isEqualTo(0.0, offset(0.001));

        assertThat(timer.getFiveMinuteRate())
                .isEqualTo(0.0, offset(0.001));

        assertThat(timer.getFifteenMinuteRate())
                .isEqualTo(0.0, offset(0.001));
    }

    @Test
    public void updateDoesNothing() throws Exception {
        final Timer timer = new NullTimer(5.5);

        assertThat(timer.getCount())
                .isEqualTo(1);

        timer.update(1, TimeUnit.SECONDS);

        assertThat(timer.getCount())
                .isEqualTo(1);

        assertThat(timer.getFifteenMinuteRate())
                .isEqualTo(5.5);

        assertThat(timer.getFiveMinuteRate())
                .isEqualTo(5.5);

        timer.update(11L, TimeUnit.SECONDS);

        assertThat(timer.getOneMinuteRate())
                .isEqualTo(5.5);

        assertThat(timer.getMeanRate())
                .isEqualTo(5.5);
    }

    @Test
    public void timesCallableInstances() throws Exception {
        final Timer timer = new NullTimer(10);

        final String value = timer.time(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return "one";
            }
        });

        assertThat(timer.getCount())
                .isEqualTo(1);

        assertThat(value)
                .isEqualTo("one");
    }

}
