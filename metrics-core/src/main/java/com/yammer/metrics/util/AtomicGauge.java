package com.yammer.metrics.util;

import com.yammer.metrics.core.Gauge;

public class AtomicGauge<T> extends Gauge<T> {
    private volatile T value = null;

    public void setValue(T value) {
        this.value = value;
    }

    @Override
    public T getValue() {
        return value;
    }
}
