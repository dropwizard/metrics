package com.codahale.metrics;

import io.dropwizard.metrics5.MetricName;

import java.util.EventListener;

@Deprecated
public interface MetricRegistryListener extends EventListener {

    abstract class Base implements MetricRegistryListener {
        @Override
        public void onGaugeAdded(String name, Gauge<?> gauge) {
        }

        @Override
        public void onGaugeRemoved(String name) {
        }

        @Override
        public void onCounterAdded(String name, Counter counter) {
        }

        @Override
        public void onCounterRemoved(String name) {
        }

        @Override
        public void onHistogramAdded(String name, Histogram histogram) {
        }

        @Override
        public void onHistogramRemoved(String name) {
        }

        @Override
        public void onMeterAdded(String name, Meter meter) {
        }

        @Override
        public void onMeterRemoved(String name) {
        }

        @Override
        public void onTimerAdded(String name, Timer timer) {
        }

        @Override
        public void onTimerRemoved(String name) {
        }
    }

    void onGaugeAdded(String name, Gauge<?> gauge);

    void onGaugeRemoved(String name);

    void onCounterAdded(String name, Counter counter);

    void onCounterRemoved(String name);

    void onHistogramAdded(String name, Histogram histogram);

    void onHistogramRemoved(String name);

    void onMeterAdded(String name, Meter meter);

    void onMeterRemoved(String name);

    void onTimerAdded(String name, Timer timer);

    void onTimerRemoved(String name);

    class Adapter implements io.dropwizard.metrics5.MetricRegistryListener {

        private MetricRegistryListener delegate;

        public Adapter(MetricRegistryListener delegate) {
            this.delegate = delegate;
        }

        @Override
        public void onGaugeAdded(MetricName name, io.dropwizard.metrics5.Gauge<?> gauge) {
            delegate.onGaugeAdded(name.getKey(), Gauge.of(gauge));
        }

        @Override
        public void onGaugeRemoved(MetricName name) {
            delegate.onGaugeRemoved(name.getKey());
        }

        @Override
        public void onCounterAdded(MetricName name, io.dropwizard.metrics5.Counter counter) {
            delegate.onCounterAdded(name.getKey(), new Counter(counter));
        }

        @Override
        public void onCounterRemoved(MetricName name) {
            delegate.onCounterRemoved(name.getKey());
        }

        @Override
        public void onHistogramAdded(MetricName name, io.dropwizard.metrics5.Histogram histogram) {
            delegate.onHistogramAdded(name.getKey(), new Histogram(histogram));
        }

        @Override
        public void onHistogramRemoved(MetricName name) {
            delegate.onHistogramRemoved(name.getKey());
        }

        @Override
        public void onMeterAdded(MetricName name, io.dropwizard.metrics5.Meter meter) {
            delegate.onMeterAdded(name.getKey(), new Meter(meter));
        }

        @Override
        public void onMeterRemoved(MetricName name) {
            delegate.onMeterRemoved(name.getKey());
        }

        @Override
        public void onTimerAdded(MetricName name, io.dropwizard.metrics5.Timer timer) {
            delegate.onTimerAdded(name.getKey(), new Timer(timer));
        }

        @Override
        public void onTimerRemoved(MetricName name) {
            delegate.onTimerRemoved(name.getKey());
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof Adapter) {
                Adapter adapter = (Adapter) o;
                return delegate.equals(adapter.delegate);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return delegate.hashCode();
        }
    }
}