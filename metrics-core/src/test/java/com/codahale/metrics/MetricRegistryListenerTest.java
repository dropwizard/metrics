package com.codahale.metrics;

import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;

public class MetricRegistryListenerTest {
    private final Counter counter = mock(Counter.class);
    private final Histogram histogram = mock(Histogram.class);
    private final Meter meter = mock(Meter.class);
    private final Timer timer = mock(Timer.class);
    private final MetricRegistryListener listener = new MetricRegistryListener.Base() {

    };

    @Test
    public void noOpsOnGaugeAdded() throws Exception {
        listener.onGaugeAdded("blah", () -> {
            throw new RuntimeException("Should not be called");
        });
    }

    @Test
    public void noOpsOnCounterAdded() throws Exception {
        listener.onCounterAdded("blah", counter);

        verifyZeroInteractions(counter);
    }

    @Test
    public void noOpsOnHistogramAdded() throws Exception {
        listener.onHistogramAdded("blah", histogram);

        verifyZeroInteractions(histogram);
    }

    @Test
    public void noOpsOnMeterAdded() throws Exception {
        listener.onMeterAdded("blah", meter);

        verifyZeroInteractions(meter);
    }

    @Test
    public void noOpsOnTimerAdded() throws Exception {
        listener.onTimerAdded("blah", timer);

        verifyZeroInteractions(timer);
    }

    @Test
    public void doesNotExplodeWhenMetricsAreRemoved() throws Exception {
        listener.onGaugeRemoved("blah");
        listener.onCounterRemoved("blah");
        listener.onHistogramRemoved("blah");
        listener.onMeterRemoved("blah");
        listener.onTimerRemoved("blah");
    }
}
