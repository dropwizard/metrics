package com.codahale.metrics.benchmarks;

import com.codahale.metrics.ExponentiallyDecayingReservoir;
import com.codahale.metrics.SlidingTimeWindowReservoir;
import com.codahale.metrics.SlidingWindowReservoir;
import com.codahale.metrics.UniformReservoir;
import com.google.caliper.Benchmark;
import com.google.caliper.runner.CaliperMain;

import java.util.concurrent.TimeUnit;

public class ReservoirBenchmark extends Benchmark {
    public static void main(String[] args) throws Exception {
        CaliperMain.main(ReservoirBenchmark.class, args);
    }

    private final UniformReservoir uniform = new UniformReservoir();
    private final ExponentiallyDecayingReservoir exponential = new ExponentiallyDecayingReservoir();
    private final SlidingWindowReservoir sliding = new SlidingWindowReservoir(1000);
    private final SlidingTimeWindowReservoir slidingTime = new SlidingTimeWindowReservoir(1, TimeUnit.SECONDS);

    @SuppressWarnings("UnusedDeclaration")
    public void timeUniformReservoir(int reps) {
        for (int i = 0; i < reps; i++) {
            uniform.update(i);
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public void timeExponentiallyDecayingReservoir(int reps) {
        for (int i = 0; i < reps; i++) {
            exponential.update(i);
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public void timeSlidingWindowReservoir(int reps) {
        for (int i = 0; i < reps; i++) {
            sliding.update(i);
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public void timeSlidingTimeWindowReservoir(int reps) {
        for (int i = 0; i < reps; i++) {
            slidingTime.update(i);
        }
    }
}
