package com.codahale.metrics;

import static com.codahale.metrics.SlidingTimeWindowMovingAverages.NUMBER_OF_BUCKETS;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.Instant;

import org.junit.Before;
import org.junit.Test;

public class SlidingTimeWindowMovingAveragesTest {

    private ManualClock clock;
    private SlidingTimeWindowMovingAverages movingAverages;
    private Meter meter;

    @Before
    public void init() {
        clock = new ManualClock();
        movingAverages = new SlidingTimeWindowMovingAverages(clock);
        meter = new Meter(movingAverages, clock);
    }

    @Test
    public void normalizeIndex() {

        SlidingTimeWindowMovingAverages stwm = new SlidingTimeWindowMovingAverages();

        assertThat(stwm.normalizeIndex(0), is(0));
        assertThat(stwm.normalizeIndex(900), is(0));
        assertThat(stwm.normalizeIndex(9000), is(0));
        assertThat(stwm.normalizeIndex(-900), is(0));

        assertThat(stwm.normalizeIndex(1), is(1));

        assertThat(stwm.normalizeIndex(899), is(899));
        assertThat(stwm.normalizeIndex(-1), is(899));
        assertThat(stwm.normalizeIndex(-901), is(899));
    }

    @Test
    public void calculateIndexOfTick() {

        SlidingTimeWindowMovingAverages stwm = new SlidingTimeWindowMovingAverages(clock);

        assertThat(stwm.calculateIndexOfTick(Instant.ofEpochSecond(0L)), is(0));
        assertThat(stwm.calculateIndexOfTick(Instant.ofEpochSecond(1L)), is(1));
    }

    @Test
    public void mark_max_without_cleanup() {

        int markCount = NUMBER_OF_BUCKETS;

        // compensate the first addSeconds in the loop; first tick should be at zero
        clock.addSeconds(-1);

        for (int i = 0; i < markCount; i++) {
            clock.addSeconds(1);
            meter.mark();
        }

        // verify that no cleanup happened yet
        assertThat(movingAverages.oldestBucketTime, is(Instant.ofEpochSecond(0L)));

        assertThat(meter.getOneMinuteRate(), is(60.0));
        assertThat(meter.getFiveMinuteRate(), is(300.0));
        assertThat(meter.getFifteenMinuteRate(), is(900.0));
    }

    @Test
    public void mark_first_cleanup() {

        int markCount = NUMBER_OF_BUCKETS + 1;

        // compensate the first addSeconds in the loop; first tick should be at zero
        clock.addSeconds(-1);

        for (int i = 0; i < markCount; i++) {
            clock.addSeconds(1);
            meter.mark();
        }

        // verify that at least one cleanup happened
        assertThat(movingAverages.oldestBucketTime, not(is(Instant.ofEpochSecond(0L))));

        assertThat(meter.getOneMinuteRate(), is(60.0));
        assertThat(meter.getFiveMinuteRate(), is(300.0));
        assertThat(meter.getFifteenMinuteRate(), is(900.0));
    }

    @Test
    public void mark_10_values() {

        // compensate the first addSeconds in the loop; first tick should be at zero
        clock.addSeconds(-1);

        for (int i = 0; i < 10; i++) {
            clock.addSeconds(1);
            meter.mark();
        }

        assertThat(meter.getCount(), is(10L));
        assertThat(meter.getOneMinuteRate(), is(10.0));
        assertThat(meter.getFiveMinuteRate(), is(10.0));
        assertThat(meter.getFifteenMinuteRate(), is(10.0));
    }

    @Test
    public void mark_1000_values() {

        for (int i = 0; i < 1000; i++) {
            clock.addSeconds(1);
            meter.mark();
        }

        // only 60/300/900 of the 1000 events took place in the last 1/5/15 minute(s)
        assertThat(meter.getOneMinuteRate(), is(60.0));
        assertThat(meter.getFiveMinuteRate(), is(300.0));
        assertThat(meter.getFifteenMinuteRate(), is(900.0));
    }

    @Test
    public void cleanup_pause_shorter_than_window() {

        meter.mark(10);

        // no mark for three minutes
        clock.addSeconds(180);
        assertThat(meter.getOneMinuteRate(), is(0.0));
        assertThat(meter.getFiveMinuteRate(), is(10.0));
        assertThat(meter.getFifteenMinuteRate(), is(10.0));
    }

    @Test
    public void cleanup_window_wrap_around() {

        // mark at 14:40 minutes of the 15 minute window...
        clock.addSeconds(880);
        meter.mark(10);

        // and query at 15:30 minutes (the bucket index must have wrapped around)
        clock.addSeconds(50);
        assertThat(meter.getOneMinuteRate(), is(10.0));
        assertThat(meter.getFiveMinuteRate(), is(10.0));
        assertThat(meter.getFifteenMinuteRate(), is(10.0));

        // and query at 30:10 minutes (the bucket index must have wrapped around for the second time)
        clock.addSeconds(880);
        assertThat(meter.getOneMinuteRate(), is(0.0));
        assertThat(meter.getFiveMinuteRate(), is(0.0));
        assertThat(meter.getFifteenMinuteRate(), is(0.0));
    }

    @Test
    public void cleanup_pause_longer_than_two_windows() {

        meter.mark(10);

        // after forty minutes all rates should be zero
        clock.addSeconds(2400);
        assertThat(meter.getOneMinuteRate(), is(0.0));
        assertThat(meter.getFiveMinuteRate(), is(0.0));
        assertThat(meter.getFifteenMinuteRate(), is(0.0));
    }
}