package com.codahale.metrics;

import java.io.OutputStream;

@Deprecated
public abstract class Snapshot {

    public abstract double getValue(double quantile);

    public abstract long[] getValues();

    public abstract int size();

    public double getMedian() {
        return getValue(0.5);
    }

    public double get75thPercentile() {
        return getValue(0.75);
    }

    public double get95thPercentile() {
        return getValue(0.95);
    }

    public double get98thPercentile() {
        return getValue(0.98);
    }

    public double get99thPercentile() {
        return getValue(0.99);
    }

    public double get999thPercentile() {
        return getValue(0.999);
    }

    public abstract long getMax();

    public abstract double getMean();

    public abstract long getMin();

    public abstract double getStdDev();

    public abstract void dump(OutputStream output);

    public static Snapshot of(io.dropwizard.metrics5.Snapshot delegate) {
        return new Snapshot() {

            @Override
            public double getValue(double quantile) {
                return delegate.getValue(quantile);
            }

            @Override
            public long[] getValues() {
                return delegate.getValues();
            }

            @Override
            public int size() {
                return delegate.size();
            }

            @Override
            public long getMax() {
                return delegate.getMax();
            }

            @Override
            public double getMean() {
                return delegate.getMean();
            }

            @Override
            public long getMin() {
                return delegate.getMin();
            }

            @Override
            public double getStdDev() {
                return delegate.getStdDev();
            }

            @Override
            public void dump(OutputStream output) {
                delegate.dump(output);
            }
        };
    }
}
