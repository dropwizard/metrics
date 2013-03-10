package com.yammer.metrics.tests;

import com.yammer.metrics.MetricRegistry;
import com.yammer.metrics.Snapshot;
import com.yammer.metrics.Timer;
import com.yammer.metrics.Clock;
import org.junit.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.offset;

public class TimerTest {
    private final MetricRegistry registry = new MetricRegistry("test", new Clock() {
        // a mock clock that increments its ticker by 50msec per call
        private long val = 0;

        @Override
        public long getTick() {
            return val += 50000000;
        }
    });
    private final Timer timer = registry.timer("timer");

    @Test
    public void aBlankTimer() throws Exception {
        assertThat(timer.getCount())
                .isZero();

        assertThat(timer.getMax())
                .isEqualTo(0);

        assertThat(timer.getMin())
                .isEqualTo(0);

        assertThat(timer.getMean())
                .isEqualTo(0.0, offset(0.001));

        assertThat(timer.getStdDev())
                .isEqualTo(0.0, offset(0.001));

        final Snapshot snapshot = timer.getSnapshot();

        assertThat(snapshot.getMedian())
                .isEqualTo(0.0, offset(0.001));

        assertThat(snapshot.get75thPercentile())
                .isEqualTo(0.0, offset(0.001));

        assertThat(snapshot.get99thPercentile())
                .isEqualTo(0.0, offset(0.001));

        assertThat(timer.getMeanRate())
                .isEqualTo(0.0, offset(0.001));

        assertThat(timer.getOneMinuteRate())
                .isEqualTo(0.0, offset(0.001));

        assertThat(timer.getFiveMinuteRate())
                .isEqualTo(0.0, offset(0.001));

        assertThat(timer.getFifteenMinuteRate())
                .isEqualTo(0.0, offset(0.001));

        assertThat(timer.getSnapshot().size())
                .isZero();
    }

    @Test
    public void timingASeriesOfEvents() throws Exception {
        timer.update(10, TimeUnit.MILLISECONDS);
        timer.update(20, TimeUnit.MILLISECONDS);
        timer.update(20, TimeUnit.MILLISECONDS);
        timer.update(30, TimeUnit.MILLISECONDS);
        timer.update(40, TimeUnit.MILLISECONDS);

        assertThat(timer.getCount())
                .isEqualTo(5);

        assertThat(timer.getMax())
                .isEqualTo(40000000);

        assertThat(timer.getMin())
                .isEqualTo(10000000);

        assertThat(timer.getMean())
                .isEqualTo(24000000, offset(0.001));

        assertThat(timer.getStdDev())
                .isEqualTo(11400000, offset(10000.0));

        final Snapshot snapshot = timer.getSnapshot();

        assertThat(snapshot.getMedian())
                .isEqualTo(20000000, offset(0.001));

        assertThat(snapshot.get75thPercentile())
                .isEqualTo(35000000, offset(0.001));

        assertThat(snapshot.get99thPercentile())
                .isEqualTo(40000000, offset(0.001));

        assertThat(timer.getSnapshot().getValues())
                .containsOnly(10000000, 20000000, 20000000, 30000000, 40000000);
    }

    @Test
    public void timingVariantValues() throws Exception {
        timer.update(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        timer.update(0, TimeUnit.NANOSECONDS);

        assertThat(timer.getStdDev())
                .isEqualTo(6.521908912666392E18, offset(0.001));
    }

    @Test
    public void timingCallableInstances() throws Exception {
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

        assertThat(timer.getMax())
                .isEqualTo(50000000);
    }

    @Test
    public void timingContexts() throws Exception {
        timer.time().stop();

        assertThat(timer.getCount())
                .isEqualTo(1);

        assertThat(timer.getMax())
                .isEqualTo(50000000);
    }
}
