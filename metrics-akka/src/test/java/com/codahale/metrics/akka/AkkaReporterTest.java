package com.codahale.metrics.akka;

import akka.actor.ActorSystem;
import akka.testkit.TestProbe;
import com.codahale.metrics.*;
import org.junit.Before;
import org.junit.Test;

import java.util.Locale;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit test class for {@link AkkaReporter}.
 */
public class AkkaReporterTest {
    private final Clock clock = mock(Clock.class);
    private final TestProbe testProbe = TestProbe.apply(ActorSystem.create("metrics-akka-test"));
    private final AkkaReporter akkaReporter= AkkaReporter.forRegistryAndReceiver(mock(MetricRegistry.class), testProbe.ref())
        .formattedFor(Locale.US)
        .withClock(clock)
        .formattedFor(TimeZone.getTimeZone("PST"))
        .convertRatesTo(TimeUnit.SECONDS)
        .convertDurationsTo(TimeUnit.MILLISECONDS)
        .filter(MetricFilter.ALL)
        .build();
    private TreeMap<String,Gauge> gauges;
    private TreeMap<String,Counter> counters;
    private TreeMap<String,Histogram> histograms;
    private TreeMap<String,Meter> meters;
    private TreeMap<String,Timer> timers;

    @Before
    public void setUp() {
        when(clock.getTime()).thenReturn(1363568676000L);

        gauges = new TreeMap<>();
        counters = new TreeMap<>();
        histograms = new TreeMap<>();
        meters = new TreeMap<>();
        timers = new TreeMap<>();
    }

    @Test
    public void allowsNullMetricsMaps() {
        gauges.put("gauge-1", mock(Gauge.class));
        counters.put("counter-1", mock(Counter.class));
        histograms = null;
        meters.put("meter-1", mock(Meter.class));
        timers.put("timer-1", mock(Timer.class));

        akkaReporter.report(gauges, counters, histograms, meters, timers);
    }

    @Test
    public void allowsEmptyMetricsMaps() {
        gauges.put("gauge-1", mock(Gauge.class));
        histograms.put("histogram-1", mock(Histogram.class));
        meters.put("meter-1", mock(Meter.class));
        timers.put("timer-1", mock(Timer.class));

        akkaReporter.report(gauges, counters, histograms, meters, timers);
    }
}
