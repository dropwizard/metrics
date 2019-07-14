package com.codahale.metrics;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class MetricRegistryListenerTest {
    private final MetricRegistryListener listener = new MetricRegistryListener() {
    };

    @Test
    public void returnsSameGauge() {
        Gauge<?> gauge = mock(Gauge.class);
        assertThat(listener.onGaugeAdded("blah", gauge))
                .isEqualTo(gauge);
    }

    @Test
    public void returnsSameCounter() {
        Counter counter = mock(Counter.class);
        assertThat(listener.onCounterAdded("blah", counter))
                .isEqualTo(counter);
    }

    @Test
    public void returnsSameHistogram() {
        Histogram histogram = mock(Histogram.class);
        assertThat(listener.onHistogramAdded("blah", histogram))
                .isEqualTo(histogram);
    }

    @Test
    public void noOpsOnMeterAdded() {
        Meter meter = mock(Meter.class);
        assertThat(listener.onMeterAdded("blah", meter))
                .isEqualTo(meter);
    }

    @Test
    public void noOpsOnTimerAdded() {
        Timer timer = mock(Timer.class);
        assertThat(listener.onTimerAdded("blah", timer))
                .isEqualTo(timer);
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
