package com.codahale.metrics;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class HistogramTest {
    private final Reservoir reservoir = mock(Reservoir.class);
    private final MeasurementPublisher measurementPublisher = mock(MeasurementPublisher.class);
    private final Histogram histogram = new Histogram("histogram", measurementPublisher, reservoir);

    @Test
    public void updatesTheCountOnUpdates() throws Exception {
        assertThat(histogram.getCount())
                .isZero();

        histogram.update(1);

        assertThat(histogram.getCount())
                .isEqualTo(1);
    }

    @Test
    public void returnsTheSnapshotFromTheReservoir() throws Exception {
        final Snapshot snapshot = mock(Snapshot.class);
        when(reservoir.getSnapshot()).thenReturn(snapshot);

        assertThat(histogram.getSnapshot())
                .isEqualTo(snapshot);
    }

    @Test
    public void updatesTheReservoir() throws Exception {
        histogram.update(1);

        verify(reservoir).update(1);
    }

    @Test
    public void notifiesMeasurementListeners() {
        histogram.update(1);
        verify(measurementPublisher)
                .histogramUpdated("histogram", 1);
        verifyNoMoreInteractions(measurementPublisher);
    }
}
