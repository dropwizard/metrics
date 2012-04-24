package com.yammer.metrics.core;

import java.util.concurrent.atomic.AtomicLong;

/**
 * An Gauge with Long number as the value. Added by Yongjun Rong
 */
public class ValueGauge extends Gauge<Long> {
    private final AtomicLong value;

    public ValueGauge() {
        this.value = new AtomicLong(0L);
    }

    public ValueGauge(Long val) {
        this.value = new AtomicLong(val.longValue());
    }

    /**
     * Set the value to a give value.
     */
    public void set(Long val) {
        value.set(val.longValue());
    }
    
    @Override
    public Long value() {
            return Long.valueOf(value.get());
    }
   
}
