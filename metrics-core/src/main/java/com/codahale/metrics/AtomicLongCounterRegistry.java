package com.codahale.metrics;

import java.util.SortedMap;

public class AtomicLongCounterRegistry extends MetricRegistry {
    @Override
    public Counter counter(String name) {
        return getOrAdd(name, counterBuilder);
    }

    AtomicLongCounterBuilder counterBuilder = new AtomicLongCounterBuilder();


    protected static class AtomicLongCounterBuilder implements MetricBuilder<AtomicLongCounter> {
        @Override
        public AtomicLongCounter newMetric() {
            return new AtomicLongCounter();
        }

        @Override
        public boolean isInstance(Metric metric) {
            return AtomicLongCounter.class.isInstance(metric);
        }
    }

    public SortedMap<String, AtomicLongCounter> getALCounters() {
        return getALCounters(MetricFilter.ALL);
    }

    public SortedMap<String, AtomicLongCounter> getALCounters(MetricFilter filter) {
        return getMetrics(AtomicLongCounter.class, filter);
    }
}