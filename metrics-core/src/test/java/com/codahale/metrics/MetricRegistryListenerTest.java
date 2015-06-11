package com.codahale.metrics;

import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;

public class MetricRegistryListenerTest {
    private static final MetricName BLAH = MetricName.build("blah");
    
    private final Gauge gauge = mock(Gauge.class);
    private final Counter counter = mock(Counter.class);
    private final Histogram histogram = mock(Histogram.class);
    private final Meter meter = mock(Meter.class);
    private final Timer timer = mock(Timer.class);
    private final MetricRegistryListener listener = new MetricRegistryListener.Base() {

    };

    @Test
    public void noOpsOnGaugeAdded() throws Exception {
        listener.onGaugeAdded(BLAH, gauge);

        verifyZeroInteractions(gauge);
    }

    @Test
    public void noOpsOnCounterAdded() throws Exception {
        listener.onCounterAdded(BLAH, counter);

        verifyZeroInteractions(counter);
    }

    @Test
    public void noOpsOnHistogramAdded() throws Exception {
        listener.onHistogramAdded(BLAH, histogram);

        verifyZeroInteractions(histogram);
    }

    @Test
    public void noOpsOnMeterAdded() throws Exception {
        listener.onMeterAdded(BLAH, meter);

        verifyZeroInteractions(meter);
    }

    @Test
    public void noOpsOnTimerAdded() throws Exception {
        listener.onTimerAdded(BLAH, timer);

        verifyZeroInteractions(timer);
    }

    @Test
    public void doesNotExplodeWhenMetricsAreRemoved() throws Exception {
        listener.onGaugeRemoved(BLAH);
        listener.onCounterRemoved(BLAH);
        listener.onHistogramRemoved(BLAH);
        listener.onMeterRemoved(BLAH);
        listener.onTimerRemoved(BLAH);
    }
}
