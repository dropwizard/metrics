package com.codahale.metrics;

import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.fest.assertions.api.Assertions.assertThat;


public class DeltaMetricRegistryTest {
    private final DeltaMetricRegistry registry = new DeltaMetricRegistry();
    private final List<MyListener> myListeners = new ArrayList<MyListener>();


    @Before
    public void setUp() throws Exception {
        for (int i = 0; i < 3; i++) {
            MyListener l = new MyListener();
            registry.addDeltaListener(l);
            myListeners.add(l);
        }
    }

    @Test
    public void accessingACounterRegistersAndReusesTheCounter() throws Exception {
        final DeltaCounter counter1 = registry.deltaCounter("thing");
        final DeltaCounter counter2 = registry.deltaCounter("thing");


        assertThat(counter1)
                .isSameAs(counter2);
    }

    @Test
    public void deltaListenerGetsCorrectIncrements() throws Exception {
        final DeltaCounter dc = registry.deltaCounter("thing");

        dc.inc(5);
        registry.runDeltaReport();
        TreeMap<String,Long> exp = new TreeMap<String,Long>();
        exp.put("thing", 5l);
        for(MyListener l: myListeners) {
            assertThat(l.lastDeltaCounters).isEqualTo(exp);
        }

        registry.runDeltaReport();
        exp.put("thing", 0l);
        for(MyListener l: myListeners) {
            assertThat(l.lastDeltaCounters).isEqualTo(exp);
        }

        dc.dec(5434);
        registry.runDeltaReport();
        exp.put("thing", -5434l);
        for(MyListener l: myListeners) {
            assertThat(l.lastDeltaCounters).isEqualTo(exp);
        }


    }


    private static class MyListener implements DeltaMetricListener {
        Map<String,Long> lastDeltaCounters = null;
        public void report(SortedMap<String, Gauge> gauges,
                           SortedMap<String, Counter> counters,
                           SortedMap<String, Long> deltaCounters,
                           SortedMap<String, Histogram> histograms,
                           SortedMap<String, Meter> meters,
                           SortedMap<String, Timer> timers) {
            lastDeltaCounters = deltaCounters;
        }
    }



}
