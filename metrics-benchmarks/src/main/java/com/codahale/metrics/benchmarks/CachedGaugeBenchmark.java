package com.codahale.metrics.benchmarks;

import com.codahale.metrics.CachedGauge;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
public class CachedGaugeBenchmark {

    private static final long LOAD_VALUE_DELAY_MILLIS = 10; // Loading delay for 10 milliseconds
    private static final int DEFAULT_VALUE = 12345; // Placeholder value

    private CachedGauge<Integer> cachedGauge = new CachedGauge<Integer>(100, TimeUnit.MILLISECONDS) {
        @Override
        protected Integer loadValue() {
            try {
                Thread.sleep(LOAD_VALUE_DELAY_MILLIS);
            } catch (InterruptedException e) {
                throw new RuntimeException("Thread was interrupted", e);
            }
            return DEFAULT_VALUE;
        }
    };

    @Benchmark
    public void perfGetValue(Blackhole blackhole) {
        blackhole.consume(cachedGauge.getValue());
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(".*" + CachedGaugeBenchmark.class.getSimpleName() + ".*")
                .warmupIterations(3)
                .measurementIterations(5)
                .threads(4)
                .forks(1)
                .build();

        new Runner(opt).run();
    }
}
