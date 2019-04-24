package com.codahale.metrics;

import org.openjdk.jcstress.annotations.Actor;
import org.openjdk.jcstress.annotations.Expect;
import org.openjdk.jcstress.annotations.JCStressTest;
import org.openjdk.jcstress.annotations.Outcome;
import org.openjdk.jcstress.annotations.State;
import org.openjdk.jcstress.infra.results.L_Result;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@JCStressTest
@Outcome(id = "\\[\\]", expect = Expect.ACCEPTABLE)
@Outcome(id = "\\[31\\]", expect = Expect.ACCEPTABLE)
@Outcome(id = "\\[15\\]", expect = Expect.ACCEPTABLE)
@Outcome(id = "\\[31, 15\\]", expect = Expect.ACCEPTABLE)
@Outcome(id = "\\[15, 31\\]", expect = Expect.ACCEPTABLE)
@State
public class SlidingTimeWindowArrayReservoirWriteReadTest {

    private final SlidingTimeWindowArrayReservoir reservoir;

    public SlidingTimeWindowArrayReservoirWriteReadTest() {
        reservoir = new SlidingTimeWindowArrayReservoir(1, TimeUnit.SECONDS);
    }

    @Actor
    public void actor1() {
        reservoir.update(31L);
    }

    @Actor
    public void actor2() {
        reservoir.update(15L);
    }

    @Actor
    public void actor3(L_Result r) {
        Snapshot snapshot = reservoir.getSnapshot();
        String stringValues = Arrays.toString(snapshot.getValues());
        r.r1 = stringValues;
    }

}