package com.codahale.metrics.jersey2;

import com.codahale.metrics.Clock;

public class TestClock extends Clock {
    public long tick;
    @Override
    public long getTick() {
        return tick;
    }
}
