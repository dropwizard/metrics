package com.codahale.metrics.benchmarks;

import com.codahale.metrics.Counter;
import com.google.caliper.Benchmark;
import com.google.caliper.runner.CaliperMain;

public class CounterBenchmark extends Benchmark {
    public static void main(String[] args) throws Exception {
        CaliperMain.main(CounterBenchmark.class, args);
    }

    private final Counter counter = new Counter();

    @SuppressWarnings("unused")
    public void timeIncrement(int reps) {
        for (int i = 0; i < reps; i++) {
            counter.inc(i);
        }
    }
}
