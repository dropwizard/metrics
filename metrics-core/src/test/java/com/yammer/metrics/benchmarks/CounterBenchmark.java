package com.yammer.metrics.benchmarks;

import com.google.caliper.Runner;
import com.google.caliper.SimpleBenchmark;
import com.yammer.metrics.Counter;

public class CounterBenchmark extends SimpleBenchmark {
    public static void main(String[] args) throws Exception {
        new Runner().run(CounterBenchmark.class.getName());
    }

    private final Counter counter = new Counter();

    @SuppressWarnings("unused")
    public void timeIncrement(int reps) {
        for (int i = 0; i < reps; i++) {
            counter.inc(i);
        }
    }
}
