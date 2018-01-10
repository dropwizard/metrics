package com.codahale.metrics;

import io.dropwizard.metrics5.MetricName;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Deprecated
public interface MetricSet extends Metric {

    Map<String, Metric> getMetrics();

    @Override
    default io.dropwizard.metrics5.MetricSet getDelegate() {
        return new Adapter(this);
    }

    static MetricSet of(io.dropwizard.metrics5.MetricSet original) {
        return new MetricSet() {
            @Override
            public Map<String, Metric> getMetrics() {
                final Map<String, Metric> items = new HashMap<>();
                for (Map.Entry<MetricName, io.dropwizard.metrics5.Metric> entry : original.getMetrics().entrySet()) {
                    items.put(entry.getKey().getKey(), Metric.of(entry.getValue()));
                }
                return Collections.unmodifiableMap(items);
            }

            @Override
            public io.dropwizard.metrics5.MetricSet getDelegate() {
                return original;
            }
        };
    }

    class Adapter implements io.dropwizard.metrics5.MetricSet {

        private final MetricSet delegate;

        Adapter(MetricSet delegate) {
            this.delegate = delegate;
        }

        @Override
        public Map<MetricName, io.dropwizard.metrics5.Metric> getMetrics() {
            final Map<MetricName, io.dropwizard.metrics5.Metric> items = new HashMap<>();
            for (Map.Entry<String, Metric> entry : delegate.getMetrics().entrySet()) {
                items.put(MetricName.build(entry.getKey()), entry.getValue().getDelegate());
            }
            return Collections.unmodifiableMap(items);
        }
    }

}
