package io.dropwizard.metrics5;

import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

public class MetricRegistryListenerTest {
    private static final MetricName BLAH = MetricName.build("blah");

    private final Counter counter = mock(Counter.class);
    private final Histogram histogram = mock(Histogram.class);
    private final Meter meter = mock(Meter.class);
    private final Timer timer = mock(Timer.class);
    private final MetricRegistryListener listener = new MetricRegistryListener.Base() {

    };

    @Test
    public void noOpsOnGaugeAdded() {
        listener.onGaugeAdded(BLAH, () -> {
            throw new RuntimeException("Should not be called");
        });
    }

    @Test
    public void noOpsOnCounterAdded() {
        listener.onCounterAdded(BLAH, counter);

        verifyNoInteractions(counter);
    }

    @Test
    public void noOpsOnHistogramAdded() {
        listener.onHistogramAdded(BLAH, histogram);

        verifyNoInteractions(histogram);
    }

    @Test
    public void noOpsOnMeterAdded() {
        listener.onMeterAdded(BLAH, meter);

        verifyNoInteractions(meter);
    }

    @Test
    public void noOpsOnTimerAdded() {
        listener.onTimerAdded(BLAH, timer);

        verifyNoInteractions(timer);
    }

    @Test
    public void doesNotExplodeWhenMetricsAreRemoved() {
        listener.onGaugeRemoved(BLAH);
        listener.onCounterRemoved(BLAH);
        listener.onHistogramRemoved(BLAH);
        listener.onMeterRemoved(BLAH);
        listener.onTimerRemoved(BLAH);
    }
}
