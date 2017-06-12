package com.codahale.metrics;

import static java.lang.Math.random;
import static java.lang.Math.sin;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

/**
 * @author bstorozhuk
 */
public class SlidingTimeWindowArrayReservoirTestLoadTest {

    public static long start = System.nanoTime();

    public static final ThreadFactory WRITERS_FACTORY = new ThreadFactory() {
        private AtomicInteger count = new AtomicInteger(0);

        @Override public Thread newThread(Runnable target) {
            Thread thread = new Thread(target, "Writer-" + count.getAndIncrement());
            thread.setDaemon(true);
            return thread;
        }
    };

    public static final ThreadFactory READERS_FACTORY = new ThreadFactory() {
        private AtomicInteger count = new AtomicInteger(0);

        @Override public Thread newThread(Runnable target) {
            Thread thread = new Thread(target, "Reader-" + count.getAndIncrement());
            thread.setDaemon(true);
            return thread;
        }
    };
    public static final long MULTIPLIER = 1000000L;

    private static void printState(Snapshot snapshot) {
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("_size", snapshot.size());
        map.put("_min", snapshot.getMin());
        map.put("_max", snapshot.getMax());
        map.put("_mean", snapshot.getMean());
        map.put("_median", snapshot.getMedian());
        map.put("_75thPercentile", snapshot.get75thPercentile());
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            System.out.println(entry.getKey() + "=" + entry.getValue());
        }
        System.out.println("===============================\n\n");
    }

    @Test
    public void loadTest() throws Exception {
        int writersCount = 3;
        final int readersCount = 1;
        final SlidingTimeWindowReservoir reservoir = new SlidingTimeWindowReservoir(60, SECONDS);
        ExecutorService writersPool = Executors.newFixedThreadPool(writersCount, WRITERS_FACTORY);
        ExecutorService readersPool = Executors.newFixedThreadPool(readersCount, READERS_FACTORY);

        for (int i = 0; i < writersCount; i++) {
            writersPool.execute(new Runnable() {
                @Override public void run() {
                    while (true) {
                        long time = System.nanoTime();
                        LockSupport.parkNanos(getWritersSleep());
                        long wavePoint = (long) (MULTIPLIER * sin(time));
                        reservoir.update(wavePoint);

                    }
                }
            });
        }

        for (int i = 0; i < readersCount; i++) {
            readersPool.execute(new Runnable() {
                @Override public void run() {
                    while (true) {
                        long writersSleep = getWritersSleep();
                        System.out.println(writersSleep);

                        LockSupport.parkNanos(SECONDS.toNanos(1));
                        Snapshot snapshot = reservoir.getSnapshot();
                        printState(snapshot);
                        if (snapshot.getMax() > MULTIPLIER) {
                            throw new RuntimeException("max");
                        }
                        if (snapshot.getMin() < -MULTIPLIER) {
                            throw new RuntimeException("min");
                        }
                        if (snapshot.getMean() <= -MULTIPLIER) { // TODO: continue this check
                            throw new RuntimeException("max");
                        }
                    }
                }
            });
        }

        writersPool.awaitTermination(1, TimeUnit.HOURS);
    }

    private long getWritersSleep() {
        double amplitude = 15000;
        double freqWaveAmp = 0.025;
        double freqWaveFreq = 0.01;
        double startRaise = 1.001;

        long time = System.nanoTime() - start;
        double frequency = freqWaveAmp * (1.1 + sin(freqWaveFreq * NANOSECONDS.toSeconds(time)));
        double angularFrequency = 2 * Math.PI * frequency;
        double result = amplitude * (startRaise + sin(angularFrequency * NANOSECONDS.toSeconds(time)));

        if (random() < 0.00001) {
            System.out.println(frequency);
        }

        return (long) result;
    }
}