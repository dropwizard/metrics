package io.dropwizard.metrics5.benchmarks;

import io.dropwizard.metrics5.SlidingTimeWindowArrayReservoir;
import io.dropwizard.metrics5.SlidingTimeWindowReservoir;
import io.dropwizard.metrics5.Snapshot;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Group;
import org.openjdk.jmh.annotations.GroupThreads;
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

/**
 * @author bstorozhuk
 */
@State(Scope.Benchmark)
public class SlidingTimeWindowReservoirsBenchmark {
    private final SlidingTimeWindowReservoir slidingTime = new SlidingTimeWindowReservoir(200, TimeUnit.MILLISECONDS);
    private final SlidingTimeWindowArrayReservoir arrTime = new SlidingTimeWindowArrayReservoir(200, TimeUnit.MILLISECONDS);

    // It's intentionally not declared as final to avoid constant folding
    private long nextValue = 0xFBFBABBA;

    @Benchmark
    @Group("slidingTime")
    @GroupThreads(3)
    public Object slidingTimeAddMeasurement() {
        slidingTime.update(nextValue);
        return slidingTime;
    }

    @Benchmark
    @Group("slidingTime")
    @GroupThreads(1)
    public Object slidingTimeRead() {
        Snapshot snapshot = slidingTime.getSnapshot();
        return snapshot;
    }

    @Benchmark
    @Group("arrTime")
    @GroupThreads(3)
    public Object arrTimeAddMeasurement() {
        arrTime.update(nextValue);
        return slidingTime;
    }

    @Benchmark
    @Group("arrTime")
    @GroupThreads(1)
    public Object arrTimeRead() {
        Snapshot snapshot = arrTime.getSnapshot();
        return snapshot;
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
            .include(".*" + SlidingTimeWindowReservoirsBenchmark.class.getSimpleName() + ".*")
            .warmupIterations(10)
            .measurementIterations(10)
            .addProfiler(GCProfiler.class)
            .measurementTime(TimeValue.seconds(3))
            .timeUnit(TimeUnit.MICROSECONDS)
            .mode(Mode.AverageTime)
            .forks(1)
            .build();

        new Runner(opt).run();
    }
}

