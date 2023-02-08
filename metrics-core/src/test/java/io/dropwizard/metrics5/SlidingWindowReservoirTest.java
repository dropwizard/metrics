package io.dropwizard.metrics5;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SlidingWindowReservoirTest {
    private final SlidingWindowReservoir reservoir = new SlidingWindowReservoir(3);

    @Test
    void handlesSmallDataStreams() {
        reservoir.update(1);
        reservoir.update(2);

        assertThat(reservoir.getSnapshot().getValues())
                .containsOnly(1, 2);
    }

    @Test
    void onlyKeepsTheMostRecentFromBigDataStreams() {
        reservoir.update(1);
        reservoir.update(2);
        reservoir.update(3);
        reservoir.update(4);

        assertThat(reservoir.getSnapshot().getValues())
                .containsOnly(2, 3, 4);
    }
}
