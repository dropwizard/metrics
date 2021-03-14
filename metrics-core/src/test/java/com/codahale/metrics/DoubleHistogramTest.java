package com.codahale.metrics;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DoubleHistogramTest {
    private final DoubleReservoir reservoir = mock(DoubleReservoir.class);
    private final DoubleHistogram histogram = new DoubleHistogram(reservoir);

    @Test
    public void updatesTheCountOnUpdates() {
        assertThat(histogram.getCount())
                .isZero();

        histogram.update(1);

        assertThat(histogram.getCount())
                .isEqualTo(1);
    }

    @Test
    public void returnsTheSnapshotFromTheDoubleReservoir() {
        final DoubleSnapshot snapshot = mock(DoubleSnapshot.class);
        when(reservoir.getSnapshot()).thenReturn(snapshot);

        assertThat(histogram.getSnapshot())
                .isEqualTo(snapshot);
    }

    @Test
    public void updatesTheDoubleReservoir() throws Exception {
        histogram.update(1);

        verify(reservoir).update(1);
    }
}
