package com.codahale.metrics.benchmarks;

import com.codahale.metrics.Meter;
import com.google.caliper.Benchmark;
import com.google.caliper.runner.CaliperMain;

public class MeterBenchmark extends Benchmark {
    public static void main(String[] args) throws Exception {
        CaliperMain.main(MeterBenchmark.class, args);
    }

    private final Meter meter = new Meter();

    @SuppressWarnings("unused")
    public void timeMark(int reps) {
        for (int i = 0; i < reps; i++) {
            meter.mark(i);
        }
    }
}
