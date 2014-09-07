package com.codahale.metrics.benchmarks;

import com.codahale.metrics.ExponentiallyDecayingReservoir;
import com.codahale.metrics.SlidingTimeWindowReservoir;
import com.codahale.metrics.SlidingWindowReservoir;
import com.codahale.metrics.UniformReservoir;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
public class ReservoirBenchmark {

    private final UniformReservoir uniform = new UniformReservoir();
    private final ExponentiallyDecayingReservoir exponential = new ExponentiallyDecayingReservoir();
    private final SlidingWindowReservoir sliding = new SlidingWindowReservoir(1000);
    private final SlidingTimeWindowReservoir slidingTime = new SlidingTimeWindowReservoir(1, TimeUnit.SECONDS);

    // It's intentionally not declared as final to avoid constant folding
    private long nextValue = 0xFBFBABBA;

    @Benchmark
    public Object perfUniformReservoir() {
        uniform.update(nextValue);
        return uniform;
    }

    @Benchmark
    public Object perfExponentiallyDecayingReservoir() {
        exponential.update(nextValue);
        return exponential;
    }
    
    @Benchmark
    public Object perfSlidingWindowReservoir() {
        sliding.update(nextValue);
        return sliding;
    }
    
    @Benchmark
    public Object perfSlidingTimeWindowReservoir() {
        slidingTime.update(nextValue);
        return slidingTime;
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(".*" + ReservoirBenchmark.class.getSimpleName() + ".*")
                .warmupIterations(3)
                .measurementIterations(5)
                .threads(4)
                .forks(1)
                .build();

        new Runner(opt).run();
    }
    
}
