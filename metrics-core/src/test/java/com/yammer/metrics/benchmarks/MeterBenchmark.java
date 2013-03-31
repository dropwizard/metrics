package com.yammer.metrics.benchmarks;

import com.google.caliper.Runner;
import com.google.caliper.SimpleBenchmark;
import com.yammer.metrics.Meter;

public class MeterBenchmark extends SimpleBenchmark {
    public static void main(String[] args) throws Exception {
        new Runner().run(MeterBenchmark.class.getName());
    }

    private final Meter meter = new Meter();

    @SuppressWarnings("unused")
    public void timeMark(int reps) {
        for (int i = 0; i < reps; i++) {
            meter.mark(i);
        }
    }
}
