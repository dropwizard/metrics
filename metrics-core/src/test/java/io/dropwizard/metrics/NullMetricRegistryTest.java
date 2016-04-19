package io.dropwizard.metrics;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class NullMetricRegistryTest {

    @Test
    public void accessingACounterRegistersAndReusesANullCounter() throws Exception {
        final MetricRegistry registry = new NullMetricRegistry();
        final Counter counter1 = registry.counter("thing");
        final Counter counter2 = registry.counter("thing");

        assertThat(counter1)
                .isSameAs(counter2);

        assertThat(counter1)
                .isInstanceOf(Counter.class);

        assertThat(counter1)
                .isExactlyInstanceOf(NullCounter.class);
    }

    @Test
    public void accessingAMeterRegistersAndReusesANullMeter() throws Exception {
        final MetricRegistry registry = new NullMetricRegistry();
        final Meter meter1 = registry.meter("thing");
        final Meter meter2 = registry.meter("thing");

        assertThat(meter1)
                .isSameAs(meter2);

        assertThat(meter1)
                .isInstanceOf(Meter.class);

        assertThat(meter1)
                .isExactlyInstanceOf(NullMeter.class);
    }

    @Test
    public void accessingAHistogramRegistersAndReusesANullHistogram() throws Exception {
        final MetricRegistry registry = new NullMetricRegistry();
        final Histogram histogram1 = registry.histogram("thing");
        final Histogram histogram2 = registry.histogram("thing");

        assertThat(histogram1)
                .isSameAs(histogram2);

        assertThat(histogram1)
                .isInstanceOf(Histogram.class);

        assertThat(histogram1)
                .isExactlyInstanceOf(NullHistogram.class);
    }

    @Test
    public void accessingATimerRegistersAndReusesANullTimer() throws Exception {
        final MetricRegistry registry = new NullMetricRegistry();
        final Timer timer1 = registry.timer("thing");
        final Timer timer2 = registry.timer("thing");

        assertThat(timer1)
                .isSameAs(timer2);

        assertThat(timer1)
                .isInstanceOf(Timer.class);

        assertThat(timer1)
                .isExactlyInstanceOf(NullTimer.class);
    }

    @Test
    public void addingRegistriesToNullMetricRegistryKeepsTypes() throws Exception {
        final MetricRegistry normalRegistry = new MetricRegistry();
        final Meter meter = normalRegistry.meter("normalMeter");

        final MetricRegistry nullRegistryInitial = new NullMetricRegistry();
        final Counter counter = nullRegistryInitial.counter("nullCounter");

        final MetricRegistry nullRegistryFinal = new NullMetricRegistry();
        final Histogram histogram = nullRegistryFinal.histogram("nullHistogram");

        nullRegistryFinal.registerAll(normalRegistry);
        nullRegistryFinal.registerAll(nullRegistryInitial);

        assertThat(nullRegistryFinal.meter("normalMeter"))
                .isSameAs(meter);

        assertThat(nullRegistryFinal.meter("normalMeter"))
                .isSameAs(normalRegistry.meter("normalMeter"));

        assertThat(nullRegistryFinal.meter("normalMeter"))
                .isInstanceOf(Meter.class);

        assertThat(nullRegistryFinal.meter("normalMeter"))
                .isExactlyInstanceOf(Meter.class);

        assertThat(nullRegistryFinal.counter("nullCounter"))
                .isSameAs(nullRegistryInitial.counter("nullCounter"));

        assertThat(nullRegistryFinal.counter("nullCounter"))
                .isSameAs(counter);

        assertThat(nullRegistryFinal.counter("nullCounter"))
                .isInstanceOf(Counter.class);

        assertThat(nullRegistryFinal.counter("nullCounter"))
                .isExactlyInstanceOf(NullCounter.class);

        assertThat(nullRegistryFinal.histogram("nullHistogram"))
                .isSameAs(histogram);
        
        assertThat(nullRegistryFinal.histogram("nullHistogram"))
                .isInstanceOf(Histogram.class);
        
        assertThat(nullRegistryFinal.histogram("nullHistogram"))
                .isExactlyInstanceOf(NullHistogram.class);
    }

    @Test
    public void addingNullMetricRegistryToMetricRegistryKeepsTypes() throws Exception {
        final MetricRegistry nullRegistry = new NullMetricRegistry();
        final Counter counter = nullRegistry.counter("nullCounter");

        final MetricRegistry normalRegistry = new MetricRegistry();
        final Histogram histogram = normalRegistry.histogram("normalHistogram");

        normalRegistry.registerAll(nullRegistry);

        assertThat(normalRegistry.counter("nullCounter"))
                .isSameAs(nullRegistry.counter("nullCounter"));

        assertThat(normalRegistry.counter("nullCounter"))
                .isSameAs(counter);

        assertThat(normalRegistry.counter("nullCounter"))
                .isInstanceOf(Counter.class);

        assertThat(normalRegistry.counter("nullCounter"))
                .isExactlyInstanceOf(NullCounter.class);

        assertThat(normalRegistry.histogram("normalHistogram"))
                .isSameAs(histogram);
        
        assertThat(normalRegistry.histogram("normalHistogram"))
                .isInstanceOf(Histogram.class);
        
        assertThat(normalRegistry.histogram("normalHistogram"))
                .isExactlyInstanceOf(Histogram.class);
    }
}
