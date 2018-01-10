package com.codahale.metrics;

@Deprecated
public interface Gauge<T> extends Metric {

    T getValue();

    @Override
    default io.dropwizard.metrics5.Gauge<T> getDelegate() {
        return new GaugeAdapter<>(this);
    }

    @SuppressWarnings("unchecked")
    static <T> Gauge<T> of(io.dropwizard.metrics5.Gauge<T> gauge) {
        if (gauge instanceof GaugeAdapter) {
            return ((GaugeAdapter) gauge).delegate;
        }
        return gauge::getValue;
    }

    class GaugeAdapter<T> implements io.dropwizard.metrics5.Gauge<T> {

        private final Gauge<T> delegate;

        GaugeAdapter(Gauge<T> gauge) {
            this.delegate = gauge;
        }

        Gauge<T> getGauge() {
            return delegate;
        }

        @Override
        public T getValue() {
            return delegate.getValue();
        }
    }
}
