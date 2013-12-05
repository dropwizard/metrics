package com.codahale.metrics;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A MetricRegistry which also supports DeltaCounters
 */
public class DeltaMetricRegistry extends MetricRegistry {

    DeltaCounterBuilder counterBuilder = new DeltaCounterBuilder();
    private final List<DeltaMetricListener> metricListeners = new ArrayList<DeltaMetricListener>();
    DeltaReporter deltaReporter;

    public DeltaMetricRegistry() {
        super();
    }

    private static final AtomicInteger FACTORY_ID = new AtomicInteger();


    public DeltaCounter deltaCounter(String name) {
        return getOrAdd(name, counterBuilder);
    }

    public void scheduleDeltaListeners(long period, TimeUnit unit) {
        if (deltaReporter != null) {
            throw new UnsupportedOperationException("can only start delta listeners once");
        }
        deltaReporter = new DeltaReporter(this, "DeltaReporter", MetricFilter.ALL, TimeUnit.SECONDS, TimeUnit.MILLISECONDS);
        deltaReporter.start(period,unit);
    }

    public void stopDeltaListeners() {
        if (deltaReporter != null) {
            deltaReporter.stop();
            deltaReporter = null;
        }
    }



    protected static class DeltaCounterBuilder implements MetricBuilder<DeltaCounter> {
        @Override
        public DeltaCounter newMetric() {
            return new DeltaCounter();
        }

        @Override
        public boolean isInstance(Metric metric) {
            return DeltaCounter.class.isInstance(metric);
        }
    }

    SortedMap<String, DeltaCounter> getDeltaCounters() {
        return getDeltaCounters(MetricFilter.ALL);
    }

    SortedMap<String, DeltaCounter> getDeltaCounters(MetricFilter filter) {
        return getMetrics(DeltaCounter.class, filter);
    }

    /**
     * this gets current value of all the delta counters, AND RESET them
     * @return
     */
    SortedMap<String,Long> getAndResetDeltas() {
        SortedMap<String,Long> deltaCounters = new TreeMap<String, Long>();
        for(Map.Entry<String,DeltaCounter> e: getDeltaCounters().entrySet()) {
            deltaCounters.put(e.getKey(), e.getValue().getAndReset());
        }
        return deltaCounters;
    }


    class DeltaReporter extends ScheduledReporter {

        protected DeltaReporter(DeltaMetricRegistry registry,
                                String name,
                                MetricFilter filter,
                                TimeUnit rateUnit,
                                TimeUnit durationUnit) {
            super(registry,name,filter,rateUnit,durationUnit);
        }


        public void report(SortedMap<String, Gauge> gauges,
                           SortedMap<String, Counter> counters,
                           SortedMap<String, Histogram> histograms,
                           SortedMap<String, Meter> meters,
                           SortedMap<String, Timer> timers) {
            //the key thing is, we get the deltas once, and pass it to all listeners
            SortedMap<String,Long> deltas = getAndResetDeltas();
            for(DeltaMetricListener m: metricListeners) {
                m.report(gauges, counters, deltas, histograms, meters, timers);
            }
        }
    }
}