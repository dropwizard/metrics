package com.codahale.metrics;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@Warmup(iterations = 2, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 4, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, warmups = 0)
public class BenchmarkNewEDR {

    private Reservoir reservoir;

    @Setup(Level.Trial)
    public void setup() {
        reservoir = new LockFreeExponentiallyDecayingReservoir();
    }


    @Benchmark
    @BenchmarkMode({Mode.AverageTime, Mode.Throughput, Mode.SampleTime})
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    @Threads(1)
    public void measureThreads_01() {
        reservoir.update(123);
    }

    @Benchmark
    @BenchmarkMode({Mode.AverageTime, Mode.Throughput, Mode.SampleTime})
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    @Threads(2)
    public void measureThreads_02() {
        reservoir.update(123);
    }

    @Benchmark
    @BenchmarkMode({Mode.AverageTime, Mode.Throughput, Mode.SampleTime})
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    @Threads(4)
    public void measureThreads_04() {
        reservoir.update(123);
    }

    @Benchmark
    @BenchmarkMode({Mode.AverageTime, Mode.Throughput, Mode.SampleTime})
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    @Threads(32)
    public void measureThreads_32() {
        reservoir.update(123);
    }

    @Benchmark
    @BenchmarkMode({Mode.AverageTime, Mode.Throughput, Mode.SampleTime})
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    @Threads(32)
    public void measureThreads_64() {
        reservoir.update(123);
    }

}
