package com.codahale.metrics;

import java.util.SortedMap;

/**
 * A MetricRegistry which also supports DeltaCounters
 */
public class DeltaMetricRegistry extends MetricRegistry {

    public DeltaCounter deltaCounter(String name) {
        return getOrAdd(name, counterBuilder);
    }

    DeltaCounterBuilder counterBuilder = new DeltaCounterBuilder();


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

    public SortedMap<String, DeltaCounter> getDeltaCounters() {
        return getDeltaCounters(MetricFilter.ALL);
    }

    public SortedMap<String, DeltaCounter> getDeltaCounters(MetricFilter filter) {
        return getMetrics(DeltaCounter.class, filter);
    }
}