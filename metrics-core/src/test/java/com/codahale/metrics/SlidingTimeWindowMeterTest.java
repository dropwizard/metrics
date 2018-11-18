package com.codahale.metrics;

import static com.codahale.metrics.SlidingTimeWindowMeter.NUMBER_OF_BUCKETS;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

public class SlidingTimeWindowMeterTest {

    class MockingClock extends Clock {

        long nextTick = 0L;

        public void setNextTick(long nextTick) {
            this.nextTick = nextTick;
        }

        @Override
        public long getTick() {
            return nextTick;
        }
    }

    private final MockingClock mockingClock = new MockingClock();

    private SlidingTimeWindowMeter movingAverages;
    private Meter meter;

    @Before
    public void init() {
        movingAverages = new SlidingTimeWindowMeter(mockingClock);
        meter = new Meter(movingAverages, mockingClock);
    }

    @Test
    public void normalizeIndex() {

        SlidingTimeWindowMeter stwm = new SlidingTimeWindowMeter();

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

        SlidingTimeWindowMeter stwm = new SlidingTimeWindowMeter(new MockingClock());

        assertThat(stwm.calculateIndexOfTick(Instant.ofEpochSecond(0L)), is(0));
        assertThat(stwm.calculateIndexOfTick(Instant.ofEpochSecond(1L)), is(1));
    }

    @Test
    public void mark_max_without_cleanup() {

        int markCount = NUMBER_OF_BUCKETS;

        for (int i = 0; i < markCount; i++) {
            mockingClock.setNextTick(TimeUnit.SECONDS.toNanos(i));
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

        for (int i = 0; i < markCount; i++) {
            mockingClock.setNextTick(TimeUnit.SECONDS.toNanos(i));
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

        for (int i = 0; i < 10; i++) {
            mockingClock.setNextTick(TimeUnit.SECONDS.toNanos(i));
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
            mockingClock.setNextTick(TimeUnit.SECONDS.toNanos(i));
            meter.mark();
        }

        // only 60/300/900 of the 1000 events took place in the last 1/5/15 minute(s)
        assertThat(meter.getOneMinuteRate(), is(60.0));
        assertThat(meter.getFiveMinuteRate(), is(300.0));
        assertThat(meter.getFifteenMinuteRate(), is(900.0));
    }
}