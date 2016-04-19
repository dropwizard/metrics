package io.dropwizard.metrics;

/**
 * A variant of MetricRegistry that creates the Null versions of metrics
 */
public class NullMetricRegistry extends MetricRegistry {
    /**
     * Returns a {@link MetricBuilder} that captures the notion of default
     * counters. This method is protected so that subclasses may override metric
     * building.
     * 
     * @return a builder that can construct a {@link NullCounter}
     */
    protected MetricBuilder<Counter> getCounterMetricBuilder() {
        return NULL_COUNTER_BUILDER;
    }

    /**
     * Returns a {@link MetricBuilder} that captures the notion of default
     * histograms. This method is protected so that subclasses may override
     * metric building.
     * 
     * @return a builder that can construct a {@link NullHistogram}
     */
    protected MetricBuilder<Histogram> getHistogramMetricBuilder() {
        return NULL_HISTOGRAM_BUILDER;
    }

    /**
     * Returns a {@link MetricBuilder} that captures the notion of default
     * meters. This method is protected so that subclasses may override metric
     * building.
     * 
     * @return a builder that can construct a {@link NullMeter}
     */
    protected MetricBuilder<Meter> getMeterMetricBuilder() {
        return NULL_METER_BUILDER;
    }

    /**
     * Returns a {@link MetricBuilder} that captures the notion of default
     * timers. This method is protected so that subclasses may override metric
     * building.
     * 
     * @return a builder that can construct a {@link NullTimer}
     */
    protected MetricBuilder<Timer> getTimerMetricBuilder() {
        return NULL_TIMER_BUILDER;
    }

    protected static final MetricBuilder<Counter> NULL_COUNTER_BUILDER = new MetricBuilder<Counter>() {
        @Override
        public Counter newMetric() {
            return new NullCounter();
        }

        @Override
        public boolean isInstance(Metric metric) {
            return metric instanceof Counter;
        }
    };

    protected static final MetricBuilder<Histogram> NULL_HISTOGRAM_BUILDER = new MetricBuilder<Histogram>() {
        @Override
        public Histogram newMetric() {
            return new NullHistogram();
        }

        @Override
        public boolean isInstance(Metric metric) {
            return metric instanceof Histogram;
        }
    };

    protected static final MetricBuilder<Meter> NULL_METER_BUILDER = new MetricBuilder<Meter>() {
        @Override
        public Meter newMetric() {
            return new NullMeter();
        }

        @Override
        public boolean isInstance(Metric metric) {
            return metric instanceof Meter;
        }
    };

    protected static final MetricBuilder<Timer> NULL_TIMER_BUILDER = new MetricBuilder<Timer>() {
        @Override
        public Timer newMetric() {
            return new NullTimer();
        }

        @Override
        public boolean isInstance(Metric metric) {
            return metric instanceof Timer;
        }
    };
}
