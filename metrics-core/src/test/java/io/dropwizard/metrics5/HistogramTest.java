package io.dropwizard.metrics5;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HistogramTest {
    private final Reservoir reservoir = mock(Reservoir.class);
    private final Histogram histogram = new Histogram(reservoir);

    @Test
    void updatesTheCountAndSumOnUpdates() {
        assertThat(histogram.getCount())
                .isZero();
        assertThat(histogram.getSum())
                .isZero();

        histogram.update(1);
        histogram.update(5);

        assertThat(histogram.getCount())
                .isEqualTo(2);
        assertThat(histogram.getSum())
                .isEqualTo(6);
    }

    @Test
    void returnsTheSnapshotFromTheReservoir() {
        final Snapshot snapshot = mock(Snapshot.class);
        when(reservoir.getSnapshot()).thenReturn(snapshot);

        assertThat(histogram.getSnapshot())
                .isEqualTo(snapshot);
    }

    @Test
    void updatesTheReservoir() throws Exception {
        histogram.update(1);

        verify(reservoir).update(1);
    }
}
