package io.dropwizard.metrics5.benchmarks;

import io.dropwizard.metrics5.ExponentiallyDecayingReservoir;
import io.dropwizard.metrics5.SlidingTimeWindowArrayReservoir;
import io.dropwizard.metrics5.SlidingTimeWindowReservoir;
import io.dropwizard.metrics5.SlidingWindowReservoir;
import io.dropwizard.metrics5.UniformReservoir;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
public class ReservoirBenchmark {

    private final UniformReservoir uniform = new UniformReservoir();
    private final ExponentiallyDecayingReservoir exponential = new ExponentiallyDecayingReservoir();
    private final SlidingWindowReservoir sliding = new SlidingWindowReservoir(1000);
    private final SlidingTimeWindowReservoir slidingTime = new SlidingTimeWindowReservoir(200, TimeUnit.MILLISECONDS);
    private final SlidingTimeWindowArrayReservoir arrTime = new SlidingTimeWindowArrayReservoir(200, TimeUnit.MILLISECONDS);

    // It's intentionally not declared as final to avoid constant folding
    private long nextValue = 0xFBFBABBA;

    @Benchmark
    public Object perfUniformReservoir() {
        uniform.update(nextValue);
        return uniform;
    }

    @Benchmark
    public Object perfSlidingTimeWindowArrayReservoir() {
        arrTime.update(nextValue);
        return arrTime;
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
            .warmupIterations(10)
            .measurementIterations(10)
            .addProfiler(GCProfiler.class)
            .measurementTime(TimeValue.seconds(3))
            .timeUnit(TimeUnit.MICROSECONDS)
            .mode(Mode.AverageTime)
            .threads(4)
            .forks(1)
            .build();

        new Runner(opt).run();
    }

}
