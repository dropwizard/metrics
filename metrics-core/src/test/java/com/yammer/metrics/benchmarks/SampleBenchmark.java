package com.yammer.metrics.benchmarks;

import com.google.caliper.Runner;
import com.google.caliper.SimpleBenchmark;
import com.yammer.metrics.ExponentiallyDecayingSample;
import com.yammer.metrics.SlidingTimeWindowSample;
import com.yammer.metrics.SlidingWindowSample;
import com.yammer.metrics.UniformSample;

import java.util.concurrent.TimeUnit;

public class SampleBenchmark extends SimpleBenchmark {
    public static void main(String[] args) throws Exception {
        new Runner().run(SampleBenchmark.class.getName());
    }

    private final UniformSample uniform = new UniformSample();
    private final ExponentiallyDecayingSample exponential = new ExponentiallyDecayingSample();
    private final SlidingWindowSample sliding = new SlidingWindowSample(1000);
    private final SlidingTimeWindowSample slidingTime = new SlidingTimeWindowSample(1, TimeUnit.SECONDS);

    @SuppressWarnings("UnusedDeclaration")
    public void timeUniformSample(int reps) {
        for (int i = 0; i < reps; i++) {
            uniform.update(i);
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public void timeExponentiallyDecayingSample(int reps) {
        for (int i = 0; i < reps; i++) {
            exponential.update(i);
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public void timeSlidingWindowSample(int reps) {
        for (int i = 0; i < reps; i++) {
            sliding.update(i);
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public void timeSlidingTimeWindowSample(int reps) {
        for (int i = 0; i < reps; i++) {
            slidingTime.update(i);
        }
    }
}
