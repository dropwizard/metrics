package io.dropwizard.metrics.jersey2;

import io.dropwizard.metrics.Clock;

public class TestClock extends Clock {
    public long tick;
    @Override
    public long getTick() {
        return tick;
    }
}
