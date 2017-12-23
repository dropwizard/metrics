package com.codahale.metrics;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SlidingWindowReservoirTest {
    private final SlidingWindowReservoir reservoir = new SlidingWindowReservoir(3);

    @Test
    public void handlesSmallDataStreams() {
        reservoir.update(1);
        reservoir.update(2);

        assertThat(reservoir.getSnapshot().getValues())
                .containsOnly(1, 2);
    }

    @Test
    public void onlyKeepsTheMostRecentFromBigDataStreams() {
        reservoir.update(1);
        reservoir.update(2);
        reservoir.update(3);
        reservoir.update(4);

        assertThat(reservoir.getSnapshot().getValues())
                .containsOnly(2, 3, 4);
    }
}
