package com.yammer.metrics.util;

import com.yammer.metrics.core.Gauge;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Returns a {@code 1} the first time it's called, a {@code 0} every time after that.
 */
public class ToggleGauge extends Gauge<Integer> {
    private final AtomicInteger value = new AtomicInteger(1);

    @Override
    public Integer value() {
        try {
            return value.get();
        } finally {
            this.value.set(0);
        }
    }
}
