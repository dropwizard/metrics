package com.codahale.metrics;

import org.openjdk.jcstress.annotations.Actor;
import org.openjdk.jcstress.annotations.Expect;
import org.openjdk.jcstress.annotations.JCStressTest;
import org.openjdk.jcstress.annotations.Outcome;
import org.openjdk.jcstress.annotations.State;
import org.openjdk.jcstress.infra.results.L_Result;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@JCStressTest
@Outcome(
    id = "\\[240, 241, 242, 243, 244, 245, 246, 247, 248, 249\\]",
    expect = Expect.ACCEPTABLE,
    desc = "Actor1 made read before Actor2 even started"
    )
@Outcome(
    id = "\\[243, 244, 245, 246, 247, 248, 249\\]",
    expect = Expect.ACCEPTABLE,
    desc = "Actor2 made trim before Actor1 even started"
    )
@Outcome(
    id = "\\[244, 245, 246, 247, 248, 249\\]",
    expect = Expect.ACCEPTABLE,
    desc = "Actor1 made trim, then Actor2 started trim and made startIndex change, " +
        "before Actor1 concurrent read."
    )
@Outcome(
    id = "\\[243, 244, 245, 246, 247, 248\\]",
    expect = Expect.ACCEPTABLE,
    desc = "Actor1 made trim, then Actor2 started trim, but not finished startIndex change, before Actor1 concurrent read."
    )
@State
public class SlidingTimeWindowArrayReservoirTrimReadTest {
    private final AtomicLong ticks = new AtomicLong(0);
    private final SlidingTimeWindowArrayReservoir reservoir;

    public SlidingTimeWindowArrayReservoirTrimReadTest() {
        reservoir = new SlidingTimeWindowArrayReservoir(10, TimeUnit.NANOSECONDS, new Clock() {
            @Override
            public long getTick() {
                return ticks.get();
            }
        });

        for (int i = 0; i < 250; i++) {
            ticks.set(i);
            reservoir.update(i);
        }
    }

    @Actor
    public void actor1(L_Result r) {
        Snapshot snapshot = reservoir.getSnapshot();
        String stringValues = Arrays.toString(snapshot.getValues());
        r.r1 = stringValues;
    }

    @Actor
    public void actor2() {
        ticks.set(253);
        reservoir.trim();
    }
}