package com.codahale.metrics;

import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

public class MetricRegistryListenerTest {
    private final Counter counter = mock(Counter.class);
    private final Histogram histogram = mock(Histogram.class);
    private final Meter meter = mock(Meter.class);
    private final Timer timer = mock(Timer.class);
    private final MetricRegistryListener listener = new MetricRegistryListener.Base() {

    };

    @Test
    public void noOpsOnGaugeAdded() {
        listener.onGaugeAdded("blah", () -> {
            throw new RuntimeException("Should not be called");
        });
    }

    @Test
    public void noOpsOnCounterAdded() {
        listener.onCounterAdded("blah", counter);

        verifyNoInteractions(counter);
    }

    @Test
    public void noOpsOnHistogramAdded() {
        listener.onHistogramAdded("blah", histogram);

        verifyNoInteractions(histogram);
    }

    @Test
    public void noOpsOnMeterAdded() {
        listener.onMeterAdded("blah", meter);

        verifyNoInteractions(meter);
    }

    @Test
    public void noOpsOnTimerAdded() {
        listener.onTimerAdded("blah", timer);

        verifyNoInteractions(timer);
    }

    @Test
    public void doesNotExplodeWhenMetricsAreRemoved() {
        listener.onGaugeRemoved("blah");
        listener.onCounterRemoved("blah");
        listener.onHistogramRemoved("blah");
        listener.onMeterRemoved("blah");
        listener.onTimerRemoved("blah");
    }
}
