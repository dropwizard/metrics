package com.codahale.metrics.benchmarks;

import com.google.caliper.Runner;
import com.google.caliper.SimpleBenchmark;
import com.codahale.metrics.ExponentiallyDecayingReservoir;
import com.codahale.metrics.SlidingTimeWindowReservoir;
import com.codahale.metrics.SlidingWindowReservoir;
import com.codahale.metrics.UniformReservoir;

import java.util.concurrent.TimeUnit;

public class ReservoirBenchmark extends SimpleBenchmark {
    public static void main(String[] args) throws Exception {
        new Runner().run(ReservoirBenchmark.class.getName());
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
