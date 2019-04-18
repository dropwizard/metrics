package com.codahale.metrics;

import org.openjdk.jcstress.annotations.Actor;
import org.openjdk.jcstress.annotations.Arbiter;
import org.openjdk.jcstress.annotations.Expect;
import org.openjdk.jcstress.annotations.JCStressTest;
import org.openjdk.jcstress.annotations.Outcome;
import org.openjdk.jcstress.annotations.State;
import org.openjdk.jcstress.infra.results.L_Result;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@JCStressTest
@Outcome(id = "\\[1023, 1029, 1034\\]", expect = Expect.ACCEPTABLE)
@State
public class SlidingTimeWindowArrayReservoirWriteReadAllocate {

    private final SlidingTimeWindowArrayReservoir reservoir;

    public SlidingTimeWindowArrayReservoirWriteReadAllocate() {
        reservoir = new SlidingTimeWindowArrayReservoir(500, TimeUnit.SECONDS);
        for (int i = 0; i < 1024; i++) {
            reservoir.update(i);
        }
    }

    @Actor
    public void actor1() {
        reservoir.update(1029L);
    }

    @Actor
    public void actor2() {
        reservoir.update(1034L);
    }

    @Arbiter
    public void arbiter(L_Result r) {
        Snapshot snapshot = reservoir.getSnapshot();
        long[] values = snapshot.getValues();
        String stringValues = Arrays.toString(Arrays.copyOfRange(values, values.length - 3, values.length));
        r.r1 = stringValues;
    }
}