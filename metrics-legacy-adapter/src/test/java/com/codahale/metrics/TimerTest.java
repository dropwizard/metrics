package com.codahale.metrics;

import org.junit.Test;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("deprecation")
public class TimerTest {

    private static class ManualClock extends Clock {

        long startTime = System.nanoTime();

        @Override
        public long getTick() {
            return startTime += 100_000_000;
        }
    }

    private static void verifyOneEvent(Timer timer) {
        assertThat(timer.getCount()).isEqualTo(1);
        assertThat(timer.getSnapshot().getValues()).containsExactly(100_000_000L);
    }

    @Test
    public void testCreate() {
        Timer timer = new Timer();
        timer.update(100, TimeUnit.MILLISECONDS);
        timer.update(200, TimeUnit.MILLISECONDS);

        assertThat(timer.getCount()).isEqualTo(2);
    }

    @Test
    public void testCreateWithCustomReservoir() {
        Timer timer = new Timer(new SlidingWindowReservoir(100));
        timer.update(100, TimeUnit.MILLISECONDS);
        timer.update(200, TimeUnit.MILLISECONDS);

        assertThat(timer.getCount()).isEqualTo(2);
    }

    @Test
    public void testCreateWithCustomReservoirAndClock() {
        Timer timer = new Timer(new SlidingWindowReservoir(100), new Clock.UserTimeClock());
        timer.update(100, TimeUnit.MILLISECONDS);
        timer.update(200, TimeUnit.MILLISECONDS);

        assertThat(timer.getCount()).isEqualTo(2);
    }

    @Test
    public void testTimerContext() {
        Timer timer = new Timer(new SlidingWindowReservoir(100), new ManualClock());
        timer.time().stop();

        verifyOneEvent(timer);
    }

    @Test
    public void testTimerRunnable() {
        Timer timer = new Timer(new SlidingWindowReservoir(100), new ManualClock());

        AtomicInteger counter = new AtomicInteger();
        timer.time((Runnable) counter::incrementAndGet);

        assertThat(counter.get()).isEqualTo(1);
        verifyOneEvent(timer);
    }

    @Test
    public void testTimerCallable() throws Exception {
        Timer timer = new Timer(new SlidingWindowReservoir(100), new ManualClock());

        String message = timer.time(() -> "SUCCESS");

        assertThat(message).isEqualTo("SUCCESS");
        verifyOneEvent(timer);
    }

    @Test
    public void testTimerSupplier() throws Exception {
        Timer timer = new Timer(new SlidingWindowReservoir(100), new ManualClock());

        Integer result = timer.timeSupplier(() -> 42);

        assertThat(result).isEqualTo(42);
        verifyOneEvent(timer);
    }

    @Test
    public void testUpdateDuration() {
        Timer timer = new Timer();
        timer.update(Duration.ofMillis(100));
        timer.update(Duration.ofMillis(200));

        assertThat(timer.getCount()).isEqualTo(2);
    }
}
