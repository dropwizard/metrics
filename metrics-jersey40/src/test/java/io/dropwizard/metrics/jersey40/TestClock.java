package io.dropwizard.metrics.jersey40;

import com.codahale.metrics.Clock;

public class TestClock extends Clock {

    public long tick;

    @Override
    public long getTick() {
        return tick;
    }
}
